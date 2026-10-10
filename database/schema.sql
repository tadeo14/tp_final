-- =============================================================================
-- Fixture de Torneos de Básquet (TFI UTN) — Esquema de base de datos (P0)
-- Motor: PostgreSQL
--
-- DER y justificación de cada tabla: docs/der.md
--
-- Criterio general: la base garantiza la INTEGRIDAD de los datos (claves,
-- unicidad, valores válidos). Las restricciones del fixture R1–R8 las verifica
-- únicamente el ValidadorRestricciones del backend (CLAUDE.md, regla 3).
--
-- ATENCIÓN: este script BORRA todas las tablas (y sus datos) antes de crearlas.
-- Es para desarrollo y pruebas locales. No ejecutarlo contra una base con
-- datos reales: se pierde todo.
-- =============================================================================

-- Borra las tablas existentes y TODOS sus datos (ver advertencia arriba).
DROP TABLE IF EXISTS partido, equipo, zona, club, franja_horaria, rama,
                     categoria, cancha, sede, torneo, organizador CASCADE;

-- -----------------------------------------------------------------------------
-- Organizador: dueño del torneo. Es el único que ve y edita un torneo en
-- BORRADOR. El público consulta sin registrarse.
-- -----------------------------------------------------------------------------
CREATE TABLE organizador (
    id            BIGSERIAL    PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

-- -----------------------------------------------------------------------------
-- Torneo y parámetros que aplican a todo el torneo.
-- Los días del torneo no se guardan: salen de las franjas horarias.
-- -----------------------------------------------------------------------------
CREATE TABLE torneo (
    id                  BIGSERIAL    PRIMARY KEY,
    organizador_id      BIGINT       NOT NULL REFERENCES organizador (id),
    nombre              VARCHAR(150) NOT NULL,
    turnos_cada_min     INTEGER      NOT NULL,
    recambio_min        INTEGER      NOT NULL,
    descanso_minimo_min INTEGER      NOT NULL,
    estado              VARCHAR(20)  NOT NULL DEFAULT 'BORRADOR',
    CONSTRAINT ck_torneo_turnos   CHECK (turnos_cada_min > 0),
    CONSTRAINT ck_torneo_recambio CHECK (recambio_min >= 0),
    CONSTRAINT ck_torneo_descanso CHECK (descanso_minimo_min >= 0),
    CONSTRAINT ck_torneo_estado   CHECK (estado IN ('BORRADOR', 'PUBLICADO'))
);

-- -----------------------------------------------------------------------------
-- Franjas horarias: ventanas habilitadas para jugar, por día (R4).
-- -----------------------------------------------------------------------------
CREATE TABLE franja_horaria (
    id        BIGSERIAL PRIMARY KEY,
    torneo_id BIGINT    NOT NULL REFERENCES torneo (id) ON DELETE CASCADE,
    fecha     DATE      NOT NULL,
    desde     TIME      NOT NULL,
    hasta     TIME      NOT NULL,
    CONSTRAINT ck_franja_horas CHECK (hasta > desde),
    CONSTRAINT uq_franja       UNIQUE (torneo_id, fecha, desde)
);

-- -----------------------------------------------------------------------------
-- Sedes y canchas (R5: solo se programa en canchas habilitadas).
-- -----------------------------------------------------------------------------
CREATE TABLE sede (
    id        BIGSERIAL    PRIMARY KEY,
    torneo_id BIGINT       NOT NULL REFERENCES torneo (id) ON DELETE CASCADE,
    nombre    VARCHAR(100) NOT NULL,
    direccion VARCHAR(200),
    CONSTRAINT uq_sede_nombre UNIQUE (torneo_id, nombre)
);

CREATE TABLE cancha (
    id         BIGSERIAL    PRIMARY KEY,
    sede_id    BIGINT       NOT NULL REFERENCES sede (id) ON DELETE CASCADE,
    nombre     VARCHAR(100) NOT NULL,
    habilitada BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_cancha_nombre UNIQUE (sede_id, nombre)
);

-- -----------------------------------------------------------------------------
-- Categorías (U9, U11, U13...) con sus parámetros de duración y tope diario.
-- La duración programada y el bloque se calculan (D7, D8), no se guardan.
-- -----------------------------------------------------------------------------
CREATE TABLE categoria (
    id                     BIGSERIAL   PRIMARY KEY,
    torneo_id              BIGINT      NOT NULL REFERENCES torneo (id) ON DELETE CASCADE,
    nombre                 VARCHAR(50) NOT NULL,
    minutos_netos          INTEGER     NOT NULL,
    cantidad_cuartos       INTEGER     NOT NULL,  -- informativo: no entra en el cálculo
    pausas_min             INTEGER     NOT NULL,  -- total entre cuartos + entretiempo
    margen_detenciones_min INTEGER     NOT NULL,
    max_partidos_dia       INTEGER     NOT NULL,
    CONSTRAINT uq_categoria_nombre  UNIQUE (torneo_id, nombre),
    CONSTRAINT uq_categoria_torneo  UNIQUE (id, torneo_id),
    CONSTRAINT ck_categoria_netos   CHECK (minutos_netos > 0),
    CONSTRAINT ck_categoria_cuartos CHECK (cantidad_cuartos > 0),
    CONSTRAINT ck_categoria_pausas  CHECK (pausas_min >= 0),
    CONSTRAINT ck_categoria_margen  CHECK (margen_detenciones_min >= 0),
    CONSTRAINT ck_categoria_tope    CHECK (max_partidos_dia > 0)
);

-- -----------------------------------------------------------------------------
-- Ramas (Masculino, Femenino...) configurables por torneo.
-- -----------------------------------------------------------------------------
CREATE TABLE rama (
    id        BIGSERIAL   PRIMARY KEY,
    torneo_id BIGINT      NOT NULL REFERENCES torneo (id) ON DELETE CASCADE,
    nombre    VARCHAR(50) NOT NULL,
    CONSTRAINT uq_rama_nombre UNIQUE (torneo_id, nombre),
    CONSTRAINT uq_rama_torneo UNIQUE (id, torneo_id)
);

-- -----------------------------------------------------------------------------
-- Zonas de una categoría y rama. La cantidad de zonas no se guarda aparte:
-- es la cantidad de filas de esta tabla para esa categoría y rama.
-- Las FKs compuestas aseguran que categoría y rama sean del mismo torneo.
-- -----------------------------------------------------------------------------
CREATE TABLE zona (
    id           BIGSERIAL   PRIMARY KEY,
    torneo_id    BIGINT      NOT NULL REFERENCES torneo (id) ON DELETE CASCADE,
    categoria_id BIGINT      NOT NULL,
    rama_id      BIGINT      NOT NULL,
    nombre       VARCHAR(50) NOT NULL,  -- ej. "Zona A"
    CONSTRAINT fk_zona_categoria FOREIGN KEY (categoria_id, torneo_id)
                                 REFERENCES categoria (id, torneo_id) ON DELETE CASCADE,
    CONSTRAINT fk_zona_rama      FOREIGN KEY (rama_id, torneo_id)
                                 REFERENCES rama (id, torneo_id) ON DELETE CASCADE,
    CONSTRAINT uq_zona_nombre    UNIQUE (categoria_id, rama_id, nombre),
    CONSTRAINT uq_zona_division  UNIQUE (id, categoria_id, rama_id)
);

-- -----------------------------------------------------------------------------
-- Clubes que inscriben equipos en el torneo.
-- -----------------------------------------------------------------------------
CREATE TABLE club (
    id        BIGSERIAL    PRIMARY KEY,
    torneo_id BIGINT       NOT NULL REFERENCES torneo (id) ON DELETE CASCADE,
    nombre    VARCHAR(150) NOT NULL,
    CONSTRAINT uq_club_nombre UNIQUE (torneo_id, nombre),
    CONSTRAINT uq_club_torneo UNIQUE (id, torneo_id)
);

-- -----------------------------------------------------------------------------
-- Equipos inscriptos. Datos personales mínimos: responsable y teléfono.
-- zona_id y orden_en_zona son el resultado del sorteo (D5): quedan NULL hasta
-- sortear y no se rehacen solos. La FK a zona incluye categoría y rama para
-- que un equipo no pueda caer en una zona de otra categoría o rama.
-- -----------------------------------------------------------------------------
CREATE TABLE equipo (
    id                   BIGSERIAL    PRIMARY KEY,
    torneo_id            BIGINT       NOT NULL REFERENCES torneo (id) ON DELETE CASCADE,
    club_id              BIGINT       NOT NULL,
    categoria_id         BIGINT       NOT NULL,
    rama_id              BIGINT       NOT NULL,
    nombre               VARCHAR(150) NOT NULL,
    responsable_nombre   VARCHAR(100) NOT NULL,
    responsable_telefono VARCHAR(30)  NOT NULL,
    zona_id              BIGINT,
    orden_en_zona        INTEGER,
    CONSTRAINT fk_equipo_club      FOREIGN KEY (club_id, torneo_id)
                                   REFERENCES club (id, torneo_id),
    CONSTRAINT fk_equipo_categoria FOREIGN KEY (categoria_id, torneo_id)
                                   REFERENCES categoria (id, torneo_id),
    CONSTRAINT fk_equipo_rama      FOREIGN KEY (rama_id, torneo_id)
                                   REFERENCES rama (id, torneo_id),
    CONSTRAINT fk_equipo_zona      FOREIGN KEY (zona_id, categoria_id, rama_id)
                                   REFERENCES zona (id, categoria_id, rama_id),
    CONSTRAINT uq_equipo_nombre    UNIQUE (torneo_id, categoria_id, rama_id, nombre),
    CONSTRAINT uq_equipo_orden     UNIQUE (zona_id, orden_en_zona),
    CONSTRAINT uq_equipo_zona      UNIQUE (id, zona_id),
    CONSTRAINT ck_equipo_sorteo    CHECK ((zona_id IS NULL) = (orden_en_zona IS NULL)),
    CONSTRAINT ck_equipo_orden     CHECK (orden_en_zona IS NULL OR orden_en_zona > 0)
);

-- -----------------------------------------------------------------------------
-- Partidos del fixture.
-- Los crea el GeneradorEnfrentamientos (zona, equipos, ronda) y el Programador
-- les asigna cancha e inicio. El fin NO se guarda: es inicio + bloque, y el
-- bloque se calcula a partir de la categoría y el torneo (D8).
-- Los "libres" de zonas impares no se guardan: un equipo está libre en la
-- ronda en la que no figura en ningún partido.
-- -----------------------------------------------------------------------------
CREATE TABLE partido (
    id                  BIGSERIAL PRIMARY KEY,
    zona_id             BIGINT    NOT NULL REFERENCES zona (id) ON DELETE CASCADE,
    equipo_local_id     BIGINT    NOT NULL,
    equipo_visitante_id BIGINT    NOT NULL,
    ronda               INTEGER   NOT NULL,
    cancha_id           BIGINT    REFERENCES cancha (id),  -- que sea del torneo y esté habilitada lo controla R5
    inicio              TIMESTAMP,
    -- Ambos equipos tienen que pertenecer a la zona del partido.
    CONSTRAINT fk_partido_local     FOREIGN KEY (equipo_local_id, zona_id)
                                    REFERENCES equipo (id, zona_id) ON DELETE CASCADE,
    CONSTRAINT fk_partido_visitante FOREIGN KEY (equipo_visitante_id, zona_id)
                                    REFERENCES equipo (id, zona_id) ON DELETE CASCADE,
    CONSTRAINT ck_partido_equipos   CHECK (equipo_local_id <> equipo_visitante_id),
    CONSTRAINT ck_partido_ronda     CHECK (ronda > 0),
    -- Sin programar: sin cancha ni inicio. Programado: ambos.
    CONSTRAINT ck_partido_programa  CHECK ((cancha_id IS NULL) = (inicio IS NULL))
);

CREATE INDEX ix_partido_zona      ON partido (zona_id);
CREATE INDEX ix_partido_local     ON partido (equipo_local_id);
CREATE INDEX ix_partido_visitante ON partido (equipo_visitante_id);
CREATE INDEX ix_partido_cancha    ON partido (cancha_id, inicio);
CREATE INDEX ix_equipo_zona       ON equipo (zona_id);

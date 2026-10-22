-- =============================================================================
-- Migración V2: versiones publicadas del fixture (D46, D47, D48).
--
-- Cada vez que se publica un torneo se crea una "publicación" con número 1, 2, 3… y una FOTO de sus partidos.
-- La foto es inmutable: nunca se modifica. Así, si después se reabre el torneo y se corrige el fixture,
-- lo que ya se comunicó al público queda registrado y la vista pública sigue mostrando la última versión.
--
-- NO MODIFICAR una vez aplicada: los cambios van en una migración nueva (V3__...).
-- =============================================================================

CREATE TABLE publicacion (
    id            BIGSERIAL    PRIMARY KEY,
    torneo_id     BIGINT       NOT NULL REFERENCES torneo (id) ON DELETE CASCADE,
    numero        INTEGER      NOT NULL,
    publicada_en  TIMESTAMPTZ  NOT NULL,
    motivo        VARCHAR(500),
    CONSTRAINT uq_publicacion_numero UNIQUE (torneo_id, numero),
    CONSTRAINT ck_publicacion_numero CHECK (numero > 0),
    -- La primera versión no necesita motivo; las correcciones (2, 3…) sí.
    CONSTRAINT ck_publicacion_motivo CHECK (numero = 1 OR (motivo IS NOT NULL AND btrim(motivo) <> ''))
);

-- Foto desnormalizada de cada partido publicado: guarda los textos tal como se publicaron.
-- equipo_local_id y equipo_visitante_id NO son claves foráneas: sirven para filtrar por equipo, pero la foto
-- no depende de que el equipo siga existiendo ni de cómo se llame hoy.
-- fin se guarda (a diferencia de partido, D8): es el horario que se comunicó (D48).
CREATE TABLE partido_publicado (
    id                  BIGSERIAL    PRIMARY KEY,
    publicacion_id      BIGINT       NOT NULL REFERENCES publicacion (id) ON DELETE CASCADE,
    equipo_local_id     BIGINT       NOT NULL,
    equipo_visitante_id BIGINT       NOT NULL,
    equipo_local        VARCHAR(150) NOT NULL,
    equipo_visitante    VARCHAR(150) NOT NULL,
    categoria           VARCHAR(50)  NOT NULL,
    rama                VARCHAR(50)  NOT NULL,
    zona                VARCHAR(50)  NOT NULL,
    ronda               INTEGER      NOT NULL,
    sede                VARCHAR(100) NOT NULL,
    cancha              VARCHAR(100) NOT NULL,
    inicio              TIMESTAMP    NOT NULL,
    fin                 TIMESTAMP    NOT NULL,
    CONSTRAINT ck_partido_publicado_equipos CHECK (equipo_local_id <> equipo_visitante_id),
    CONSTRAINT ck_partido_publicado_ronda   CHECK (ronda > 0),
    CONSTRAINT ck_partido_publicado_horario CHECK (fin > inicio)
);

CREATE INDEX ix_partido_publicado_publicacion ON partido_publicado (publicacion_id);

-- =============================================================================
-- Pruebas del esquema (DATOS DE PRUEBA, no son del torneo real)
-- Ejecutar en una base de desarrollo que ya tenga aplicada la migración V1
-- (backend/src/main/resources/db/migration), por ejemplo después de arrancar el backend una vez.
-- Cada prueba intenta cargar algo inválido y muestra si la base lo rechazó.
-- Al final muestra una tabla con el resultado de cada prueba (sirve en cualquier editor web).
-- =============================================================================

CREATE TEMP TABLE resultado_prueba (n SERIAL, resultado TEXT);

-- Datos mínimos válidos
INSERT INTO organizador (nombre, email, password_hash) VALUES ('Organizador prueba', 'org@prueba.com', 'hash');
INSERT INTO torneo (organizador_id, nombre, turnos_cada_min, recambio_min, descanso_minimo_min)
VALUES (1, 'Torneo prueba 1', 15, 10, 90),
       (1, 'Torneo prueba 2', 15, 10, 90);
INSERT INTO categoria (torneo_id, nombre, minutos_netos, cantidad_cuartos, pausas_min, margen_detenciones_min, max_partidos_dia)
VALUES (1, 'U13', 40, 4, 4, 10, 3),
       (2, 'U9', 28, 4, 4, 10, 3);
INSERT INTO rama (torneo_id, nombre) VALUES (1, 'Masculino');
INSERT INTO club (torneo_id, nombre) VALUES (1, 'Club A');
INSERT INTO zona (torneo_id, categoria_id, rama_id, nombre) VALUES (1, 1, 1, 'Zona A'), (1, 1, 1, 'Zona B');
INSERT INTO equipo (torneo_id, club_id, categoria_id, rama_id, nombre, responsable_nombre, responsable_telefono, zona_id, orden_en_zona)
VALUES (1, 1, 1, 1, 'Equipo 1', 'Responsable', '000', 1, 1),
       (1, 1, 1, 1, 'Equipo 2', 'Responsable', '000', 1, 2);
INSERT INTO sede (torneo_id, nombre) VALUES (1, 'Sede torneo 1'), (2, 'Sede torneo 2');
INSERT INTO cancha (sede_id, nombre) VALUES (1, 'Cancha 1'), (2, 'Cancha del torneo 2');
INSERT INTO partido (zona_id, equipo_local_id, equipo_visitante_id, ronda, cancha_id, inicio)
VALUES (1, 1, 2, 1, 1, '2026-11-07 09:00');

-- -----------------------------------------------------------------------------
-- Pruebas: cada bloque espera que la base RECHACE el dato
-- -----------------------------------------------------------------------------
DO $$ BEGIN
  INSERT INTO partido (zona_id, equipo_local_id, equipo_visitante_id, ronda) VALUES (1, 1, 1, 2);
  INSERT INTO resultado_prueba (resultado) VALUES ('FALLA: aceptó un equipo jugando contra sí mismo');
EXCEPTION WHEN others THEN INSERT INTO resultado_prueba (resultado) VALUES ('OK: rechazó un equipo jugando contra sí mismo');
END $$;

DO $$ BEGIN
  INSERT INTO partido (zona_id, equipo_local_id, equipo_visitante_id, ronda, cancha_id) VALUES (1, 1, 2, 2, 1);
  INSERT INTO resultado_prueba (resultado) VALUES ('FALLA: aceptó un partido con cancha pero sin hora');
EXCEPTION WHEN others THEN INSERT INTO resultado_prueba (resultado) VALUES ('OK: rechazó un partido con cancha pero sin hora');
END $$;

DO $$ BEGIN
  INSERT INTO equipo (torneo_id, club_id, categoria_id, rama_id, nombre, responsable_nombre, responsable_telefono)
  VALUES (1, 1, 2, 1, 'Equipo 3', 'Responsable', '000');
  INSERT INTO resultado_prueba (resultado) VALUES ('FALLA: aceptó un equipo con categoría de otro torneo');
EXCEPTION WHEN others THEN INSERT INTO resultado_prueba (resultado) VALUES ('OK: rechazó un equipo con categoría de otro torneo');
END $$;

DO $$ BEGIN
  UPDATE equipo SET zona_id = 2 WHERE id = 1;
  INSERT INTO resultado_prueba (resultado) VALUES ('FALLA: dejó cambiar de zona a un equipo con partidos');
EXCEPTION WHEN others THEN INSERT INTO resultado_prueba (resultado) VALUES ('OK: no dejó cambiar de zona a un equipo con partidos');
END $$;

DO $$ BEGIN
  INSERT INTO torneo (organizador_id, nombre, turnos_cada_min, recambio_min, descanso_minimo_min, estado)
  VALUES (1, 'Torneo raro', 15, 10, 90, 'CERRADO');
  INSERT INTO resultado_prueba (resultado) VALUES ('FALLA: aceptó un estado que no existe');
EXCEPTION WHEN others THEN INSERT INTO resultado_prueba (resultado) VALUES ('OK: rechazó un estado que no existe');
END $$;

-- Caso conocido (punto 1 de la revisión): la base NO lo impide, lo controla el validador (R5)
DO $$ BEGIN
  INSERT INTO partido (zona_id, equipo_local_id, equipo_visitante_id, ronda, cancha_id, inicio)
  VALUES (1, 2, 1, 2, 2, '2026-11-07 11:00');
  INSERT INTO resultado_prueba (resultado) VALUES ('ESPERADO: aceptó una cancha de otro torneo (lo controla el ValidadorRestricciones, R5)');
  DELETE FROM partido WHERE cancha_id = 2;
EXCEPTION WHEN others THEN INSERT INTO resultado_prueba (resultado) VALUES ('Rechazó una cancha de otro torneo');
END $$;

SELECT resultado FROM resultado_prueba ORDER BY n;

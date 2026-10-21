-- =============================================================================
-- Datos de ejemplo: torneo del caso real (docs/caso-real.md), listo para el recorrido de la API.
--
-- Carga el torneo en BORRADOR con su configuración: franjas, 2 sedes × 4 canchas, categorías U9/U11/U13
-- con sus duraciones, ramas, las 13 zonas elegidas y los 55 equipos. NO sortea ni genera partidos:
-- eso se hace con la API (POST /sorteo, /fixture/generar, /fixture/programar, /publicar).
--
-- Del caso real: cantidades, parámetros, sedes ("Club A" y "Club B", anonimizadas) y zonas.
-- DATOS DE PRUEBA (no figuran en el caso real): las fechas, los nombres de los equipos, el club de los
-- equipos, el responsable y el teléfono, y el email del organizador.
--
-- Uso:
--   1. Arrancar el backend una vez (Flyway crea las tablas).
--   2. Registrar el organizador dueño del torneo: POST /api/auth/registro con el email
--      ejemplo@fixture.test y la contraseña que quieran (ver backend/recorrido.http).
--   3. Ejecutar este archivo en la base (psql o el editor SQL de Neon). Al final muestra el id del torneo.
--   4. Iniciar sesión con ese organizador (POST /api/auth/login) y seguir el recorrido.
-- Se puede ejecutar más de una vez: cada vez crea un torneo nuevo.
-- =============================================================================

-- El torneo queda a nombre del organizador ejemplo@fixture.test, que tiene que estar registrado (paso 2):
-- solo él lo ve y lo edita (D45). Este script no crea organizadores para no guardar contraseñas en el repo.
-- Si no está registrado, este INSERT falla con "null value in column organizador_id" y no se carga nada más.
INSERT INTO torneo (organizador_id, nombre, turnos_cada_min, recambio_min, descanso_minimo_min)
VALUES ((SELECT id FROM organizador WHERE email = 'ejemplo@fixture.test'),
        'Torneo formativo (caso real)', 15, 10, 90);

-- Sábado 9 a 20 y domingo 9 a 16 (fechas de ejemplo).
INSERT INTO franja_horaria (torneo_id, fecha, desde, hasta)
VALUES (currval(pg_get_serial_sequence('torneo', 'id')), DATE '2026-11-07', TIME '09:00', TIME '20:00'),
       (currval(pg_get_serial_sequence('torneo', 'id')), DATE '2026-11-08', TIME '09:00', TIME '16:00');

INSERT INTO sede (torneo_id, nombre)
VALUES (currval(pg_get_serial_sequence('torneo', 'id')), 'Club A'),
       (currval(pg_get_serial_sequence('torneo', 'id')), 'Club B');

-- 4 canchas por sede, en orden (el programador las recorre en el orden de carga, D37).
INSERT INTO cancha (sede_id, nombre)
SELECT s.id, 'Cancha ' || n
FROM sede s
CROSS JOIN generate_series(1, 4) AS n
WHERE s.torneo_id = currval(pg_get_serial_sequence('torneo', 'id'))
ORDER BY s.nombre, n;

-- Duraciones del caso real: juego neto, 4 cuartos, 1' + 2' + 1' de pausas, 10' de margen, tope 3 por día.
INSERT INTO categoria (torneo_id, nombre, minutos_netos, cantidad_cuartos, pausas_min, margen_detenciones_min, max_partidos_dia)
VALUES (currval(pg_get_serial_sequence('torneo', 'id')), 'U9',  28, 4, 4, 10, 3),
       (currval(pg_get_serial_sequence('torneo', 'id')), 'U11', 32, 4, 4, 10, 3),
       (currval(pg_get_serial_sequence('torneo', 'id')), 'U13', 40, 4, 4, 10, 3);

INSERT INTO rama (torneo_id, nombre)
VALUES (currval(pg_get_serial_sequence('torneo', 'id')), 'Masculino'),
       (currval(pg_get_serial_sequence('torneo', 'id')), 'Femenino');

-- El caso real no dice de qué club es cada equipo.
INSERT INTO club (torneo_id, nombre)
VALUES (currval(pg_get_serial_sequence('torneo', 'id')), 'Club de prueba');

-- Zonas elegidas (caso-real.md, sección 3): 13 zonas.
INSERT INTO zona (torneo_id, categoria_id, rama_id, nombre)
SELECT c.torneo_id, c.id, r.id, z.nombre
FROM (VALUES (1, 'U9',  'Masculino', 'Zona A'), (2, 'U9',  'Masculino', 'Zona B'),
             (3, 'U9',  'Femenino',  'Zona A'),
             (4, 'U11', 'Masculino', 'Zona A'), (5, 'U11', 'Masculino', 'Zona B'), (6, 'U11', 'Masculino', 'Zona C'),
             (7, 'U11', 'Femenino',  'Zona A'), (8, 'U11', 'Femenino',  'Zona B'),
             (9, 'U13', 'Masculino', 'Zona A'), (10, 'U13', 'Masculino', 'Zona B'),
             (11, 'U13', 'Femenino', 'Zona A'), (12, 'U13', 'Femenino', 'Zona B'), (13, 'U13', 'Femenino', 'Zona C'))
     AS z(orden, categoria, rama, nombre)
JOIN categoria c ON c.torneo_id = currval(pg_get_serial_sequence('torneo', 'id')) AND c.nombre = z.categoria
JOIN rama r ON r.torneo_id = c.torneo_id AND r.nombre = z.rama
ORDER BY z.orden;

-- 55 equipos (caso-real.md, "Categorías, ramas y equipos"). Nombres de prueba: "U9 Masculino 01", etc.
INSERT INTO equipo (torneo_id, club_id, categoria_id, rama_id, nombre, responsable_nombre, responsable_telefono)
SELECT c.torneo_id, cl.id, c.id, r.id,
       c.nombre || ' ' || r.nombre || ' ' || lpad(n::text, 2, '0'),
       'Responsable de prueba', '0000-0000'
FROM (VALUES (1, 'U9',  'Masculino', 10), (2, 'U9',  'Femenino', 5),
             (3, 'U11', 'Masculino', 12), (4, 'U11', 'Femenino', 9),
             (5, 'U13', 'Masculino', 8),  (6, 'U13', 'Femenino', 11))
     AS g(orden, categoria, rama, cantidad)
JOIN categoria c ON c.torneo_id = currval(pg_get_serial_sequence('torneo', 'id')) AND c.nombre = g.categoria
JOIN rama r ON r.torneo_id = c.torneo_id AND r.nombre = g.rama
JOIN club cl ON cl.torneo_id = c.torneo_id
CROSS JOIN LATERAL generate_series(1, g.cantidad) AS n
ORDER BY g.orden, n;

SELECT currval(pg_get_serial_sequence('torneo', 'id')) AS torneo_id;

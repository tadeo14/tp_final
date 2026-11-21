-- =============================================================================
-- Datos de prueba: torneo de demostración para la cuenta pública demo@fixture.test (D71, D72).
--
-- TODO ES FICTICIO: nombre del torneo, sedes, clubes, equipos, responsables, teléfonos y fecha.
-- No usa nada del caso real (docs/caso-real.md): ni sus clubes ni sus sedes.
--
-- Carga el torneo "[PRUEBA] Torneo demo" en BORRADOR, listo para el recorrido:
--   - 1 día (sábado 12/12/2026, de 9 a 18), 2 sedes ficticias con 2 canchas cada una.
--   - 2 categorías (U9 y U11) y 2 ramas.
--   - 4 clubes ficticios y 10 equipos: U9 Masculino 6 (2 zonas de 3) y U11 Femenino 4 (1 zona de 4).
--     Las zonas son las que propone "Proponer zonas" con máximo 5 (D69).
-- NO sortea ni genera partidos: eso se hace desde la app (Recorrido → Sortear, Generar, Programar…).
--
-- Uso (Supabase → SQL Editor, o psql):
--   - Primera vez: registrar la cuenta demo@fixture.test desde la app (README, "Cuenta de demostración")
--     y ejecutar este archivo.
--   - Reinicio de la cuenta demo (docs/deploy.md): 1) el DELETE de sus torneos, 2) este archivo.
-- Se puede ejecutar más de una vez: cada vez crea un torneo nuevo, sin tocar los existentes.
-- =============================================================================

-- El torneo queda a nombre de demo@fixture.test, que tiene que estar registrada. Si no lo está, este INSERT falla
-- con "null value in column organizador_id" y no se carga nada más.
INSERT INTO torneo (organizador_id, nombre, turnos_cada_min, recambio_min, descanso_minimo_min)
VALUES ((SELECT id FROM organizador WHERE email = 'demo@fixture.test'),
        '[PRUEBA] Torneo demo', 15, 10, 90);

INSERT INTO franja_horaria (torneo_id, fecha, desde, hasta)
VALUES (currval(pg_get_serial_sequence('torneo', 'id')), DATE '2026-12-12', TIME '09:00', TIME '18:00');

INSERT INTO sede (torneo_id, nombre, direccion)
VALUES (currval(pg_get_serial_sequence('torneo', 'id')), 'Polideportivo Demo Norte', 'Calle Ficticia 100'),
       (currval(pg_get_serial_sequence('torneo', 'id')), 'Gimnasio Demo Sur', 'Avenida Inventada 200');

-- 2 canchas por sede, en orden (el programador las recorre en el orden de carga, D37).
INSERT INTO cancha (sede_id, nombre)
SELECT s.id, 'Cancha ' || n
FROM sede s
CROSS JOIN generate_series(1, 2) AS n
WHERE s.torneo_id = currval(pg_get_serial_sequence('torneo', 'id'))
ORDER BY s.id, n;

-- Duraciones de ejemplo: juego neto, 4 cuartos, pausas, margen y tope de 3 partidos por día.
INSERT INTO categoria (torneo_id, nombre, minutos_netos, cantidad_cuartos, pausas_min, margen_detenciones_min, max_partidos_dia)
VALUES (currval(pg_get_serial_sequence('torneo', 'id')), 'U9',  28, 4, 4, 10, 3),
       (currval(pg_get_serial_sequence('torneo', 'id')), 'U11', 32, 4, 4, 10, 3);

INSERT INTO rama (torneo_id, nombre)
VALUES (currval(pg_get_serial_sequence('torneo', 'id')), 'Masculino'),
       (currval(pg_get_serial_sequence('torneo', 'id')), 'Femenino');

INSERT INTO club (torneo_id, nombre)
SELECT currval(pg_get_serial_sequence('torneo', 'id')), c.nombre
FROM (VALUES (1, 'Club Demo Norte'), (2, 'Club Demo Sur'), (3, 'Club Demo Este'), (4, 'Club Demo Oeste'))
     AS c(orden, nombre)
ORDER BY c.orden;

INSERT INTO zona (torneo_id, categoria_id, rama_id, nombre)
SELECT c.torneo_id, c.id, r.id, z.nombre
FROM (VALUES (1, 'U9', 'Masculino', 'Zona A'), (2, 'U9', 'Masculino', 'Zona B'),
             (3, 'U11', 'Femenino', 'Zona A'))
     AS z(orden, categoria, rama, nombre)
JOIN categoria c ON c.torneo_id = currval(pg_get_serial_sequence('torneo', 'id')) AND c.nombre = z.categoria
JOIN rama r ON r.torneo_id = c.torneo_id AND r.nombre = z.rama
ORDER BY z.orden;

-- 10 equipos ficticios. Datos personales de prueba (regla 7): nunca teléfonos reales en la cuenta demo.
INSERT INTO equipo (torneo_id, club_id, categoria_id, rama_id, nombre, responsable_nombre, responsable_telefono)
SELECT c.torneo_id, cl.id, c.id, r.id, e.nombre, 'Responsable de prueba', '0000-0000'
FROM (VALUES (1,  'U9',  'Masculino', 'Club Demo Norte', 'Norte U9 Azul'),
             (2,  'U9',  'Masculino', 'Club Demo Norte', 'Norte U9 Blanco'),
             (3,  'U9',  'Masculino', 'Club Demo Sur',   'Sur U9'),
             (4,  'U9',  'Masculino', 'Club Demo Este',  'Este U9 Rojo'),
             (5,  'U9',  'Masculino', 'Club Demo Este',  'Este U9 Negro'),
             (6,  'U9',  'Masculino', 'Club Demo Oeste', 'Oeste U9'),
             (7,  'U11', 'Femenino',  'Club Demo Norte', 'Norte U11'),
             (8,  'U11', 'Femenino',  'Club Demo Sur',   'Sur U11'),
             (9,  'U11', 'Femenino',  'Club Demo Este',  'Este U11'),
             (10, 'U11', 'Femenino',  'Club Demo Oeste', 'Oeste U11'))
     AS e(orden, categoria, rama, club, nombre)
JOIN categoria c ON c.torneo_id = currval(pg_get_serial_sequence('torneo', 'id')) AND c.nombre = e.categoria
JOIN rama r ON r.torneo_id = c.torneo_id AND r.nombre = e.rama
JOIN club cl ON cl.torneo_id = c.torneo_id AND cl.nombre = e.club
ORDER BY e.orden;

SELECT currval(pg_get_serial_sequence('torneo', 'id')) AS torneo_id;

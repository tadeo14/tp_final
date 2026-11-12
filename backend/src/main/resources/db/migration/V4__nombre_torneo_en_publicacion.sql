-- =============================================================================
-- Migración V4: el nombre del torneo pasa a la versión publicada (D70).
-- Cada publicación es una foto inmutable (D46): el público ve el nombre con el que se publicó, aunque después
-- se renombre el torneo. Hasta ahora la vista pública lo leía de torneo.nombre, que se puede editar (D60).
--
-- V2 y V3 ya están aplicadas y no se modifican: la columna se agrega acá.
-- NO MODIFICAR una vez aplicada: los cambios van en una migración nueva (V5__...).
-- =============================================================================

ALTER TABLE publicacion ADD COLUMN nombre_torneo VARCHAR(150);

-- BACKFILL: las versiones publicadas antes de esta migración no guardaron el nombre con el que se publicaron.
-- Se completan con el nombre ACTUAL del torneo, que es la mejor información disponible: si el torneo se renombró
-- después de publicar, esas versiones quedan con el nombre nuevo. Desde V4 cada publicación guarda el suyo.
UPDATE publicacion p
SET nombre_torneo = (SELECT t.nombre FROM torneo t WHERE t.id = p.torneo_id);

ALTER TABLE publicacion ALTER COLUMN nombre_torneo SET NOT NULL;

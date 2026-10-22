-- =============================================================================
-- Migración V3: el club de cada equipo vuelve a la foto publicada (D48).
-- Las familias buscan el fixture por club, y la vista pública lo mostraba antes de las versiones (V2).
--
-- V2 ya está aplicada y no se modifica: las columnas se agregan acá.
-- NO MODIFICAR una vez aplicada: los cambios van en una migración nueva (V4__...).
-- =============================================================================

ALTER TABLE partido_publicado ADD COLUMN club_local     VARCHAR(150);
ALTER TABLE partido_publicado ADD COLUMN club_visitante VARCHAR(150);

-- Fotos publicadas antes de esta migración: se completan con el club actual del equipo.
-- Es la mejor información disponible; si el equipo ya no existe, queda "(sin dato)".
UPDATE partido_publicado pp
SET club_local     = COALESCE((SELECT c.nombre FROM equipo e JOIN club c ON c.id = e.club_id
                               WHERE e.id = pp.equipo_local_id), '(sin dato)'),
    club_visitante = COALESCE((SELECT c.nombre FROM equipo e JOIN club c ON c.id = e.club_id
                               WHERE e.id = pp.equipo_visitante_id), '(sin dato)');

ALTER TABLE partido_publicado ALTER COLUMN club_local     SET NOT NULL;
ALTER TABLE partido_publicado ALTER COLUMN club_visitante SET NOT NULL;

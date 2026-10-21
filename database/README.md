# /database

## Esquema

El esquema de la base **no está en esta carpeta**: lo crean las migraciones de Flyway en
[`backend/src/main/resources/db/migration`](../backend/src/main/resources/db/migration).

- `V1__esquema_inicial.sql` crea las 11 tablas del modelo (ver [`docs/der.md`](../docs/der.md)).
- Flyway las aplica solo al arrancar el backend y anota cuáles ya corrió en la tabla `flyway_schema_history`.
- Una migración ya aplicada **no se modifica**: cualquier cambio va en una nueva (`V2__...`, `V3__...`).

Están dentro del backend porque tienen que viajar dentro del jar para que funcionen igual
en local y en el despliegue (decisión D21).

## Qué va acá

- `pruebas_schema.sql`: pruebas de integridad del esquema. Cargan datos inválidos y muestran si la base
  los rechaza. Se corren en una base de desarrollo con la migración V1 aplicada.
- `caso_real.sql`: datos de ejemplo del caso real (torneo en BORRADOR con 55 equipos y 13 zonas), para probar el
  recorrido de la API. Antes hay que registrar el organizador `ejemplo@fixture.test` por la API. Ver `backend/README.md`.

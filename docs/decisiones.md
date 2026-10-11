# Registro de decisiones

Cada decisión con su motivo, para poder defenderla en la oral.

| # | Fecha | Decisión | Motivo |
|---|---|---|---|
| D1 | 2026-10-10 | Separar el fixture en **generador de enfrentamientos** y **programador** | Pedido del tutor. Son dos problemas distintos y así se diseñan y prueban por separado |
| D2 | 2026-10-10 | El caso real (55 equipos, 2 sedes, 8 canchas) es el principal caso de prueba | Pedido del tutor: resolver un problema real y comprobable |
| D3 | 2026-10-10 | Formato P0: **zonas por sorteo + todos contra todos dentro de cada zona**, sin fase final | Con las reglas del caso real, todos contra todos no tiene solución (ver `caso-real.md`) |
| D4 | 2026-10-10 | Zonas impares: una zona queda con un equipo menos; equipos impares en una zona tienen "libre" | Práctica habitual en torneos formativos |
| D5 | 2026-10-10 | El resultado del sorteo se guarda y no se rehace solo | El fixture tiene que ser reproducible y auditable |
| D6 | 2026-10-10 | Restricciones obligatorias R1–R8; un fixture que viole alguna es inválido | Pedido del tutor. Ver `restricciones.md` |
| D7 | 2026-10-10 | Duración programada = juego neto + pausas + margen por detenciones + recambio, redondeada a "turnos cada" | El inicio solo no alcanza: hay que reservar el tiempo real de cancha |
| D8 | 2026-10-10 | La duración programada se calcula, no se guarda | Evita datos derivados que pueden quedar desactualizados (normalización) |
| D9 | 2026-10-10 | "Turnos cada" 15 min, descanso 90 min, máximo 3 partidos por día por equipo (caso real; configurables) | Valores definidos por la organización. Las guías de NBA/USA Basketball sugieren máximo 2 por día para 9–14 años |
| D10 | 2026-10-10 | Si no hay solución, el sistema lo informa y no relaja restricciones | Pedido del tutor |
| D11 | 2026-10-10 | Estados BORRADOR y PUBLICADO; la vista pública solo muestra lo publicado | Pedido del tutor |
| D12 | 2026-10-10 | Comidas, fase final y edición manual quedan fuera de P0 | Priorizar que el generador funcione (pedido del tutor) |
| D13 | 2026-10-10 | Algoritmo en clases de dominio sin Spring ni JPA | Se testea con JUnit sin base de datos |
| D14 | 2026-10-10 | La base garantiza integridad (FKs, únicos, CHECK); R1–R8 los valida solo el `ValidadorRestricciones` | Una sola fuente de verdad para las reglas del fixture. Además R1–R2 necesitarían guardar el fin del partido, que es derivado (D8) |
| D15 | 2026-10-10 | El partido guarda solo `cancha_id` e `inicio`; el fin es `inicio + bloque` | Consecuencia de D7 y D8: si cambia un parámetro de la categoría, el fin se recalcula solo |
| D16 | 2026-10-10 | El sorteo se guarda en `equipo.zona_id` + `equipo.orden_en_zona` | Hace reproducible el fixture (D5). El orden fija quién se cruza con quién en cada ronda |
| D17 | 2026-10-10 | FKs compuestas para que equipo, zona y partido no mezclen torneos, categorías ni ramas | Errores de carga que la base puede impedir sin lógica en el backend |
| D18 | 2026-10-10 | Los "libres" y la cantidad de zonas no se guardan: se deducen | Evita datos derivados (normalización) |
| D19 | 2026-10-10 | Categorías, ramas y clubes son tablas por torneo, no enums | Regla 1 de `CLAUDE.md`: nada de valores fijos en el código |
| D20 | 2026-10-10 | Con el torneo PUBLICADO no se sortea, genera ni programa de nuevo; el servicio lo rechaza | El fixture publicado es el que ven los equipos: regenerarlo cambiaría horarios sin aviso. Solo en BORRADOR se puede generar y regenerar (`restricciones.md`, sección 6) |
| D21 | 2026-10-11 | El esquema se crea con migraciones Flyway en `backend/src/main/resources/db/migration`; `database/schema.sql` pasa a ser `V1__esquema_inicial.sql` sin el DROP | Las migraciones tienen que viajar dentro del jar para funcionar igual en local y desplegado. Una sola copia del esquema evita que se desactualicen dos versiones. Flyway nunca borra datos: solo aplica las migraciones nuevas |
| D22 | 2026-10-11 | `ddl-auto=validate`: Hibernate no crea tablas, solo verifica que las entidades coincidan con la base | El esquema lo manejan solo las migraciones. Si una entidad no coincide con su tabla, la app no arranca y el error aparece enseguida |
| D23 | 2026-10-11 | Credenciales de la base solo por variables de entorno (`DB_URL`, `DB_USER`, `DB_PASSWORD`) | Nunca se sube una contraseña a GitHub. El mismo código corre contra la base local, la de tests o Neon cambiando solo las variables |
| D24 | 2026-10-11 | Desarrollo y tests con PostgreSQL local en el entorno de Claude Code | El entorno no permite conectarse a Neon (la red bloquea el host). La base online se elige en el paso 9 (despliegue) |

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

# DER — Modelo de datos (P0)

> DDL completo en la migración Flyway [`V1__esquema_inicial.sql`](../backend/src/main/resources/db/migration/V1__esquema_inicial.sql).
> Cubre solo el alcance P0 de `CLAUDE.md`: no hay comidas, resultados, fase final ni jugadores.

## Diagrama

```mermaid
erDiagram
    ORGANIZADOR ||--o{ TORNEO : organiza
    TORNEO ||--o{ FRANJA_HORARIA : "habilita"
    TORNEO ||--o{ SEDE : tiene
    SEDE ||--o{ CANCHA : tiene
    TORNEO ||--o{ CATEGORIA : define
    TORNEO ||--o{ RAMA : define
    TORNEO ||--o{ CLUB : inscribe
    CATEGORIA ||--o{ ZONA : "se divide en"
    RAMA ||--o{ ZONA : "se divide en"
    CLUB ||--o{ EQUIPO : presenta
    CATEGORIA ||--o{ EQUIPO : agrupa
    RAMA ||--o{ EQUIPO : agrupa
    ZONA |o--o{ EQUIPO : "sorteo asigna"
    ZONA ||--o{ PARTIDO : contiene
    EQUIPO ||--o{ PARTIDO : "juega de local"
    EQUIPO ||--o{ PARTIDO : "juega de visitante"
    CANCHA |o--o{ PARTIDO : "se programa en"

    ORGANIZADOR {
        bigint id PK
        varchar nombre
        varchar email UK
        varchar password_hash
    }
    TORNEO {
        bigint id PK
        bigint organizador_id FK
        varchar nombre
        int turnos_cada_min
        int recambio_min
        int descanso_minimo_min
        varchar estado "BORRADOR | PUBLICADO"
    }
    FRANJA_HORARIA {
        bigint id PK
        bigint torneo_id FK
        date fecha
        time desde
        time hasta
    }
    SEDE {
        bigint id PK
        bigint torneo_id FK
        varchar nombre
        varchar direccion
    }
    CANCHA {
        bigint id PK
        bigint sede_id FK
        varchar nombre
        boolean habilitada
    }
    CATEGORIA {
        bigint id PK
        bigint torneo_id FK
        varchar nombre "U9, U11, U13"
        int minutos_netos
        int cantidad_cuartos "informativo, no entra en el cálculo"
        int pausas_min
        int margen_detenciones_min
        int max_partidos_dia
    }
    RAMA {
        bigint id PK
        bigint torneo_id FK
        varchar nombre "Masculino, Femenino"
    }
    ZONA {
        bigint id PK
        bigint torneo_id FK
        bigint categoria_id FK
        bigint rama_id FK
        varchar nombre "Zona A"
    }
    CLUB {
        bigint id PK
        bigint torneo_id FK
        varchar nombre
    }
    EQUIPO {
        bigint id PK
        bigint torneo_id FK
        bigint club_id FK
        bigint categoria_id FK
        bigint rama_id FK
        varchar nombre
        varchar responsable_nombre
        varchar responsable_telefono
        bigint zona_id FK "NULL hasta el sorteo"
        int orden_en_zona "NULL hasta el sorteo"
    }
    PARTIDO {
        bigint id PK
        bigint zona_id FK
        bigint equipo_local_id FK
        bigint equipo_visitante_id FK
        int ronda
        bigint cancha_id FK "NULL hasta programar"
        timestamp inicio "NULL hasta programar"
    }
```

## Entidades

| Entidad | Qué representa | Notas |
|---|---|---|
| `organizador` | Usuario que crea y administra torneos | Único usuario con login. El público no se registra |
| `torneo` | Un torneo con sus parámetros generales | `turnos_cada_min`, `recambio_min`, `descanso_minimo_min`, `estado` |
| `franja_horaria` | Ventana de juego de un día (R4) | Los días del torneo salen de acá; no se guardan fechas de inicio/fin |
| `sede`, `cancha` | Dónde se juega (R5) | `habilitada` permite sacar una cancha sin borrarla |
| `categoria` | U9, U11, U13… con los parámetros de duración y tope diario (R6) | Los parámetros que en `restricciones.md` son "nivel categoría". `cantidad_cuartos` es informativo (ver abajo) |
| `rama` | Masculino, Femenino… | Configurable, no es un enum fijo |
| `zona` | Grupo de una categoría y rama | La cantidad de zonas es la cantidad de filas, no un campo aparte |
| `club` | Club que inscribe equipos | Por torneo. Puede coincidir con una sede, pero son conceptos distintos |
| `equipo` | Equipo inscripto | Solo responsable y teléfono como datos personales. `zona_id` + `orden_en_zona` = resultado del sorteo |
| `partido` | Enfrentamiento del fixture | Lo crea el generador (zona, equipos, ronda) y lo completa el programador (cancha, inicio). La base no impide asignar una cancha de otro torneo: eso lo controla R5 |

## Cálculos que no se guardan (D8)

| Dato | Cómo se obtiene |
|---|---|
| Duración programada | `minutos_netos + pausas_min + margen_detenciones_min` (categoría) `+ recambio_min` (torneo) |
| Bloque | Duración programada redondeada hacia arriba al múltiplo de `turnos_cada_min` |
| Fin del partido | `inicio + bloque` |
| Categoría y rama del partido | Las de su zona |
| Equipo "libre" en una ronda | El equipo de la zona que no figura en ningún partido de esa ronda |
| Días del torneo | Fechas distintas de `franja_horaria` |

**`cantidad_cuartos` no participa del cálculo.** `minutos_netos` ya es el total de juego (ej. U9: 4 × 7 = 28) y
`pausas_min` ya suma todas las pausas (1' + 2' + 1' = 4). Se guarda porque `restricciones.md` lo lista como
parámetro de la categoría y sirve para mostrar el formato del partido (4 cuartos de 7 minutos).
No es un dato derivado: no se puede obtener de las otras columnas.

## Qué garantiza la base y qué garantiza el backend

La base asegura que los datos sean **coherentes entre sí**; las reglas del fixture R1–R8 las verifica
**solo** el `ValidadorRestricciones` (regla 3 de `CLAUDE.md`), para no duplicar lógica.

| Garantía | Dónde | Cómo |
|---|---|---|
| Categoría, rama y club de un equipo son del mismo torneo | Base | FKs compuestas `(id, torneo_id)` |
| Un equipo solo puede caer en una zona de su categoría y rama | Base | FK `(zona_id, categoria_id, rama_id)` → `zona` |
| Los dos equipos de un partido son de la zona del partido | Base | FK `(equipo_id, zona_id)` → `equipo` |
| Un equipo no juega contra sí mismo | Base | `CHECK (equipo_local_id <> equipo_visitante_id)` |
| Un equipo con partidos no se puede cambiar de zona (D5) | Base | Mismas FKs: hay que borrar los partidos antes |
| Un partido está sin programar o tiene cancha **e** inicio | Base | `CHECK ((cancha_id IS NULL) = (inicio IS NULL))` |
| Solo existen los estados BORRADOR y PUBLICADO | Base | `CHECK` |
| Parámetros positivos / no negativos | Base | `CHECK` |
| La cancha de un partido pertenece al torneo del partido y está habilitada | Backend | R5 en `ValidadorRestricciones` |
| R1–R8 (superposiciones, descanso, franjas, tope, turnos, completitud) | Backend | `ValidadorRestricciones` |
| No se sortea, genera ni programa un torneo PUBLICADO (D20) | Backend | Servicio, antes de llamar al generador o al programador |

R1 y R2 no se pueden expresar en la base sin guardar el fin del partido, que es un dato derivado (D8).

R5 tampoco se controla en la base: `partido.cancha_id` apunta a cualquier `cancha`, sin verificar que su sede sea
del torneo de la zona del partido. Hacerlo con FKs obligaría a copiar `torneo_id` en `cancha` y `partido`;
como R5 ya tiene que estar en el validador (también verifica `habilitada`), se deja ahí para no duplicar la regla.

# Restricciones del fixture

> Todas las cifras son **configurables por torneo o por categoría**. Ningún valor queda fijo en el código.
> Los valores de ejemplo salen de [caso-real.md](caso-real.md).

## 1. Dos problemas separados

| Etapa | Pregunta que responde | Entrada | Salida |
|---|---|---|---|
| **Generador de enfrentamientos** | ¿Quién juega contra quién? | Equipos de cada zona | Lista de partidos sin cancha ni horario |
| **Programador** | ¿En qué cancha y a qué hora? | Partidos + canchas + franjas + reglas | Cada partido con cancha, inicio y fin |

Las zonas se arman antes, por sorteo. El resultado del sorteo **se guarda** y no se rehace solo, para que el fixture sea reproducible y auditable.

## 2. Parámetros

| Parámetro | Nivel | Ejemplo (caso real) |
|---|---|---|
| Días y franjas horarias (desde / hasta) | Torneo | Sáb 9–20, Dom 9–16 |
| Sedes y canchas habilitadas | Torneo | 2 sedes × 4 canchas |
| Turnos cada | Torneo | 15 min |
| Recambio | Torneo | 10 min |
| Descanso mínimo | Torneo | 90 min |
| Minutos netos de juego | Categoría | U9: 28, U11: 32, U13: 40 |
| Cantidad de cuartos | Categoría | 4 |
| Pausas entre cuartos y entretiempo | Categoría | 1' + 2' + 1' |
| Margen por detenciones | Categoría | 10 min |
| Máximo de partidos por día por equipo | Categoría | 3 |
| Cantidad de zonas | Categoría y rama | Ej.: U11 masc = 3 |

## 3. Cálculos

```
duración programada = minutos netos + pausas + margen por detenciones + recambio
bloque              = duración programada redondeada hacia arriba al múltiplo de "turnos cada"
fin programado      = inicio + bloque
```

Ejemplo U13: 40 + 4 + 10 + 10 = 64 min → bloque de 75 min. Un partido que empieza a las 10:00 termina (programado) a las 11:15.

La duración programada **se calcula, no se guarda**, para que no quede desactualizada si cambia alguno de sus componentes.

## 4. Restricciones obligatorias

Si un fixture viola **cualquiera** de estas reglas, es **inválido**: no se guarda ni se publica.

| # | Restricción | Condición |
|---|---|---|
| R1 | Cancha sin superposición | Dos partidos en la misma cancha no se pisan: `inicio_B ≥ fin_A` |
| R2 | Equipo sin superposición | Un equipo no tiene dos partidos al mismo tiempo |
| R3 | Descanso mínimo | Para partidos consecutivos de un mismo equipo: `inicio_siguiente ≥ fin_anterior + descanso` |
| R4 | Dentro de franja | El partido empieza y termina dentro de una franja habilitada del día: `inicio ≥ desde` y `fin ≤ hasta` |
| R5 | Cancha habilitada | El partido se asigna a una cancha habilitada del torneo |
| R6 | Tope diario | Un equipo no juega más del máximo de partidos por día |
| R7 | Inicio en turno | El inicio coincide con un turno válido (9:00, 9:15, 9:30… si es cada 15) |
| R8 | Completitud | Todos los partidos generados quedan programados |

Ejemplo de R3 (U9, bloque 60, descanso 90): juega 9:00–10:00 → no puede empezar otro partido antes de las 11:30.

## 5. Cuando no hay solución

Si con la configuración actual no se pueden programar todos los partidos respetando R1–R8, el sistema:

- **no** relaja restricciones,
- **no** deja partidos sin programar,
- **no** crea superposiciones,

y responde con uno de estos dos mensajes, **indicando en ambos qué zona o equipo no se pudo ubicar**, para que el organizador ajuste (más zonas, más franjas, menos margen, etc.):

| Situación | Mensaje | Qué se indica |
|---|---|---|
| Un **chequeo por conteo**, hecho antes de programar, **demuestra** que es imposible | **"No existe una programación válida con la configuración actual"** | La zona y el equipo que necesitan más partidos de los que pueden jugar, o las horas de cancha que faltan |
| El conteo no demuestra que sea imposible, pero el programador **no encuentra** lugar para algún partido | **"No se encontró una programación válida con la configuración actual"** | El partido que no se pudo ubicar, con su zona y su ronda |

Por qué dos mensajes (D34): el programador es greedy, no vuelve atrás. Si no encuentra lugar, eso no prueba que no exista
ninguna solución. Solo se afirma "no existe" cuando el conteo lo demuestra.

### Chequeo por conteo (D35)

Se hace antes de programar. Si alguna de estas condiciones falla, seguro que no hay solución:

1. **Partidos por equipo.** Cada equipo tiene que poder jugar todos sus partidos. En una franja de L minutos, con bloque B
   y descanso D, un equipo juega como máximo `⌊(L + D) / (B + D)⌋` partidos. Por día, como máximo eso sumado en las franjas
   del día, y nunca más que el tope diario. El total de los días tiene que alcanzar.
   Ejemplo T10: un equipo de una zona de 10 necesita 9 partidos y puede jugar como máximo 3 × 2 días = 6.
2. **Horas de cancha.** La suma de los bloques de todos los partidos no puede superar los minutos de cancha disponibles
   (minutos de cada franja × canchas habilitadas).

## 6. Estados y edición

| Estado | Quién lo ve | Qué se puede hacer |
|---|---|---|
| BORRADOR | Solo el organizador | Generar, regenerar, revisar |
| PUBLICADO | Todos (vista pública) | Consultar por equipo, cancha u horario |

Si en el futuro se permite mover partidos a mano, el backend vuelve a validar **las mismas reglas R1–R8** con el mismo validador del generador.

## 7. Batería de pruebas mínima

Objetivo: poder afirmar con tests que **el generador nunca publica un fixture que viole una restricción obligatoria**.

| # | Caso | Resultado esperado |
|---|---|---|
| T1 | 4 equipos, todos contra todos | 6 partidos, cada equipo juega 3 |
| T2 | 5 equipos (impar) | 10 partidos, cada equipo juega 4 y tiene 1 fecha libre |
| T3 | Dos categorías compartiendo canchas | Ninguna cancha con partidos superpuestos |
| T4 | Conflicto de cancha (fixture armado a mano) | El validador lo rechaza (R1) |
| T5 | Conflicto de equipo (fixture armado a mano) | El validador lo rechaza (R2) |
| T6 | Descanso exactamente en el mínimo | Válido (R3 usa `≥`) |
| T7 | Descanso un turno por debajo del mínimo | Inválido (R3) |
| T8 | Partido que terminaría después del cierre de franja | Inválido (R4) |
| T9 | Equipo con 4 partidos en un día y tope 3 | Inválido (R6) |
| T10 | Configuración sin solución (caso real con todos contra todos) | Mensaje "No existe una programación válida…" |
| T11 | Caso real con zonas | Fixture completo y válido |

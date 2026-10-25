# Algoritmo: sorteo de zonas y enfrentamientos

> Primera mitad del fixture: **quién juega contra quién**. Cancha y horario los asigna después el Programador.
> Código: `backend/src/main/java/ar/edu/utn/fixture/dominio/` — `PropuestaZonas`, `SorteoZonas` y `GeneradorEnfrentamientos`.
> Son clases de dominio puras: no usan Spring ni la base de datos (regla 4 de `CLAUDE.md`).

## 0. Propuesta de zonas (`PropuestaZonas`, D69)

Antes del sorteo hay que definir **cuántas zonas** tiene cada categoría y rama. El botón "Proponer zonas" las
calcula con una regla simple; el organizador la revisa y después puede editar o eliminar zonas.

Entrada: la cantidad de equipos `n` de cada categoría y rama y el **máximo de equipos por zona** `m`. El máximo no
está fijo en el código (regla 1): viene en el pedido; la pantalla precarga 5, que reproduce el caso real.

1. Cantidad de zonas: **ceil(n / m)**.
2. Los equipos se reparten **lo más parejo posible**: las primeras zonas quedan con uno más, igual que en el sorteo.
3. Nombres "Zona A", "Zona B"…
4. Con `n = 1` no se propone zona y se avisa (una zona necesita al menos 2 equipos). Con `n = 0` el grupo se omite.
5. `m` tiene que ser al menos 3: con `m = 2` y una cantidad impar de equipos quedaría una zona de 1.
   Con `m ≥ 3`, ceil(n / m) ≤ n / 2 para todo `n ≥ 2`, así que ninguna zona queda con menos de 2.

Caso real con `m = 5` (las mismas 13 zonas de `caso-real.md`, sección 3):

| Grupo | Equipos | ceil(n / 5) | Zonas |
|---|---|---|---|
| U9 masc | 10 | 2 | 5 + 5 |
| U9 fem | 5 | 1 | 5 |
| U11 masc | 12 | 3 | 4 + 4 + 4 |
| U11 fem | 9 | 2 | 5 + 4 |
| U13 masc | 8 | 2 | 4 + 4 |
| U13 fem | 11 | 3 | 4 + 4 + 3 |

La propuesta es solo la **estructura**: no mueve equipos. Qué equipo va a cada zona lo decide el sorteo (sección 1).
Si un grupo ya tiene zonas, reemplazarlas pide confirmación y deshace el sorteo (D66).

## 1. Sorteo de zonas (`SorteoZonas`)

Entrada: los equipos de una categoría y rama, y la cantidad de zonas.

1. Se **mezclan** los equipos al azar.
2. Se **reparten como cartas**: el 1.º a la zona 1, el 2.º a la zona 2… y al llegar a la última zona se vuelve a la primera.

Ejemplo: 11 equipos en 3 zonas (U13 femenino del caso real):

```
mezclados:  e1  e2  e3  e4  e5  e6  e7  e8  e9  e10  e11
            │   │   │   │   │   │   │   │   │   │    │
zona:       1   2   3   1   2   3   1   2   3   1    2

Zona 1: e1 e4 e7 e10   (4)
Zona 2: e2 e5 e8 e11   (4)
Zona 3: e3 e6 e9       (3)  ← la última queda con uno menos (D4)
```

- Las zonas difieren **a lo sumo en un equipo**.
- El orden de cada equipo dentro de su zona es su `orden_en_zona`, que se guarda en la base junto con la zona (D5, D16).
- Se rechaza una cantidad de zonas que deje alguna zona con menos de 2 equipos.

## 2. Todos contra todos: método del círculo (`GeneradorEnfrentamientos`)

Con **n** equipos en la zona (n par):

- hay **n − 1 rondas**, y en cada ronda juegan todos: **n / 2 partidos**;
- en total, **n·(n − 1) / 2 partidos**, y cada par de equipos se enfrenta **una sola vez**.

### Cómo funciona

Se ponen los equipos en dos filas enfrentadas. Juegan los que quedan **uno frente al otro**.
Después de cada ronda, el **primero queda fijo** y los demás **giran una posición** (el último pasa a ser el segundo).

Ejemplo con 4 equipos (T1). `A` queda fijo:

```
Ronda 1             Ronda 2             Ronda 3

 A    B              A    D              A    C
 │    │              │    │              │    │
 D    C              C    B              B    D

A–D  B–C            A–C  D–B            A–B  C–D
```

Cómo se pasa de una ronda a la otra (mirando la lista `[A, B, C, D]`):

```
Ronda 1: [A | B  C  D]     enfrenta 1.º con 4.º y 2.º con 3.º  →  A–D, B–C
               ↻ el último (D) pasa al 2.º lugar
Ronda 2: [A | D  B  C]                                        →  A–C, D–B
               ↻ el último (C) pasa al 2.º lugar
Ronda 3: [A | C  D  B]                                        →  A–B, C–D
```

Resultado: 6 partidos y cada equipo juega 3 — exactamente lo que pide **T1**.

### Zonas impares: la fecha libre

Si la zona tiene un número impar de equipos se agrega un **lugar vacío**. El equipo al que le toca enfrentarse
con el lugar vacío **no juega esa ronda**: tiene fecha libre.

Ejemplo con 5 equipos (T2). Se agrega `—` y queda una lista de 6:

```
Ronda 1: [A | B  C  D  E  —]   →  B–E, C–D        libre: A
Ronda 2: [A | —  B  C  D  E]   →  E–A, B–C        libre: D
Ronda 3: [A | E  —  B  C  D]   →  A–D, E–C        libre: B
Ronda 4: [A | D  E  —  B  C]   →  C–A, D–B        libre: E
Ronda 5: [A | C  D  E  —  B]   →  A–B, D–E        libre: C
```

Resultado: 10 partidos, cada equipo juega 4 y tiene **una** fecha libre — lo que pide **T2**.

El libre **no se guarda** en la base (D18): se deduce viendo qué equipo de la zona no aparece en ningún partido de esa ronda.

### Local y visitante

El equipo fijo jugaría siempre "de local". Para repartirlo, en las rondas pares se invierte su partido.
En el ejemplo de 5 equipos, `A` es visitante en las rondas 2 y 4 y local en la 3 y la 5.
En un torneo formativo con sedes neutrales esto es solo informativo, pero evita un fixture desparejo a la vista.

### ¿Por qué funciona?

Como un equipo queda fijo y los demás giran, en cada ronda cada equipo tiene enfrente a un rival distinto.
Después de n − 1 giros, cada uno pasó por todas las posiciones y quedó frente a cada rival exactamente una vez.

## 3. Comprobación con el caso real

Con las zonas elegidas en [`caso-real.md`](caso-real.md) (sección 3), el generador produce **91 partidos**,
igual al total de la tabla. Está cubierto por un test (`casoReal_conLasZonasElegidas_generaLos91Partidos`).

| Tamaño de zona | Partidos (n·(n − 1) / 2) | Rondas |
|---|---|---|
| 3 | 3 | 3 (con libre) |
| 4 | 6 | 3 |
| 5 | 10 | 5 (con libre) |

## 4. Tests

| Test | Qué comprueba |
|---|---|
| T1 — `t1_cuatroEquipos_seisPartidos_cadaEquipoJuegaTres` | 6 partidos, cada equipo juega 3, cada par una vez, nadie juega dos veces en una ronda |
| T2 — `t2_cincoEquipos_diezPartidos_cadaEquipoJuegaCuatroYTieneUnaFechaLibre` | 10 partidos, cada equipo juega 4, un libre por ronda y una fecha libre por equipo |
| Sorteo: 9 → 5 + 4, 11 → 4 + 4 + 3, 12 → 4 + 4 + 4 | Tamaños de zona del caso real |
| Sorteo: misma semilla, mismo resultado | El sorteo se puede reproducir en los tests |
| Validaciones | Zonas con menos de 2 equipos, equipos repetidos, 0 zonas |
| Propuesta: caso real con máximo 5 | Las 13 zonas del caso real con los mismos tamaños (`PropuestaZonasTest`) |
| Propuesta: 11 con máximo 5 → 4 + 4 + 3; n = 1 avisa; n = 0 se omite; máximo ≥ 3 | Regla de la sección 0 |

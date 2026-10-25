# Algoritmo: programador de cancha y horario

> Segunda mitad del fixture: **en qué cancha y a qué hora** se juega cada partido.
> Código: `backend/src/main/java/ar/edu/utn/fixture/dominio/Programador.java`. Clase de dominio pura (regla 4).

El programador es **greedy**: a cada partido le da el **primer lugar válido** que encuentra y **no vuelve atrás**.
Es lo mismo que hace una persona armando la grilla a mano, pero sin cansarse.

## Paso 1 — Chequeo por conteo (D35)

Antes de programar, se hacen dos cuentas. Si alguna falla, **seguro que no hay solución** y el sistema responde
**"No existe una programación válida con la configuración actual"**, indicando la zona y el equipo.

1. **¿Cada equipo puede jugar todos sus partidos?** En una franja de L minutos, con bloque B y descanso D,
   un equipo juega como máximo `⌊(L + D) / (B + D)⌋` partidos (cada partido ocupa B y necesita D de descanso
   antes del siguiente). Por día, nunca más que el tope.

   Caso real con U9 (B = 60, D = 90): sábado 9–20 → `⌊(660 + 90) / 150⌋ = 5`, tope 3 → **3**;
   domingo 9–16 → `⌊(420 + 90) / 150⌋ = 3` → **3**. En total, **6 partidos** como máximo.

2. **¿Alcanzan las horas de cancha?** La suma de los bloques de todos los partidos no puede superar
   los minutos de franja × canchas habilitadas.

**T10** (caso real con todos contra todos): un equipo de U9 masculino (zona de 10) necesita 9 partidos y puede jugar 6.
Además se necesitan 261 h de cancha y hay 144 h. Resultado: "No existe…", con las 5 zonas que no entran
(todas menos U9 femenino, como en `caso-real.md`).

## Paso 2 — Ordenar los partidos (D33)

1. **Por ronda**: primero las rondas 1 de todas las zonas, después las rondas 2, etc.
2. **Dentro de la ronda, los más difíciles primero**: bloque más largo (U13, 75 min) y zona más grande.

Es solo el orden en que se procesan: **no es una regla R1–R8** y el validador no lo controla.
Si un partido de la ronda 2 encuentra un lugar válido antes que uno de la ronda 1, queda antes.

## Paso 3 — Ubicar cada partido en el primer casillero que sirva (D36)

Un **casillero** es un horario de turno en una cancha habilitada. Se recorren de más temprano a más tarde y,
para cada horario, las canchas en orden:

```
           9:00   9:15   9:30   9:45  10:00  ...  19:45
Cancha 1   [ ]    [ ]    [ ]    [ ]   [ ]          [ ]
Cancha 2   [ ]    [ ]    [ ]    [ ]   [ ]          [ ]
```

- Solo se ofrecen horarios de turno (**R7**), en canchas habilitadas (**R5**), donde el bloque entra en la franja (**R4**).
- Para el resto de las reglas (**R1, R2, R3, R6**) el programador **le pregunta al `ValidadorRestricciones`**:
  le pasa el candidato junto con los partidos ya ubicados que pueden chocar con él (misma cancha o algún equipo en común).
  Así la lógica de las reglas está en un solo lugar (regla 3).

Si ningún casillero sirve, el programador se detiene y responde
**"No se encontró una programación válida con la configuración actual"**, indicando el partido, su zona y su ronda.
No dice "no existe" porque el greedy no vuelve atrás: que no encuentre lugar no prueba que no haya solución (D34).

### Ejemplo

Zona U9 de 4 equipos (A, B, C, D), 2 canchas, bloque 60, descanso 90:

```
Ronda 1:  A–D → Cancha 1  9:00   primer casillero libre
          B–C → Cancha 2  9:00   la Cancha 1 está ocupada hasta 10:00
Ronda 2:  C–A → Cancha 1 11:30   A y C terminaron 10:00 → descanso hasta 11:30
          D–B → Cancha 2 11:30
Ronda 3:  A–B → Cancha 1 14:00
          C–D → Cancha 2 14:00
```

## Paso 4 — Control final

Con todos los partidos ubicados, el fixture completo pasa por el `ValidadorRestricciones`.
Solo se devuelve si es válido. Si no lo fuera, es un error de programación y se lanza una excepción
(no puede pasar, porque cada partido se ubicó solo donde el validador lo aceptó).

## Resultado con el caso real (T11)

Con las 13 zonas de `caso-real.md` (91 partidos, 8 canchas):

| | Partidos | Último partido termina |
|---|---|---|
| Sábado | 75 | 19:45 |
| Domingo | 16 | 12:30 |

El fixture cumple R1–R8 y se calcula en alrededor de un segundo.

## Limitación conocida: los partidos no se reparten entre los días

El greedy toma siempre el **primer** horario válido, así que llena el sábado antes de pasar al domingo.
El fixture es válido (cumple R1–R8), pero con el caso real queda así:

| Equipos | Cantidad |
|---|---|
| Juegan **solo el sábado** | **35 de 55** (todos los de U11 masculino, U13 masculino y U13 femenino, y 4 de U11 femenino) |
| Juegan los dos días | 20 (todos los de U9 y 5 de U11 femenino) |
| Juegan solo el domingo | 0 |

Muchos equipos terminan el sábado y el domingo queda casi vacío (16 partidos, el último termina 12:30).

**Consultado con el organizador real (D64):** no tiene problema con cómo queda; para clubes y familias puede ser
mejor jugar un solo día y evitar un segundo traslado. No se cambia el programador. Si más adelante se pidiera
repartir, entraría como **preferencia de optimización**, no como una restricción obligatoria nueva (R1–R8 no
cambian). Ver [`hoja-de-ruta.md`](hoja-de-ruta.md), "Consultado con el organizador real".

## Tests

| Test | Qué comprueba |
|---|---|
| **T3** — dos categorías en una sola cancha | U9 y U13 comparten la cancha sin superponerse |
| **T10** — caso real, todos contra todos | "No existe…", con las zonas y el equipo que no llegan, y las horas que faltan |
| **T11** — caso real con zonas | 91 partidos programados y válidos |
| "No se encontró" | Zona de 3 en una franja de 5 h: el conteo no lo detecta, pero el tercer partido no entra |
| Orden y reproducibilidad | El resultado sigue el orden recibido y el mismo pedido da el mismo fixture |

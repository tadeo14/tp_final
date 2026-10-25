# Caso real de referencia

> Torneo formativo real. Las sedes son dos clubes reales: **Atlético María Juana** y **Litoral María Juana** (D63).
> El organizador está de acuerdo con que figuren sus clubes.
> Los equipos y su reparto entre los clubes siguen siendo **datos de prueba**: nombres reales de clubes con
> participación ficticia (sección 4). Las dos sedes no figuran como clubes participantes.
> Es el **principal caso de prueba del sistema**, como pidió el tutor.

## 1. Situación actual (cómo se organiza hoy)

| Tarea | Cómo se hace hoy |
|---|---|
| Enfrentamientos (quién juega contra quién) | Por sorteo |
| Asignación de cancha y horario | A mano |
| Esfuerzo | **2 días de reuniones de 5 horas cada uno (≈ 10 horas)** |
| Difusión | Canales informales (WhatsApp, grupos) |

Problemas: superposiciones de horarios, canchas ociosas, equipos que no saben dónde ni cuándo juegan, y cada cambio obliga a rehacer la grilla a mano.

## 2. Datos del torneo

### Días y franjas horarias

| Día | Desde | Hasta |
|---|---|---|
| Sábado | 09:00 | 20:00 |
| Domingo | 09:00 | 16:00 |

### Sedes y canchas

| Sede | Canchas |
|---|---|
| Atlético María Juana | 4 |
| Litoral María Juana | 4 |
| **Total** | **8** |

Todas las canchas sirven para todas las categorías. Un equipo puede jugar en las dos sedes el mismo día (el descanso mínimo cubre el traslado).

### Categorías, ramas y equipos

| Categoría | Masculino | Femenino | Total |
|---|---|---|---|
| U9 | 10 | 5 | 15 |
| U11 | 12 | 9 | 21 |
| U13 | 8 | 11 | 19 |
| **Total** | **30** | **25** | **55** |

### Duración de los partidos

Todos los partidos se juegan en 4 cuartos, con reloj corrido (el tiempo no se detiene en cada falta).

| Concepto | U9 | U11 | U13 |
|---|---|---|---|
| Juego neto | 4 × 7 = 28 min | 4 × 8 = 32 min | 4 × 10 = 40 min |
| Pausas entre cuartos (1' + 2' entretiempo + 1') | 4 min | 4 min | 4 min |
| Margen por detenciones (tiros libres, imprevistos) | 10 min | 10 min | 10 min |
| Recambio (salida/entrada de equipos, calentamiento) | 10 min | 10 min | 10 min |
| **Duración programada** | **52 min** | **56 min** | **64 min** |
| **Bloque en cancha (turnos cada 15 min)** | **60 min** | **60 min** | **75 min** |

### Reglas del torneo

| Regla | Valor |
|---|---|
| Turnos cada | 15 minutos |
| Descanso mínimo entre partidos del mismo equipo | 90 minutos |
| Máximo de partidos por día por equipo | 3 |
| Formato | Zonas por sorteo, todos contra todos dentro de cada zona |
| Zonas impares | Una zona queda con un equipo menos |
| Fase final | No (cada zona tiene su ganador). Posible mejora futura |

## 3. Análisis de factibilidad

Con las reglas anteriores, un equipo puede jugar **como máximo 6 partidos** en el fin de semana (3 por día).

### Todos contra todos: no hay solución

| Grupo | Equipos | Partidos por equipo | ¿Entra? |
|---|---|---|---|
| U9 masc | 10 | 9 | No |
| U9 fem | 5 | 4 | Sí |
| U11 masc | 12 | 11 | No |
| U11 fem | 9 | 8 | No |
| U13 masc | 8 | 7 | No |
| U13 fem | 11 | 10 | No |

Además, todos contra todos son 240 partidos ≈ 261 horas de cancha, contra 144 disponibles (8 canchas × 18 horas).

**Resultado esperado del sistema:** "No existe una programación válida con la configuración actual".

### Con zonas: configuración elegida

| Grupo | Equipos | Zonas | Partidos por equipo | Partidos | Horas de cancha |
|---|---|---|---|---|---|
| U9 masc | 10 | 5 + 5 | 4 | 20 | 20 |
| U9 fem | 5 | Zona única | 4 | 10 | 10 |
| U11 masc | 12 | 4 + 4 + 4 | 3 | 18 | 18 |
| U11 fem | 9 | 5 + 4 | 4 / 3 | 16 | 16 |
| U13 masc | 8 | 4 + 4 | 3 | 12 | 15 |
| U13 fem | 11 | 4 + 4 + 3 | 3 / 2 | 15 | 18,75 |
| **Total** | **55** | **13 zonas** | | **91** | **≈ 98 de 144 (68%)** |

La ocupación del 68% hace que el caso no sea trivial: entra en total, pero el programador tiene que respetar a la vez canchas, descansos y topes diarios.

## 4. Datos de ejemplo: clubes de los equipos

El caso real no dice de qué club es cada equipo. Para que el ejemplo se parezca a un torneo real, `database/caso_real.sql`
usa **14 clubes con nombres reales** de básquet de Argentina, tomados de información pública: notas de los diarios
**Conclusión** y **La Capital** sobre el básquet de la región (D59).

> **Los equipos y su reparto entre los clubes son ficticios.** No representan la participación real de ningún club
> en este ni en ningún torneo. Responsable y teléfono siguen siendo de prueba.

Provincial · Sportsmen Unidos · Náutico Sportivo Avellaneda · Unión Sionista · Sportivo América ·
Santa Paula de Gálvez · Almagro de Esperanza · Atlético Carcarañá · Atalaya · Unión de Santo Tomé ·
Paganini Alumni de Granadero Baigorria · Alumni de Casilda · Unión de Santa Fe · Centenario de Venado Tuerto

**Reparto (sin azar, siempre igual):** los 55 equipos se numeran en orden (U9 masculino, U9 femenino, U11 masculino,
U11 femenino, U13 masculino, U13 femenino) y se reparten como cartas: el equipo *k* va al club número
((*k* − 1) mod 14) + 1, en el orden de la lista. Como ningún grupo tiene más de 14 equipos, un club nunca tiene dos
equipos en la misma categoría y rama. Resultado: 13 clubes con 4 equipos y 1 con 3, todos con equipos en 3 categorías
y en las 2 ramas. Cada equipo se llama como su club (en una misma categoría y rama no se repite).

| Club | Equipos |
|---|---|
| Provincial | U9 M, U9 F, U11 F, U13 M |
| Sportsmen Unidos | U9 M, U11 M, U11 F, U13 M |
| Náutico Sportivo Avellaneda | U9 M, U11 M, U11 F, U13 F |
| Unión Sionista | U9 M, U11 M, U11 F, U13 F |
| Sportivo América | U9 M, U11 M, U11 F, U13 F |
| Santa Paula de Gálvez | U9 M, U11 M, U11 F, U13 F |
| Almagro de Esperanza | U9 M, U11 M, U11 F, U13 F |
| Atlético Carcarañá | U9 M, U11 M, U11 F, U13 F |
| Atalaya | U9 M, U11 M, U13 M, U13 F |
| Unión de Santo Tomé | U9 M, U11 M, U13 M, U13 F |
| Paganini Alumni de Granadero Baigorria | U9 F, U11 M, U13 M, U13 F |
| Alumni de Casilda | U9 F, U11 M, U13 M, U13 F |
| Unión de Santa Fe | U9 F, U11 M, U13 M, U13 F |
| Centenario de Venado Tuerto | U9 F, U11 F, U13 M |

## 5. Referencias consultadas

- NBA & USA Basketball, *Youth Guidelines* (https://youthguidelines.nba.com): para 9 a 14 años recomienda como máximo 2 partidos por día, con precaución. No fija minutos de descanso. Nuestro tope de 3 es más permisivo.
- No se encontró una norma argentina pública que fije topes de partidos o descansos en torneos formativos. Los valores de este torneo los definió la organización.

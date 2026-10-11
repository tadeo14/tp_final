# Hoja de ruta

> Versión en el repo de la hoja de ruta del 10/10/2026 (la original es un PDF del chat del proyecto).
> Fase A = el corazón del sistema (lo que pide Oscar ahora). Fase B = el producto usable y online. Fase C = cierre del TP.

## Pasos

| # | Fase | Paso | Entregable | Estado |
|---|---|---|---|---|
| 1 | A | Definición del problema | `caso-real.md`, `restricciones.md`, `decisiones.md`, `CLAUDE.md` | HECHO |
| 2 | A | Modelo de datos | `der.md`, esquema SQL (migración V1), `pruebas_schema.sql` | HECHO |
| 3 | A | Proyecto Spring Boot base | Backend que arranca y crea las tablas (probado con PostgreSQL local, D24) | HECHO |
| 4 | A | Generador de enfrentamientos y zonas | Sorteo + todos contra todos, tests T1–T2 | HECHO |
| 5 | A | Validador de restricciones R1–R8 | `ValidadorRestricciones`, tests T4–T9 | HECHO |
| 6 | A | Programador de cancha y horario | Programador greedy, tests T3, T10, T11 | HECHO |
| 7 | A | API REST del recorrido | Endpoints + BORRADOR/PUBLICADO + datos del caso real | EN CURSO |
| 8 | A | Entrega de avance a Oscar | README actualizado + mensaje al tutor | PENDIENTE |
| 9 | B | Despliegue del backend | API online (Render o Railway) + base online; verificar que Flyway crea las tablas | PENDIENTE |
| 10 | B | Frontend del organizador | Pantallas: configurar, inscribir, generar, publicar | PENDIENTE |
| 11 | B | Vista pública y difusión | Fixture por equipo/cancha/horario + link para WhatsApp | PENDIENTE |
| 12 | B | Despliegue del frontend | App online (Vercel o Netlify) | PENDIENTE |
| 13 | C | Documentación e informe final | README final, informe, merge a `master` | PENDIENTE |
| 14 | C | Video | Guion en inglés + grabación | PENDIENTE |
| 15 | C | Defensa oral | Presentación + ensayo de preguntas | PENDIENTE |

## Mejoras P1 (si sobra tiempo, en este orden)

Ninguna se empieza antes de terminar la fase A.

1. **Repartir los partidos de cada equipo entre los días.** Hoy el programador llena el sábado primero:
   con el caso real, 35 de 55 equipos juegan solo el sábado (ver `algoritmo-programador.md`, "Limitación conocida").
2. Fase final con cruces entre zonas.
3. Edición manual de partidos (validada con las mismas reglas R1–R8).
4. Carga de resultados.
5. Turnos de comidas.

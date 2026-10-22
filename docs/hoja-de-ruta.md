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
| 7 | A | API REST del recorrido | Endpoints + BORRADOR/PUBLICADO + datos del caso real | HECHO |
| 8 | A | Entrega de avance a Oscar | README actualizado + mensaje al tutor | PENDIENTE |
| 9 | B | Despliegue del backend | Render (Docker) + Supabase. Imagen, `render.yaml` y guía en `deploy.md` listos; falta crear las cuentas y el servicio | EN CURSO |
| 10 | B | Frontend del organizador | Pantallas: configurar, inscribir, generar, publicar | PENDIENTE |
| 11 | B | Vista pública y difusión | Fixture por equipo/cancha/horario + link para WhatsApp | PENDIENTE |
| 12 | B | Despliegue del frontend | App online (Vercel o Netlify) | PENDIENTE |
| 13 | C | Documentación e informe final | README final, informe, merge a `master` | PENDIENTE |
| 14 | C | Video | Guion en inglés + grabación | PENDIENTE |
| 15 | C | Defensa oral | Presentación + ensayo de preguntas | PENDIENTE |

## A confirmar con el organizador real

**Reparto de los partidos entre sábado y domingo.** Hoy el programador llena el sábado primero: con el caso real,
35 de 55 equipos juegan solo el sábado (ver `algoritmo-programador.md`, "Limitación conocida"). El fixture es válido.

No se implementa nada hasta confirmarlo con el organizador real: para clubes y familias puede ser preferible jugar
un solo día y evitar un segundo traslado (devolución G165-dev_3). Si el organizador pide repartir, entra como
**preferencia de optimización** del programador, **no** como una restricción obligatoria nueva (R1–R8 no cambian).

## Mejoras P1 (si sobra tiempo, en este orden)

Ninguna se empieza antes de terminar la fase A. Según la devolución G165-dev_3, el foco ahora es
autenticación → frontend mínimo → despliegue → prueba con el organizador real, no módulos nuevos.

1. Fase final con cruces entre zonas.
2. Edición manual de partidos (validada con las mismas reglas R1–R8).
3. Carga de resultados.
4. Turnos de comidas.

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
| 10 | B | Frontend del organizador | React + Vite (D54): login, mis torneos, configuración, recorrido, publicación con versiones | HECHO |
| 11 | B | Vista pública y difusión | Equipos y fixture por equipo sin login + botón de WhatsApp (D57) | HECHO |
| 12 | B | Despliegue del frontend | Sitio estático de Render (D58). Configuración y guía en `deploy.md` listas; falta crear el sitio | EN CURSO |
| 13 | C | Documentación e informe final | README final, informe, merge a `master` | PENDIENTE |
| 14 | C | Video | Guion en inglés + grabación | PENDIENTE |
| 15 | C | Defensa oral | Presentación + ensayo de preguntas | PENDIENTE |

## Consultado con el organizador real

**Reparto de los partidos entre sábado y domingo (resuelto, D64).** El programador llena el sábado primero: con el
caso real, 35 de 55 equipos juegan solo el sábado (ver `algoritmo-programador.md`, "Limitación conocida"). El fixture
es válido. La consulta se hizo y **el organizador no tiene problema con cómo queda hoy**: no se cambia el programador.

## Mejoras P1 (si sobra tiempo, en este orden)

Ninguna se empieza antes de terminar la fase A. Según la devolución G165-dev_3, el foco ahora es
autenticación → frontend mínimo → despliegue → prueba con el organizador real, no módulos nuevos.

1. Fase final con cruces entre zonas.
2. Edición manual de partidos (validada con las mismas reglas R1–R8).
3. Carga de resultados.
4. Turnos de comidas.

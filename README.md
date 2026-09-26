<!-- /docs/propuesta.md (o README.md en la raíz del repo) -->

# 🏀 Fixture y Difusión de Torneos de Básquet

Proyecto Final — **Tecnicatura Universitaria en Programación a Distancia (UTN)**

## 📌 Descripción

Los organizadores de torneos de básquet en clubes locales arman a mano el fixture,
la asignación de canchas y horarios, y la coordinación de comidas, y difunden esa
información por canales informales (WhatsApp, grupos, planillas impresas). Esto
genera superposiciones de horarios, canchas ociosas y equipos que no saben dónde
ni cuándo juegan.

Este proyecto propone una **app web** que centraliza en un solo lugar la
configuración del torneo, la generación automática del fixture (sin superposiciones
y con descanso mínimo entre partidos), la logística de comidas y una vista pública
de difusión, para torneos de cualquier categoría y tamaño (infantiles, juveniles o
mayores).

## 🎯 Objetivos

**Objetivo general**

Desarrollar una plataforma web que automatice la organización de torneos de
básquet (fixture, canchas, horarios, comidas) y publique la información en una
vista pública de solo lectura.

**Objetivos específicos**

- Permitir configurar un torneo (días, sedes, canchas, franjas horarias, categorías y ramas) sin valores fijos en el código.
- Permitir inscribir equipos por club, categoría y rama.
- Generar automáticamente un fixture sin superposiciones de cancha/horario y con descanso mínimo entre partidos de un mismo equipo.
- Asignar turnos de comida a los equipos según su propia grilla de partidos.
- Publicar una vista pública del fixture, filtrable por equipo, cancha u horario, con link para compartir.

## 🔎 Antecedentes

*Información pública relevada de cada sitio, no verificada — se toma solo la idea, no diseño ni marca.*

| App | Qué resuelve | Qué tomamos de referencia |
|---|---|---|
| Playinga | Fixture multisede, calendario drag & drop, detección de conflictos | Idea de detección automática de superposiciones |
| Xporty | Horarios automáticos según disponibilidad de cancha, fases y clasificaciones | Enfoque de asignación automática de horarios |
| Enjore | Página pública del torneo y difusión | Vista pública de solo lectura |
| Exposure Basketball | Flujo específico de básquet: agenda, tablas y resultados | Validación de agenda por equipo |
| Reservaplay | Formulario de inscripción por categoría | Formulario de inscripción de equipos |

## 💡 Diferencial

Las apps de referencia ya resuelven fixture, canchas y horarios por separado. Nuestro
diferencial es una herramienta **simple, en español y gratuita**, pensada para clubes
locales (no ligas profesionales), que integra en un solo lugar **fixture + logística
de comidas + difusión pública**, algo que ninguna de las referencias cubre junta.

## 🧩 Alcance del proyecto (MVP)

| Módulo | Organizador | Público (sin login) |
|---|---|---|
| Configuración del torneo | Define sedes, canchas, franjas, categorías y ramas | — |
| Inscripción de equipos | Carga club, categoría, rama y contacto del responsable | — |
| Fixture automático | Genera partidos sin superposición ni conflicto de cancha | Ve el fixture completo |
| Turnos de comida | Asigna equipos a turnos según su grilla de partidos | Ve su turno asignado |
| Vista pública | — | Filtra por equipo, cancha u horario; link para compartir |


## 🛠️ Stack tecnológico

| Área | Tecnología |
|---|---|
| Backend | Java + Spring Boot (Spring Data JPA) |
| Frontend | React |
| Base de datos | PostgreSQL |
| Despliegue | Backend + BD en Render/Railway; frontend en Vercel/Netlify |
| Control de versiones | Git y GitHub |

## 🗓️ Plan de trabajo

| Etapa | Actividad | Duración estimada |
|---|---|---|
| 1 | Propuesta, tutor, repo, antecedentes | Semanas 1-2 |
| 2 | Diseño: modelo de datos y listado de módulos | Semanas 2-3 |
| 3 | Backend: modelos, API REST, algoritmo de fixture | Semanas 3-6 |
| 4 | Frontend: configuración, inscripción, vista pública | Semanas 5-7 |
| 5 | Integración, datos de prueba, turnos de comida | Semana 8 |
| 6 | Despliegue, documentación, video | Semanas 9-10 |

> Estimación inicial, se ajusta con el tutor tras aprobar la propuesta.

## 👥 Equipo

- Daniel
- Tadeo

**Tutor propuesto:** Oscar Londero

## 📂 Repositorio

Todo el desarrollo (backend, frontend, base de datos y documentación) vive en este
repositorio, organizado en `/frontend`, `/backend`, `/database` y `/docs`.

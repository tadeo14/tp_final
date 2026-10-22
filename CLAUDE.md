# CLAUDE.md — Fixture de Torneos de Básquet (TFI UTN)

Este archivo le da contexto a Claude Code. Leelo completo antes de cada tarea.

## Proyecto

Trabajo Final Integrador de la Tecnicatura Universitaria en Programación (UTN, a distancia).
Integrantes: Daniel y Tadeo. Tutor: Oscar Londero.

App web para organizar torneos formativos de básquet: genera los enfrentamientos, los programa
en canchas y horarios sin conflictos, y publica el fixture en una vista pública de solo lectura.

**Antes de cualquier tarea leé:**
- `docs/caso-real.md` — torneo real de referencia y análisis de factibilidad (es el caso de prueba principal)
- `docs/restricciones.md` — parámetros, restricciones obligatorias R1–R8 y batería de tests T1–T11
- `docs/decisiones.md` — decisiones tomadas y su justificación

Si algo de este archivo contradice esos documentos, mandan los documentos. Si encontrás una contradicción, avisá antes de seguir.

## Stack

| Área | Tecnología |
|---|---|
| Backend | Java 17 + Spring Boot 3 + Spring Data JPA, Maven |
| Base de datos | PostgreSQL |
| Tests | JUnit 5 |
| Frontend | React (más adelante, no es prioridad ahora) |
| Despliegue | Backend + BD en Render o Railway; frontend en Vercel o Netlify |

## Estructura del repo

```
/backend    Proyecto Spring Boot (las migraciones Flyway van en backend/src/main/resources/db/migration)
/frontend   App React (todavía no)
/database   Datos de ejemplo (seed) y pruebas de la base. El esquema NO va acá: ver D21
/docs       Documentación, decisiones, DER, informes
```

Todo archivo nuevo va en una de estas carpetas. Única excepción: `render.yaml` en la raíz, porque Render lo exige ahí (D51).

## Alcance actual (P0)

**Recorrido mínimo a demostrar:**
crear torneo → categoría → equipos → canchas y franjas → sortear zonas → generar enfrentamientos
→ programar → validar restricciones → publicar → consultar por equipo.

**Entra en P0:**
- Configuración del torneo (días, franjas, sedes, canchas, categorías, ramas, parámetros)
- Inscripción de equipos: nombre, club, categoría, rama, responsable y teléfono
- Zonas por sorteo (resultado guardado) y todos contra todos dentro de cada zona, con "libre" si es impar
- Programación en cancha y horario respetando R1–R8
- Mensaje "No existe una programación válida con la configuración actual" cuando no hay solución
- Estados BORRADOR y PUBLICADO
- API REST para el recorrido; la interfaz visual puede esperar

**No entra (no lo implementes salvo pedido explícito):**
fase final o cruces, comidas, edición manual de partidos, optimización de tiempos muertos,
carga de resultados, jugadores, pagos, notificaciones, roles complejos.

## Reglas de diseño

1. **Ningún parámetro del torneo va hardcodeado.** Duraciones, descanso, tope diario, "turnos cada",
   recambio y franjas vienen de la configuración. Los valores del caso real solo aparecen en tests y en datos de ejemplo.
2. **Separar dos problemas:** `GeneradorEnfrentamientos` (quién contra quién) y `Programador` (cancha y horario).
3. **Un único `ValidadorRestricciones`** que verifica R1–R8. Lo usa el programador antes de devolver un resultado
   y el servicio antes de publicar. Nunca duplicar la lógica de validación.
4. **El algoritmo va en clases de dominio puras** (sin Spring ni JPA), así se testea con JUnit sin base de datos.
5. **Nunca relajar restricciones** para que "entre". Si no hay solución, se informa.
6. **Datos derivados no se guardan:** la duración programada y el bloque se calculan.
7. **Datos personales mínimos:** solo responsable y teléfono del equipo. Nada de DNI, fotos ni salud.
8. Nombres de dominio en español (`Torneo`, `Partido`, `Cancha`), código y comentarios claros.

## Cómo trabajar

- **Pasos chicos.** Una funcionalidad por vez, con su test. No generes todo el proyecto de una.
- **Versión simple primero.** Para el programador, empezá con un algoritmo greedy explicable
  (ordenar partidos de los grupos más restringidos primero y asignar el primer turno y cancha válidos).
  Si no alcanza, proponé mejoras antes de implementarlas.
- **Tests primero en el algoritmo:** implementá los casos de `docs/restricciones.md` (T1–T11).
- **Explicá cada decisión técnica en 2 o 3 líneas** y registrala en `docs/decisiones.md`.
  Los integrantes tienen que poder defender todo el código en una oral.
- **Preguntá antes de:** cambiar el alcance, agregar dependencias que no estén en el stack,
  o elegir algo que afecte la base de datos (por ejemplo, herramienta de migraciones).
- **No inventes datos de torneos.** Usá los del caso real o datos marcados como de prueba.
- Commits chicos y descriptivos en español.

## Próxima entrega (lo que pidió el tutor)

1. DER en `docs/der.md` (Mermaid) y esquema SQL (hoy migración Flyway V1, ver D21)
2. Proyecto Spring Boot en `/backend` que compile y arranque
3. Generador de enfrentamientos y zonas, con tests
4. Validador R1–R8 y programador, con tests T1–T11
5. Endpoints REST mínimos para el recorrido completo

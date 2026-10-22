# 🏀 Fixture de Torneos de Básquet

Trabajo Final Integrador — **Tecnicatura Universitaria en Programación a Distancia (UTN)**
Equipo: Daniel y Tadeo · Tutor: Oscar Londero

API web que organiza torneos formativos de básquet: arma los enfrentamientos, los programa en canchas y
horarios **sin conflictos** y publica el fixture en una vista pública de solo lectura.

## Alcance (P0)

Recorrido: crear torneo → categorías → equipos → canchas y franjas → **sortear zonas** → **generar
enfrentamientos** (todos contra todos dentro de cada zona, con fecha libre si es impar) → **programar** →
**validar** las reglas R1–R8 → **publicar** → consultar por equipo.

- Si no hay solución, el sistema lo informa y **no relaja reglas**.
- Estados BORRADOR y PUBLICADO. Un torneo publicado no se modifica; para corregirlo se reabre y se publica una
  nueva versión con motivo, y el público sigue viendo la última versión publicada (D46, D47).
- **Fuera de P0:** comidas, fase final, edición manual, resultados, jugadores, notificaciones.

## Caso real

El principal caso de prueba es un torneo real anonimizado ([`docs/caso-real.md`](docs/caso-real.md)):
**55 equipos**, U9/U11/U13 masculino y femenino, **2 sedes × 4 canchas**, sábado y domingo.

- Con todos contra todos **no hay solución** (un equipo de 10 necesitaría 9 partidos y puede jugar 6).
- Con **13 zonas** sí: **91 partidos**, programados sin violar ninguna regla.

## Qué está hecho

| Paso | Entregable | Docs |
|---|---|---|
| 1. Definición del problema | Caso real, restricciones R1–R8, decisiones | [`restricciones.md`](docs/restricciones.md), [`decisiones.md`](docs/decisiones.md) |
| 2. Modelo de datos | DER y migraciones Flyway V1 (11 tablas) y V2 (versiones publicadas) | [`der.md`](docs/der.md) |
| 3. Proyecto Spring Boot | Java 17, Spring Boot 3.5, JPA, Flyway, PostgreSQL | [`backend/README.md`](backend/README.md) |
| 4. Sorteo y enfrentamientos | Método del círculo — tests T1, T2 | [`algoritmo-enfrentamientos.md`](docs/algoritmo-enfrentamientos.md) |
| 5. Validador R1–R8 | Un solo `ValidadorRestricciones` — tests T4 a T9 | [`restricciones.md`](docs/restricciones.md) |
| 6. Programador | Greedy + chequeo por conteo — tests T3, T10, T11 | [`algoritmo-programador.md`](docs/algoritmo-programador.md) |
| 7. API REST | Recorrido completo, BORRADOR/PUBLICADO con versiones, datos del caso real, login con JWT | [`backend/README.md`](backend/README.md) |

**106 tests** (JUnit 5), todos pasan. Plan completo en [`docs/hoja-de-ruta.md`](docs/hoja-de-ruta.md);
propuesta original (objetivos, antecedentes, diferencial) en [`docs/propuesta.md`](docs/propuesta.md).

## Cómo correrlo

Requiere Java 17+ y un PostgreSQL. La configuración va por variables de entorno (nunca en el repo):

```bash
cd backend
export DB_URL=jdbc:postgresql://localhost:5432/fixture_test DB_USER=fixture DB_PASSWORD=fixture
./mvnw test             # tests (no dejan datos en la base)

export JWT_SECRETO="$(head -c 48 /dev/urandom | base64)" JWT_EXPIRACION_MIN=120
./mvnw spring-boot:run  # API en http://localhost:8080 — Flyway crea las tablas
```

Las operaciones del organizador exigen iniciar sesión (JWT, D44) y cada organizador solo ve sus torneos (D45);
la vista pública no pide login.

**Recorrido con el caso real:** registrar el organizador `ejemplo@fixture.test`, ejecutar
[`database/caso_real.sql`](database/caso_real.sql) en la base, iniciar sesión y seguir
[`backend/recorrido.http`](backend/recorrido.http) (sortear → generar → programar → validar → publicar → consultar
un equipo). Detalle en [`backend/README.md`](backend/README.md).

## Limitaciones conocidas

- **Reparto entre días:** el programador llena el sábado primero; con el caso real, 35 de 55 equipos juegan solo
  el sábado. El fixture es válido. Antes de cambiarlo hay que confirmar con el organizador real si es un problema;
  si lo es, entra como preferencia de optimización, no como regla nueva.
- **Base online:** el entorno de desarrollo no llega a Neon; por ahora se prueba con PostgreSQL local (D24).
  La base online se configura en el despliegue (paso 9).

## Stack

Java 17 · Spring Boot 3.5 · Spring Data JPA · Flyway · PostgreSQL · JUnit 5 · Maven.
Más adelante: frontend en React y despliegue (Render/Railway + Vercel/Netlify).

## Estructura

```
/backend    API Spring Boot (dominio puro, entidades, servicios, controladores, tests)
/database   Datos de ejemplo del caso real y pruebas del esquema
/docs       Caso real, restricciones, decisiones, DER, algoritmos, hoja de ruta
/frontend   (fase B)
```

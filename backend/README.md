# Backend — Fixture de Torneos de Básquet

Spring Boot 3.5 · Java 17 · Maven (incluye wrapper: no hace falta instalar Maven, solo Java 17 o superior).

## Variables de entorno

| Variable | Ejemplo |
|---|---|
| `DB_URL` | En local: `jdbc:postgresql://localhost:5432/fixture_test`. En Supabase: el Session pooler, `jdbc:postgresql://aws-0-<region>.pooler.supabase.com:5432/postgres?sslmode=require` (ver [`docs/deploy.md`](../docs/deploy.md)) |
| `DB_USER` | usuario de la base (en Supabase: `postgres.<id-del-proyecto>`) |
| `DB_PASSWORD` | contraseña de la base — **nunca** se sube al repo |
| `JWT_SECRETO` | secreto para firmar los tokens, al menos 32 caracteres. Generarlo con `head -c 48 /dev/urandom \| base64` — **nunca** se sube al repo |
| `JWT_EXPIRACION_MIN` | (opcional) minutos que dura un token; `480` si no se define |
| `CORS_ORIGENES` | (opcional) dominios del frontend separados por coma, por ejemplo `https://fixture-frontend.onrender.com`. En local, `http://localhost:5173` para usar el frontend |

| `PORT` | (opcional) puerto HTTP; `8080` si no se define. Render lo define solo |

Sin `JWT_SECRETO` la aplicación no arranca. Los tests no lo necesitan: generan un secreto al azar.

## Docker

```bash
docker build -t fixture-backend .
docker run -p 8080:8080 -e DB_URL=... -e DB_USER=... -e DB_PASSWORD=... -e JWT_SECRETO=... fixture-backend
```

La imagen compila con Maven y JDK 17 y corre con el JRE 17, con la memoria limitada para el plan gratis de
Render (D49). Despliegue paso a paso en [`docs/deploy.md`](../docs/deploy.md).

## Comandos

```bash
./mvnw test             # tests (usan la base de DB_URL; no dejan datos)
./mvnw spring-boot:run  # arranca en http://localhost:8080
```

En Windows: `mvnw.cmd` en lugar de `./mvnw`.

Al arrancar, Flyway crea o actualiza las tablas con las migraciones de
`src/main/resources/db/migration` (D21). Para comprobar que responde: `GET /api/salud`.

## Tests con PostgreSQL local

Así se corren en el entorno de Claude Code (D24). Sirve igual en cualquier PC con PostgreSQL instalado.

1. Crear un usuario y una base vacía para los tests (una sola vez):

   ```bash
   sudo -u postgres psql -c "CREATE ROLE fixture LOGIN PASSWORD 'fixture'"
   sudo -u postgres psql -c "CREATE DATABASE fixture_test OWNER fixture"
   ```

   Es una base local de desarrollo, por eso la contraseña simple. Nunca usar estas credenciales en una base online.

2. Correr los tests apuntando a esa base:

   ```bash
   DB_URL=jdbc:postgresql://localhost:5432/fixture_test DB_USER=fixture DB_PASSWORD=fixture ./mvnw test
   ```

Flyway crea las tablas la primera vez. Los tests no dejan datos: cada uno hace rollback al terminar.
Si cambia una migración ya aplicada, borrar y crear de nuevo la base (`DROP DATABASE fixture_test`).

## API

Errores con formato `{"error": "...", "detalles": [...]}`: 400 datos inválidos, 401 falta iniciar sesión o
login incorrecto, 404 no existe (o es de otro organizador, D45), 409 torneo publicado (D20, D39: hay que reabrirlo) o dato repetido,
422 sin programación válida o fixture que no cumple R1–R8.

**Login (público)**

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/api/auth/registro` | Registra un organizador (la contraseña se guarda como hash PBKDF2) |
| POST | `/api/auth/login` | Devuelve un token JWT. Se manda en `Authorization: Bearer {token}` |

**Configuración (organizador, con token)** — cada organizador solo ve y edita sus torneos (D45).

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/api/torneos` | Mis torneos |
| POST | `/api/torneos` | Crea un torneo en BORRADOR, a nombre del organizador del token |
| GET | `/api/torneos/{id}` | Datos y estado del torneo |
| GET | `/api/torneos/{id}/configuracion` | Toda la configuración: franjas, sedes con sus canchas, categorías, ramas, clubes y zonas |
| POST | `/api/torneos/{id}/franjas` | Agrega una franja horaria |
| POST | `/api/torneos/{id}/sedes` | Agrega una sede |
| POST | `/api/sedes/{sedeId}/canchas` | Agrega una cancha a la sede |
| POST | `/api/torneos/{id}/categorias` | Agrega una categoría con sus duraciones y tope diario |
| POST | `/api/torneos/{id}/ramas` | Agrega una rama |
| POST | `/api/torneos/{id}/clubes` | Agrega un club |
| POST | `/api/torneos/{id}/zonas` | Define una zona de una categoría y rama |
| POST | `/api/torneos/{id}/equipos` | Inscribe un equipo |
| GET | `/api/torneos/{id}/equipos` | Lista los equipos (sin datos de contacto) |

**Fixture (organizador, con token)**

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/api/torneos/{id}/sorteo` | Sortea los equipos en las zonas y guarda el resultado |
| POST | `/api/torneos/{id}/fixture/generar` | Todos contra todos en cada zona |
| POST | `/api/torneos/{id}/fixture/programar` | Asigna cancha y horario (422 si no hay solución) |
| GET | `/api/torneos/{id}/fixture` | Fixture completo, con el fin calculado de cada partido |
| GET | `/api/torneos/{id}/validacion` | Resultado de R1–R8 |
| POST | `/api/torneos/{id}/publicar` | Publica si cumple R1–R8: crea la versión siguiente con una foto de los partidos. Desde la versión 2 exige `{"motivo": "..."}` |
| POST | `/api/torneos/{id}/reabrir` | Vuelve a BORRADOR un torneo publicado para corregirlo; el público sigue viendo la última versión |
| GET | `/api/torneos/{id}/publicaciones` | Historial de versiones: número, fecha y motivo |

**Vista pública (sin token)** — muestra siempre la última versión publicada (con su número, fecha y motivo),
aunque el torneo se haya reabierto. Un torneo nunca publicado responde 404.

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/api/publico/torneos/{id}/equipos` | Equipos del torneo |
| GET | `/api/publico/torneos/{id}/equipos/{equipoId}/fixture` | Partidos del equipo y sus fechas libres |

## Probar el recorrido con el caso real

1. Arrancar el backend una vez, para que Flyway cree las tablas.
2. Registrar el organizador `ejemplo@fixture.test` con `POST /api/auth/registro` (está en `recorrido.http`).
3. Ejecutar [`database/caso_real.sql`](../database/caso_real.sql) en la base: carga el torneo del caso real
   (55 equipos, 13 zonas, 8 canchas) a nombre de ese organizador y muestra su id.
4. Seguir [`recorrido.http`](recorrido.http): login, sortear, generar, programar, validar, publicar y consultar un equipo.

`recorrido.http` usa el formato de la extensión REST Client de VS Code. Tiene también un recorrido
desde cero con un torneo chico de prueba.

## Paquetes (`ar.edu.utn.fixture`)

| Paquete | Qué contiene |
|---|---|
| `dominio` | Algoritmo del fixture en clases puras, sin Spring ni JPA |
| `entidad` | Clases JPA, una por tabla |
| `repositorio` | Acceso a datos (Spring Data JPA) |
| `servicio` | Casos de uso: cargan datos, llaman al dominio y guardan el resultado |
| `controlador` | Endpoints REST |
| `seguridad` | Login con JWT: rutas públicas y protegidas, filtro del token, CORS |
| `dto` | Datos de entrada y salida de la API |

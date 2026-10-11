# Backend — Fixture de Torneos de Básquet

Spring Boot 3.5 · Java 17 · Maven (incluye wrapper: no hace falta instalar Maven, solo Java 17 o superior).

## Variables de entorno

| Variable | Ejemplo |
|---|---|
| `DB_URL` | `jdbc:postgresql://<host>.neon.tech/<base>?sslmode=require` (endpoint directo, no el `-pooler`) |
| `DB_USER` | usuario de la base |
| `DB_PASSWORD` | contraseña de la base — **nunca** se sube al repo |

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

Errores con formato `{"error": "...", "detalles": [...]}`: 400 datos inválidos, 404 no existe,
409 torneo publicado (D20, D39) o dato repetido, 422 sin programación válida o fixture que no cumple R1–R8.

**Configuración (organizador)**

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/api/organizadores` | Crea un organizador (la contraseña se guarda como hash) |
| POST | `/api/torneos` | Crea un torneo en BORRADOR |
| GET | `/api/torneos/{id}` | Datos y estado del torneo |
| POST | `/api/torneos/{id}/franjas` | Agrega una franja horaria |
| POST | `/api/torneos/{id}/sedes` | Agrega una sede |
| POST | `/api/sedes/{sedeId}/canchas` | Agrega una cancha a la sede |
| POST | `/api/torneos/{id}/categorias` | Agrega una categoría con sus duraciones y tope diario |
| POST | `/api/torneos/{id}/ramas` | Agrega una rama |
| POST | `/api/torneos/{id}/clubes` | Agrega un club |
| POST | `/api/torneos/{id}/zonas` | Define una zona de una categoría y rama |
| POST | `/api/torneos/{id}/equipos` | Inscribe un equipo |
| GET | `/api/torneos/{id}/equipos` | Lista los equipos (sin datos de contacto) |

**Fixture (organizador)**

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/api/torneos/{id}/sorteo` | Sortea los equipos en las zonas y guarda el resultado |
| POST | `/api/torneos/{id}/fixture/generar` | Todos contra todos en cada zona |
| POST | `/api/torneos/{id}/fixture/programar` | Asigna cancha y horario (422 si no hay solución) |
| GET | `/api/torneos/{id}/fixture` | Fixture completo, con el fin calculado de cada partido |
| GET | `/api/torneos/{id}/validacion` | Resultado de R1–R8 |
| POST | `/api/torneos/{id}/publicar` | Publica si cumple R1–R8; después no se puede modificar |

**Vista pública (sin login, solo torneos publicados)**

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/api/publico/torneos/{id}/equipos` | Equipos del torneo |
| GET | `/api/publico/torneos/{id}/equipos/{equipoId}/fixture` | Partidos del equipo y sus fechas libres |

## Probar el recorrido con el caso real

1. Arrancar el backend una vez, para que Flyway cree las tablas.
2. Ejecutar [`database/caso_real.sql`](../database/caso_real.sql) en la base: carga el torneo del caso real
   (55 equipos, 13 zonas, 8 canchas) y muestra su id.
3. Seguir [`recorrido.http`](recorrido.http): sortear, generar, programar, validar, publicar y consultar un equipo.

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

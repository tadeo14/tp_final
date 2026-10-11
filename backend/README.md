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

## Paquetes (`ar.edu.utn.fixture`)

| Paquete | Qué contiene |
|---|---|
| `dominio` | Algoritmo del fixture en clases puras, sin Spring ni JPA |
| `entidad` | Clases JPA, una por tabla |
| `repositorio` | Acceso a datos (Spring Data JPA) |
| `servicio` | Casos de uso: cargan datos, llaman al dominio y guardan el resultado |
| `controlador` | Endpoints REST |

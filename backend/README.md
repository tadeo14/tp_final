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

## Paquetes (`ar.edu.utn.fixture`)

| Paquete | Qué contiene |
|---|---|
| `dominio` | Algoritmo del fixture en clases puras, sin Spring ni JPA |
| `entidad` | Clases JPA, una por tabla |
| `repositorio` | Acceso a datos (Spring Data JPA) |
| `servicio` | Casos de uso: cargan datos, llaman al dominio y guardan el resultado |
| `controlador` | Endpoints REST |

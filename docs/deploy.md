# Despliegue: backend en Render + base en Supabase

> Paso 9 de la hoja de ruta. El backend corre en **Render** (web service con Docker, plan gratis) y la base
> PostgreSQL en **Supabase** (plan gratis). Decisiones D49 a D53.

```
Navegador / recorrido.http ──HTTPS──▶ Render: fixture-backend (Docker, Java 17)
                                             │
                                             │ JDBC + SSL, Session pooler (IPv4, puerto 5432)
                                             ▼
                                      Supabase: PostgreSQL (tablas creadas por Flyway)
```

Nada de esto requiere instalar programas: todo se hace desde el navegador. Para el backup (sección 5) sí hace
falta `pg_dump`.

## 1. Supabase: crear la base

1. Entrar a <https://supabase.com>, crear una cuenta (se puede con GitHub) y una organización.
2. **New project**:
   - **Name:** `fixture-basquet` (o el que quieran).
   - **Database password:** usar **Generate a password** y **guardarla** en un lugar seguro (gestor de contraseñas).
     Es `DB_PASSWORD`. No se vuelve a mostrar.
   - **Region:** **East US (North Virginia)**. Conviene que esté cerca de Render (paso 2), no de los usuarios:
     cada pedido a la API hace varias consultas a la base.
   - **Data API: desactivarla** (en las opciones de seguridad al crear el proyecto). Ver el recuadro de abajo.
3. Esperar a que el proyecto termine de crearse (1 o 2 minutos).
4. **No crear tablas a mano.** Las crea Flyway la primera vez que arranca el backend (V1, V2 y V3).

> **⚠️ Desactivar la Data API (obligatorio).** Supabase publica automáticamente las tablas del esquema `public`
> en una API REST. Nuestras tablas no tienen reglas de acceso (RLS) porque el backend se conecta directo a la base,
> así que con esa API cualquiera podría leer los hashes de contraseña y los teléfonos de los responsables.
> El backend no usa esa API: desactivarla no afecta nada.
>
> - Al crear el proyecto: destildar la opción de la Data API.
> - Si ya está creado: **Project Settings → Data API** → desactivarla (o sacar `public` de *Exposed schemas*).
> - Para comprobarlo: **Advisors → Security Advisor** no debería marcar "RLS disabled in public" como accesible por la API.

### Los datos de conexión (Session pooler)

1. En el proyecto, botón **Connect** (arriba).
2. Elegir **Session pooler**. **No** usar:
   - *Direct connection* (`db.<id>.supabase.co`): es solo IPv6, y Render no tiene IPv6 saliente.
   - *Transaction pooler* (puerto **6543**): no admite sentencias preparadas y falla con Flyway.
3. Supabase muestra algo así:

   ```
   postgresql://postgres.abcdefghijklmnop:[YOUR-PASSWORD]@aws-0-us-east-1.pooler.supabase.com:5432/postgres
   ```

4. De ahí salen tres variables (el usuario va separado, no dentro de la URL):

   | Variable | Valor (ejemplo) |
   |---|---|
   | `DB_URL` | `jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require` |
   | `DB_USER` | `postgres.abcdefghijklmnop` (con el punto y el id del proyecto) |
   | `DB_PASSWORD` | la contraseña del paso 2 |

   Ojo: `DB_URL` empieza con `jdbc:`, no lleva usuario ni contraseña, y termina en `?sslmode=require`.

## 2. Render: crear el servicio

Hay dos formas. La **A** es la que se recomienda; la **B** usa el archivo `render.yaml` del repo.

### A. Desde el panel (recomendada)

1. Entrar a <https://render.com>, crear una cuenta con GitHub y darle acceso al repo `tadeo14/tp_final`.
2. **New → Web Service** → elegir el repo `tadeo14/tp_final`.
3. Completar:

   | Campo | Valor |
   |---|---|
   | **Name** | `fixture-backend` (define la URL: `https://fixture-backend.onrender.com`) |
   | **Language / Runtime** | **Docker** |
   | **Branch** | `developC` |
   | **Region** | **Virginia (US East)** (la misma zona que Supabase) |
   | **Root Directory** | `backend` |
   | **Dockerfile Path** | `./Dockerfile` (relativo a `backend`) |
   | **Instance Type** | **Free** |

   > Si el campo *Dockerfile Path* no muestra el prefijo `backend/` y el build falla con "Dockerfile not found",
   > poner `./backend/Dockerfile` y, en *Docker Build Context Directory*, `./backend`.

4. **Environment Variables** (ver la tabla de la sección 3).
5. **Advanced → Health Check Path:** `/api/salud`.
6. **Deploy Web Service.** El primer build tarda unos minutos (descarga Maven y las dependencias).

### B. Con el Blueprint (`render.yaml`)

1. **New → Blueprint** → elegir el repo `tadeo14/tp_final` y la rama `developC`.
2. Render lee `render.yaml` (en la raíz del repo) y crea el servicio `fixture-backend` con Docker, plan gratis,
   health check y la lista de variables.
3. Pide los valores de las variables marcadas sin valor. `JWT_SECRETO` lo genera Render solo.

## 3. Variables de entorno en Render

| Variable | Obligatoria | Valor |
|---|---|---|
| `DB_URL` | Sí | La del Session pooler (sección 1) |
| `DB_USER` | Sí | `postgres.<id-del-proyecto>` |
| `DB_PASSWORD` | Sí | La contraseña de la base |
| `JWT_SECRETO` | Sí | Al menos 32 caracteres al azar. En el panel, el botón **Generate** de Render; o `openssl rand -base64 48` |
| `JWT_EXPIRACION_MIN` | No | Minutos que dura un login. Si no se define: **480** (8 horas) |
| `CORS_ORIGENES` | No | Dominio del frontend cuando exista, ej. `https://fixture-front.onrender.com`. Vacío: ningún otro dominio puede llamar a la API |

**No hay que definir:**
- `PORT`: lo pone Render, y la aplicación lo lee.
- `JAVA_TOOL_OPTIONS`: ya viene en la imagen y limita la memoria para el plan gratis.

Los valores **nunca** se suben al repo ni se pegan en chats: solo en el panel de Render.

## 4. Verificar el despliegue

1. **Logs del servicio en Render.** Tienen que aparecer:
   ```
   Successfully applied 3 migrations to schema "public", now at version v3
   Started FixtureApplication in ... seconds
   ```
   La primera vez. En los arranques siguientes dice `Schema "public" is up to date`.
2. **Salud:** abrir `https://fixture-backend.onrender.com/api/salud` en el navegador. Tiene que responder:
   ```json
   {"estado":"OK","base":"conectada"}
   ```
3. **Tablas:** en Supabase → **Table Editor**, tienen que verse las 13 tablas del modelo (11 de V1 y 2 de V2)
   y `flyway_schema_history`.
4. **Recorrido completo con [`backend/recorrido.http`](../backend/recorrido.http):**
   1. Cambiar la primera línea por la URL pública:
      ```
      @base = https://fixture-backend.onrender.com
      ```
   2. Ejecutar **Registrar el organizador del caso real** (crea `ejemplo@fixture.test`).
   3. En Supabase → **SQL Editor**, pegar y ejecutar [`database/caso_real.sql`](../database/caso_real.sql).
      Al final muestra el id del torneo: ponerlo en `@torneo`.
   4. Seguir los pedidos en orden: login, sortear, generar (91 partidos), programar, validar, publicar,
      vista pública sin token, reabrir y republicar con motivo.

## 5. Backup de la base (pg_dump)

No dependan solo de lo que guarde el plan gratis: hagan un backup propio antes de cada cambio importante
y antes de cada fecha de torneo. Hace falta `pg_dump` en una PC (viene con PostgreSQL), de una **versión igual
o mayor** que la de Supabase (se ve en Supabase → **Project Settings → Infrastructure**).

```bash
# Con la URL del Session pooler. Pide la contraseña (DB_PASSWORD).
pg_dump "postgresql://postgres.<id>@aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require" \
  --schema=public --no-owner --no-privileges --format=custom \
  --file=fixture-$(date +%Y-%m-%d).dump
```

- `--schema=public`: solo nuestras tablas (incluida `flyway_schema_history`), no las internas de Supabase.
- `--format=custom`: archivo comprimido, se restaura con `pg_restore`.
- Guardar el archivo fuera de la PC (Drive, etc.): tiene hashes de contraseñas y teléfonos.

Restaurar en una base vacía (por ejemplo, un proyecto nuevo de Supabase o un PostgreSQL local):

```bash
pg_restore --no-owner --no-privileges \
  --dbname="postgresql://postgres.<id>@aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require" \
  fixture-2026-11-01.dump
```

Después de restaurar, al arrancar el backend Flyway ve las migraciones ya aplicadas y no toca nada.

## 6. Limitaciones del plan gratis

- **Arranque en frío (~1 minuto).** Render apaga el servicio gratis después de 15 minutos sin pedidos. El
  siguiente pedido lo despierta: arranca la JVM, Spring y Flyway, y esa primera respuesta tarda alrededor de
  **1 minuto**. Los siguientes son rápidos. Antes de una demo o del torneo, abrir `/api/salud` un rato antes.
- **Memoria: 512 MB.** La imagen limita la JVM (`-Xmx300m`). En la prueba local con 512 MB, el recorrido completo
  del caso real usó unos 200 MB.
- **Supabase pausa los proyectos gratis** después de un tiempo sin actividad. Si pasa, el backend no conecta
  (`/api/salud` falla): se reactiva desde el panel de Supabase con **Restore project**.
- **Sin disco persistente en Render:** no importa, todo se guarda en Supabase.

## 7. Si algo falla

| Síntoma en los logs de Render | Causa probable |
|---|---|
| `Could not resolve placeholder 'DB_URL'` (o `JWT_SECRETO`) | Falta esa variable en Render |
| `JWT_SECRETO tiene que tener al menos 32 caracteres` | El secreto es corto |
| `Network is unreachable` o `UnknownHostException ... supabase.co` | Se usó la conexión directa (IPv6): usar el Session pooler |
| `prepared statement "S_1" already exists` o errores raros de Flyway | Se usó el Transaction pooler (6543): usar el 5432 |
| `password authentication failed` | `DB_USER` sin el `.<id>` del proyecto, o contraseña equivocada |
| `Tenant or user not found` | `DB_USER` o región del host equivocados: copiarlos de nuevo de **Connect → Session pooler** |
| El servicio se reinicia solo al arrancar | Se quedó sin memoria o el health check no respondió: revisar que el Health Check Path sea `/api/salud` |

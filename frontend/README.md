# Frontend — Fixture de Torneos de Básquet

React + Vite en JavaScript, con `react-router-dom` y CSS propio (D54). Requiere Node 20.19+ o 22.12+.

## Pantallas

| Ruta | Pantalla | Login |
|---|---|---|
| `/login` | Ingresar y crear cuenta del organizador | No |
| `/torneos` | Mis torneos: lista y crear torneo | Sí |
| `/torneos/:id` | Detalle: parámetros (editables), Configuración (alta, editar y eliminar cada elemento, D68), Recorrido (sortear → generar → programar → validar) y Publicación (publicar, reabrir, historial) | Sí |
| `/torneo/:id` | Vista pública: equipos con su club, versión y motivo de la corrección | No |
| `/torneo/:id/equipo/:equipoId` | Vista pública: fixture del equipo (rival, club del rival, cancha, día y hora) | No |

Los enlaces de la vista pública son los que se comparten por WhatsApp (D57).

## Correrlo en local

Con el backend corriendo en `http://localhost:8080` y `CORS_ORIGENES=http://localhost:5173` (ver
[`backend/README.md`](../backend/README.md)):

```bash
cd frontend
npm ci          # instala las versiones exactas de package-lock.json
npm run dev     # http://localhost:5173
```

La URL del backend sale de `VITE_API_URL`: `.env.development` para `npm run dev` y `.env.production` para
`npm run build`. Para usar otra sin tocar esos archivos, crear `.env.development.local` (no se sube al repo).

## Compilar

```bash
npm run build   # deja el sitio en dist/
npm run preview # sirve dist/ en http://localhost:4173 para probarlo
```

Despliegue como sitio estático de Render: [`docs/deploy.md`](../docs/deploy.md), sección 8.

## Organización

```
src/
  api.js          Todos los pedidos a la API: token, errores del backend tal cual, 401 → login (D55)
  sesion.js       Token en localStorage dentro de try/catch (D55)
  formato.js      Fechas y horas para mostrar
  paginas/        Una por ruta (publico/ = vista sin login)
  componentes/    Piezas compartidas; torneo/ = pestañas del detalle, publico/ = versión y WhatsApp
  estilos.css     Estilos propios, pensados primero para el celular
```

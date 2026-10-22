// Cliente de la API. Todos los pedidos pasan por acá: agrega el token y traduce los errores del backend.
import { borrarSesion, leerToken } from './sesion.js';

const URL_API = (import.meta.env.VITE_API_URL ?? '').replace(/\/$/, '');

/** Error de la API con el mensaje y los detalles tal cual los manda el backend: {"error": "...", "detalles": [...]}. */
export class ErrorApi extends Error {
  constructor(mensaje, detalles = [], estado = 0) {
    super(mensaje);
    this.detalles = detalles;
    this.estado = estado;
  }
}

/**
 * Hace un pedido a la API y devuelve el cuerpo de la respuesta.
 * Si el backend responde 401 a un pedido con token, la sesión venció: se borra el token y se vuelve al login (D55).
 */
export async function pedir(ruta, { metodo = 'GET', cuerpo, autenticado = true } = {}) {
  const encabezados = {};
  if (cuerpo !== undefined) encabezados['Content-Type'] = 'application/json';
  const token = autenticado ? leerToken() : null;
  if (token) encabezados.Authorization = `Bearer ${token}`;

  let respuesta;
  try {
    respuesta = await fetch(URL_API + ruta, {
      method: metodo,
      headers: encabezados,
      body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
    });
  } catch {
    throw new ErrorApi('No se pudo conectar con el servidor. Revisá la conexión e intentá de nuevo.');
  }

  if (respuesta.status === 401 && autenticado) {
    borrarSesion();
    window.location.assign('/login?sesion=vencida');
    throw new ErrorApi('La sesión venció. Iniciá sesión de nuevo.', [], 401);
  }

  const texto = await respuesta.text();
  let datos = null;
  try {
    datos = texto ? JSON.parse(texto) : null;
  } catch {
    datos = null;
  }
  if (!respuesta.ok) {
    throw new ErrorApi(datos?.error ?? `El servidor respondió con un error (${respuesta.status}).`,
      datos?.detalles ?? [], respuesta.status);
  }
  return datos;
}

/** Atajos para los pedidos más comunes. */
export const api = {
  get: (ruta, opciones) => pedir(ruta, opciones),
  post: (ruta, cuerpo, opciones) => pedir(ruta, { ...opciones, metodo: 'POST', cuerpo }),
};

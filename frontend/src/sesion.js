// Sesión del organizador en localStorage (D55). Siempre dentro de try/catch: en modo privado o con el
// almacenamiento bloqueado, localStorage puede fallar, y la app tiene que seguir andando (sin recordar la sesión).

const CLAVE = 'fixture.sesion';

/** Copia en memoria, por si localStorage no está disponible. */
let sesionEnMemoria = null;

/** Guarda la respuesta del login: token, vencimiento y datos del organizador. */
export function guardarSesion({ token, expiraEn, organizador }) {
  try {
    localStorage.setItem(CLAVE, JSON.stringify({ token, expiraEn, organizador }));
  } catch {
    // Sin almacenamiento: la sesión dura mientras la pestaña esté abierta (ver sesionEnMemoria).
  }
  sesionEnMemoria = { token, expiraEn, organizador };
}

function leerSesion() {
  let sesion = sesionEnMemoria;
  try {
    const guardada = localStorage.getItem(CLAVE);
    if (guardada) sesion = JSON.parse(guardada);
  } catch {
    // Se usa la copia en memoria.
  }
  if (sesion && sesion.expiraEn && new Date(sesion.expiraEn) <= new Date()) {
    borrarSesion();
    return null;
  }
  return sesion;
}

/** El token si hay una sesión vigente; si no, null. */
export function leerToken() {
  return leerSesion()?.token ?? null;
}

export function leerOrganizador() {
  return leerSesion()?.organizador ?? null;
}

export function borrarSesion() {
  sesionEnMemoria = null;
  try {
    localStorage.removeItem(CLAVE);
  } catch {
    // Nada que borrar.
  }
}

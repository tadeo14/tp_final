// Formatos de fechas y horas para mostrar. Las fechas y horas de los partidos llegan del backend sin zona horaria
// (por ejemplo "2026-11-07T10:15:00"): se muestran tal cual, sin convertir.

/** "2026-11-07" → "sábado 7/11". */
export function dia(fechaIso) {
  const [anio, mes, d] = fechaIso.slice(0, 10).split('-').map(Number);
  const nombre = new Date(anio, mes - 1, d).toLocaleDateString('es-AR', { weekday: 'long' });
  return `${nombre} ${d}/${mes}`;
}

/** "2026-11-07T10:15:00" o "10:15:00" → "10:15". */
export function hora(valor) {
  const t = valor.includes('T') ? valor.split('T')[1] : valor;
  return t.slice(0, 5);
}

/** Fecha y hora de un instante (con zona, como la de una publicación), en la hora local del navegador. */
export function momento(instante) {
  return new Date(instante).toLocaleString('es-AR', { dateStyle: 'short', timeStyle: 'short' });
}

/** Agrupa una lista de partidos por día, en orden. */
export function porDia(partidos) {
  const grupos = new Map();
  for (const p of partidos) {
    const clave = p.inicio ? p.inicio.slice(0, 10) : 'sin-programar';
    if (!grupos.has(clave)) grupos.set(clave, []);
    grupos.get(clave).push(p);
  }
  return [...grupos.entries()];
}

/** "U9 Masculino - Zona B" → "Zona B" cuando la categoría y la rama ya se muestran en el título del grupo. */
export function zonaSinGrupo(zona, categoria, rama) {
  const prefijo = `${categoria} ${rama} - `;
  return zona.startsWith(prefijo) ? zona.slice(prefijo.length) : zona;
}

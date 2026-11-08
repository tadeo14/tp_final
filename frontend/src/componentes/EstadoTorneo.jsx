/** Etiqueta del estado del torneo: BORRADOR o PUBLICADO. */
export default function EstadoTorneo({ estado }) {
  return <span className={`estado estado-${estado}`}>{estado}</span>;
}

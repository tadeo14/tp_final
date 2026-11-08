import { Link } from 'react-router-dom';
import { leerToken } from '../../sesion.js';

/** Si el organizador tiene la sesión iniciada, un enlace para volver a la pantalla del torneo. Sin sesión, nada (D62). */
export default function VolverAlTorneo({ torneoId }) {
  if (!leerToken()) return null;
  return <p><Link to={`/torneos/${torneoId}`}>← Volver al torneo</Link></p>;
}

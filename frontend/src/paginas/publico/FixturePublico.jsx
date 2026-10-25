import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../../api.js';
import Cargando from '../../componentes/Cargando.jsx';
import Encabezado from '../../componentes/Encabezado.jsx';
import MensajeError from '../../componentes/MensajeError.jsx';
import CompartirWhatsApp from '../../componentes/publico/CompartirWhatsApp.jsx';
import VersionVigente from '../../componentes/publico/VersionVigente.jsx';
import VolverAlTorneo from '../../componentes/publico/VolverAlTorneo.jsx';
import { dia, hora } from '../../formato.js';

/** Pantalla 5, sin login: fixture de un equipo según la última versión publicada (D57). */
export default function FixturePublico() {
  const { id, equipoId } = useParams();
  const [datos, setDatos] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    setDatos(null);
    setError(null);
    api.get(`/api/publico/torneos/${id}/equipos/${equipoId}/fixture`, { autenticado: false })
      .then(setDatos).catch(setError);
  }, [id, equipoId]);

  return (
    <>
      <Encabezado publico />
      <main className="contenedor">
        <VolverAlTorneo torneoId={id} />
        <p><Link to={`/torneo/${id}`}>← Todos los equipos</Link></p>
        <MensajeError error={error} />
        {!datos && !error && <Cargando />}
        {datos && (
          <>
            <h1>{datos.torneo}</h1>
            <h2>{datos.equipo}</h2>
            <p className="nota">{datos.club} · {datos.zona}</p>
            <VersionVigente version={datos.version} />
            <ul className="lista">
              {datos.partidos.map((p) => (
                <li key={p.ronda} className="partido">
                  <p className="partido-cuando">{dia(p.inicio)} · {hora(p.inicio)} a {hora(p.fin)}</p>
                  <p className="partido-rival">vs <strong>{p.rival}</strong> <span className="nota">({p.clubRival})</span></p>
                  <p>📍 {p.sede} · {p.cancha}</p>
                  <p className="nota">Ronda {p.ronda} · {p.condicion}</p>
                </li>
              ))}
            </ul>
            {datos.rondasLibres.length > 0 && (
              <p className="nota">
                Fecha libre en la{datos.rondasLibres.length > 1 ? 's rondas' : ' ronda'} {datos.rondasLibres.join(', ')}.
              </p>
            )}
            <CompartirWhatsApp texto={`Fixture de ${datos.equipo} (${datos.club}) en ${datos.torneo}`} />
          </>
        )}
      </main>
    </>
  );
}

import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../../api.js';
import Cargando from '../../componentes/Cargando.jsx';
import Encabezado from '../../componentes/Encabezado.jsx';
import MensajeError from '../../componentes/MensajeError.jsx';
import CompartirWhatsApp from '../../componentes/publico/CompartirWhatsApp.jsx';
import VersionVigente from '../../componentes/publico/VersionVigente.jsx';
import VolverAlTorneo from '../../componentes/publico/VolverAlTorneo.jsx';
import { zonaSinGrupo } from '../../formato.js';

/** Pantalla 5, sin login: equipos de la última versión publicada, agrupados por categoría y rama (D57). */
export default function EquiposPublicos() {
  const { id } = useParams();
  const [datos, setDatos] = useState(null);
  const [error, setError] = useState(null);
  const [busqueda, setBusqueda] = useState('');

  useEffect(() => {
    api.get(`/api/publico/torneos/${id}/equipos`, { autenticado: false }).then(setDatos).catch(setError);
  }, [id]);

  const grupos = useMemo(() => {
    if (!datos) return [];
    const texto = busqueda.trim().toLowerCase();
    const resultado = new Map();
    for (const e of datos.equipos) {
      if (texto && !`${e.nombre} ${e.club}`.toLowerCase().includes(texto)) continue;
      const clave = `${e.categoria} · ${e.rama}`;
      if (!resultado.has(clave)) resultado.set(clave, []);
      resultado.get(clave).push(e);
    }
    return [...resultado.entries()];
  }, [datos, busqueda]);

  return (
    <>
      <Encabezado publico />
      <main className="contenedor">
        <VolverAlTorneo torneoId={id} />
        <MensajeError error={error} />
        {!datos && !error && <Cargando />}
        {datos && (
          <>
            <h1>{datos.torneo}</h1>
            <VersionVigente version={datos.version} />
            <p>Elegí tu equipo para ver contra quién juega, dónde y a qué hora.</p>
            <label className="buscador">
              Buscar por equipo o club
              <input type="search" value={busqueda} onChange={(e) => setBusqueda(e.target.value)}
                placeholder="Escribí el nombre" />
            </label>
            {grupos.length === 0 && <p className="vacio">No hay equipos con ese nombre.</p>}
            {grupos.map(([grupo, equipos]) => (
              <section key={grupo}>
                <h2>{grupo}</h2>
                <ul className="lista lista-enlaces">
                  {equipos.map((e) => (
                    <li key={e.id}>
                      <Link to={`/torneo/${id}/equipo/${e.id}`}>
                        <span>
                          <strong>{e.nombre}</strong>
                          <br />
                          <span className="nota">{e.club} · {zonaSinGrupo(e.zona, e.categoria, e.rama)}</span>
                        </span>
                        <span aria-hidden="true">›</span>
                      </Link>
                    </li>
                  ))}
                </ul>
              </section>
            ))}
            <CompartirWhatsApp texto={`Fixture de ${datos.torneo}`} />
          </>
        )}
      </main>
    </>
  );
}

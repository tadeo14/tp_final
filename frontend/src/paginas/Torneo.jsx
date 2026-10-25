import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api.js';
import Cargando from '../componentes/Cargando.jsx';
import Encabezado from '../componentes/Encabezado.jsx';
import EstadoTorneo from '../componentes/EstadoTorneo.jsx';
import MensajeError from '../componentes/MensajeError.jsx';
import Configuracion from '../componentes/torneo/Configuracion.jsx';
import ParametrosTorneo from '../componentes/torneo/ParametrosTorneo.jsx';
import Publicacion from '../componentes/torneo/Publicacion.jsx';
import Recorrido from '../componentes/torneo/Recorrido.jsx';

const PESTANIAS = [
  { id: 'configuracion', nombre: 'Configuración' },
  { id: 'recorrido', nombre: 'Recorrido' },
  { id: 'publicacion', nombre: 'Publicación' },
];

/** Pantallas 3 y 4: detalle del torneo (configuración, recorrido y publicación). Siempre muestra el estado. */
export default function Torneo() {
  const { id } = useParams();
  const [datos, setDatos] = useState(null);
  const [error, setError] = useState(null);
  const [pestania, setPestania] = useState('configuracion');

  const recargar = useCallback(async () => {
    const base = `/api/torneos/${id}`;
    const [torneo, configuracion, equipos, fixture, publicaciones] = await Promise.all([
      api.get(base), api.get(`${base}/configuracion`), api.get(`${base}/equipos`), api.get(`${base}/fixture`),
      api.get(`${base}/publicaciones`),
    ]);
    setDatos({ torneo, configuracion, equipos, fixture, publicaciones });
  }, [id]);

  useEffect(() => {
    recargar().catch(setError);
  }, [recargar]);

  const torneo = datos?.torneo;
  const publicado = torneo?.estado === 'PUBLICADO';

  return (
    <>
      <Encabezado />
      <main className="contenedor">
        <p><Link to="/torneos">← Mis torneos</Link></p>
        <MensajeError error={error} />
        {!datos && !error && <Cargando />}
        {torneo && (
          <>
            <div className="titulo-torneo">
              <h1>{torneo.nombre}</h1>
              <EstadoTorneo estado={torneo.estado} />
            </div>
            <ParametrosTorneo torneo={torneo} publicado={publicado} recargar={recargar} />

            <div className="pestanias" role="tablist">
              {PESTANIAS.map((p) => (
                <button key={p.id} type="button" role="tab" aria-selected={pestania === p.id}
                  className={pestania === p.id ? 'pestania activa' : 'pestania'} onClick={() => setPestania(p.id)}>
                  {p.nombre}
                </button>
              ))}
            </div>

            {pestania === 'configuracion' && (
              <Configuracion torneoId={id} configuracion={datos.configuracion} equipos={datos.equipos}
                publicado={publicado} recargar={recargar} />
            )}
            {pestania === 'recorrido' && (
              <Recorrido torneoId={id} publicado={publicado} fixture={datos.fixture} recargar={recargar} />
            )}
            {pestania === 'publicacion' && (
              <Publicacion torneoId={id} publicado={publicado} publicaciones={datos.publicaciones} recargar={recargar} />
            )}
          </>
        )}
      </main>
    </>
  );
}

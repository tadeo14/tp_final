import { useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api.js';
import { momento } from '../../formato.js';
import MensajeError from '../MensajeError.jsx';
import ConfirmacionEnLinea from './ConfirmacionEnLinea.jsx';

/**
 * Publicación del fixture (D46, D47): publicar, reabrir para corregir e historial de versiones.
 * Desde la versión 2 el motivo es obligatorio. El público siempre ve la última versión publicada.
 */
export default function Publicacion({ torneoId, publicado, publicaciones, recargar }) {
  const base = `/api/torneos/${torneoId}`;
  const [motivo, setMotivo] = useState('');
  const [enCurso, setEnCurso] = useState(false);
  const [error, setError] = useState(null);
  const [aviso, setAviso] = useState(null);

  const proxima = publicaciones.length + 1;
  const pideMotivo = proxima > 1;

  async function ejecutar(accion, textoAviso) {
    setEnCurso(true);
    setError(null);
    setAviso(null);
    try {
      await accion();
      await recargar(); // primero se actualiza el estado del torneo, después se avisa
      setAviso(textoAviso);
    } catch (e) {
      setError(e);
    } finally {
      setEnCurso(false);
    }
  }

  function publicar(evento) {
    evento.preventDefault();
    const cuerpo = pideMotivo ? { motivo } : undefined;
    ejecutar(async () => {
      await api.post(`${base}/publicar`, cuerpo);
      setMotivo('');
    }, `Se publicó la versión ${proxima}.`);
  }

  // Reabrir se pregunta dentro de la página, sin ventanas del navegador (D68).
  const [preguntandoReabrir, setPreguntandoReabrir] = useState(false);

  async function reabrir() {
    await ejecutar(() => api.post(`${base}/reabrir`), 'El torneo volvió a BORRADOR: ya podés corregirlo.');
    setPreguntandoReabrir(false);
  }

  return (
    <>
      <section className="tarjeta">
        <h2>{publicado ? 'Torneo publicado' : `Publicar la versión ${proxima}`}</h2>
        {publicado ? (
          <>
            <p>
              El público ve la versión {publicaciones.length}. Para corregir algo (por ejemplo, si se da de baja un equipo o una
              cancha queda fuera de servicio), reabrí el torneo, corregí y volvé a publicar.
            </p>
            {preguntandoReabrir ? (
              <ConfirmacionEnLinea textoConfirmar="Sí, reabrir" textoEnCurso="Reabriendo…" enCurso={enCurso}
                texto={`El torneo vuelve a BORRADOR para corregirlo. El público va a seguir viendo la versión ${publicaciones.length} `
                  + 'hasta que publiques la corrección. ¿Seguimos?'}
                alConfirmar={reabrir} alCancelar={() => setPreguntandoReabrir(false)} />
            ) : (
              <button type="button" className="boton-secundario" disabled={enCurso} onClick={() => setPreguntandoReabrir(true)}>
                Reabrir para corregir
              </button>
            )}
          </>
        ) : (
          <form className="formulario" onSubmit={publicar}>
            <p className="nota">
              Antes de publicar se validan las reglas R1–R8. Si falla alguna, el fixture no se publica.
              {publicaciones.length > 0 && ` Mientras tanto, el público sigue viendo la versión ${publicaciones.length}.`}
            </p>
            {pideMotivo && (
              <label>
                Motivo de la corrección
                <textarea value={motivo} onChange={(e) => setMotivo(e.target.value)} required maxLength={500} rows={3}
                  placeholder="Por ejemplo: se dio de baja un equipo" />
                <span className="ayuda">Obligatorio desde la versión 2: lo ve el público junto al fixture.</span>
              </label>
            )}
            <button type="submit" disabled={enCurso}>{enCurso ? 'Publicando…' : `Publicar versión ${proxima}`}</button>
          </form>
        )}
        <MensajeError error={error} />
        {aviso && <div className="aviso aviso-ok"><p>{aviso}</p></div>}
        {publicaciones.length > 0 && (
          <p><Link to={`/torneo/${torneoId}`}>Ver la vista pública →</Link></p>
        )}
      </section>

      <section className="tarjeta">
        <h2>Historial de versiones</h2>
        {publicaciones.length === 0 ? <p className="vacio">Todavía no se publicó.</p> : (
          <ul className="lista">
            {publicaciones.map((v) => (
              <li key={v.numero}>
                <strong>Versión {v.numero}</strong> · {momento(v.publicadaEn)}
                {v.numero === publicaciones[0].numero && <span className="nota"> (la que ve el público)</span>}
                <br />
                <span className="nota">{v.motivo ?? 'Primera publicación'}</span>
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  );
}

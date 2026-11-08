import { useState } from 'react';
import { api, conConfirmacion } from '../../api.js';
import MensajeError from '../MensajeError.jsx';
import ConfirmarInvalidacion from './ConfirmarInvalidacion.jsx';

/**
 * "Proponer zonas" (D69): con el máximo de equipos por zona, el backend calcula cuántas zonas armar en cada
 * categoría y rama. Primero muestra la vista previa (no guarda nada) y después "Crear zonas" las guarda.
 * Si ya hay zonas, el backend pide confirmación para reemplazarlas y se confirma dentro de la página.
 * Las zonas son solo la estructura: los equipos los reparte el sorteo.
 */
export default function ProponerZonas({ torneoId, recargar }) {
  const ruta = `/api/torneos/${torneoId}/zonas/propuesta`;
  const [abierto, setAbierto] = useState(false);
  const [maximo, setMaximo] = useState('5'); // se precarga 5, que reproduce el caso real; es editable (regla 1)
  const [propuesta, setPropuesta] = useState(null);
  const [enCurso, setEnCurso] = useState(false);
  const [error, setError] = useState(null);
  const [aviso, setAviso] = useState(null);

  const cuerpo = () => ({ maximoEquiposPorZona: Number(maximo) });

  function cerrar() {
    setAbierto(false);
    setPropuesta(null);
    setError(null);
  }

  async function verPropuesta(evento) {
    evento.preventDefault();
    setEnCurso(true);
    setError(null);
    setPropuesta(null);
    try {
      setPropuesta(await api.post(ruta, cuerpo()));
    } catch (e) {
      setError(e);
    } finally {
      setEnCurso(false);
    }
  }

  async function crearZonas(confirmar) {
    setEnCurso(true);
    setError(null);
    try {
      const creadas = await api.post(conConfirmacion(`${ruta}/crear`, confirmar), cuerpo());
      await recargar();
      cerrar();
      setAviso(`${creadas.length === 1 ? 'Se creó 1 zona' : `Se crearon ${creadas.length} zonas`}. Ahora podés sortear en la pestaña Recorrido.`);
    } catch (e) {
      setError(e);
    } finally {
      setEnCurso(false);
    }
  }

  if (!abierto) {
    return (
      <>
        {aviso && <div className="aviso aviso-ok"><p>{aviso}</p></div>}
        <button type="button" className="boton-secundario" onClick={() => { setAviso(null); setAbierto(true); }}>
          Proponer zonas
        </button>
      </>
    );
  }

  return (
    <div className="subtarjeta propuesta-zonas">
      <h3>Proponer zonas</h3>
      <p className="nota">
        Se arman tantas zonas como hagan falta para no pasar el máximo, con los equipos repartidos lo más parejo
        posible. Después las podés editar o eliminar.
      </p>
      <form className="formulario" onSubmit={verPropuesta}>
        <fieldset className="fila" disabled={enCurso}>
          <label>
            Máximo de equipos por zona
            <input type="number" inputMode="numeric" min={3} value={maximo} required
              onChange={(e) => { setMaximo(e.target.value); setPropuesta(null); setError(null); }} />
          </label>
        </fieldset>
        {!propuesta && (
          <div className="botones">
            <button type="submit" disabled={enCurso}>{enCurso ? 'Calculando…' : 'Ver propuesta'}</button>
            <button type="button" className="boton-secundario" onClick={cerrar}>Cancelar</button>
          </div>
        )}
      </form>

      {propuesta && (
        <>
          {propuesta.grupos.length === 0 ? <p className="vacio">No hay equipos inscriptos.</p> : (
            <div className="tabla-scroll">
              <table>
                <thead>
                  <tr><th>Categoría</th><th>Rama</th><th>Equipos</th><th>Zonas (equipos)</th></tr>
                </thead>
                <tbody>
                  {propuesta.grupos.map((g) => (
                    <tr key={`${g.categoriaId}-${g.ramaId}`}>
                      <td>{g.categoria}</td>
                      <td>{g.rama}</td>
                      <td>{g.equipos}</td>
                      <td>
                        {g.zonas.length === 0 ? <span className="nota">{g.aviso}</span> : (
                          <>
                            <ul className="zonas-propuestas">
                              {g.zonas.map((z) => <li key={z.nombre}>{z.nombre} ({z.equipos})</li>)}
                            </ul>
                            {g.zonasActuales.length > 0 && (
                              <span className="nota reemplaza">Reemplaza {g.zonasActuales.join(', ')}</span>
                            )}
                          </>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          <p>Total: {propuesta.totalZonas} {propuesta.totalZonas === 1 ? 'zona' : 'zonas'}.</p>
          {propuesta.reemplazaZonas && !error?.requiereConfirmacion && (
            <div className="aviso aviso-advertencia">
              <p>Algunas categorías ya tienen zonas: al crear las propuestas se van a reemplazar.</p>
            </div>
          )}
          {!error?.requiereConfirmacion && (
            <div className="botones">
              <button type="button" disabled={enCurso || propuesta.totalZonas === 0} onClick={() => crearZonas(false)}>
                {enCurso ? 'Creando…' : 'Crear zonas'}
              </button>
              <button type="button" className="boton-secundario" disabled={enCurso} onClick={cerrar}>Cancelar</button>
            </div>
          )}
        </>
      )}

      {error?.requiereConfirmacion ? (
        <ConfirmarInvalidacion error={error} enCurso={enCurso} alConfirmar={() => crearZonas(true)}
          alCancelar={() => setError(null)} />
      ) : (
        <MensajeError error={error} />
      )}
    </div>
  );
}

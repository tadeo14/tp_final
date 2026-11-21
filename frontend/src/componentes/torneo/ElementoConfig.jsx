import { useState } from 'react';
import MensajeError from '../MensajeError.jsx';
import ConfirmacionEnLinea from './ConfirmacionEnLinea.jsx';
import ConfirmarInvalidacion from './ConfirmarInvalidacion.jsx';
import FormularioAlta from './FormularioAlta.jsx';

/**
 * Un elemento de la configuración (una franja, una cancha, un equipo…) con sus botones Editar y Eliminar (D66).
 * - Editar abre el mismo formulario del alta con los datos cargados. `formulario.inicial` puede ser un objeto o una
 *   función que los pide al backend (el equipo trae responsable y teléfono aparte, D67).
 * - Eliminar pregunta dentro de la página, sin ventanas del navegador.
 * Si el backend pide confirmación porque el cambio invalida el fixture, se muestra su mensaje y se puede confirmar.
 * Con el torneo publicado no hay botones: hay que reabrirlo (D47).
 */
export default function ElementoConfig({ children, descripcion, publicado, formulario, eliminar }) {
  const [modo, setModo] = useState('ver'); // ver | editar | eliminar
  const [inicial, setInicial] = useState(null);
  const [enCurso, setEnCurso] = useState(false);
  const [error, setError] = useState(null);

  function volver() {
    setModo('ver');
    setError(null);
  }

  async function abrirEdicion() {
    setError(null);
    if (typeof formulario.inicial !== 'function') {
      setInicial(formulario.inicial);
      setModo('editar');
      return;
    }
    setEnCurso(true);
    try {
      setInicial(await formulario.inicial());
      setModo('editar');
    } catch (e) {
      setError(e);
    } finally {
      setEnCurso(false);
    }
  }

  async function confirmarEliminacion(confirmar) {
    setEnCurso(true);
    setError(null);
    try {
      await eliminar(confirmar); // al terminar, la lista se recarga y este elemento desaparece
    } catch (e) {
      setError(e);
      setEnCurso(false);
    }
  }

  if (modo === 'editar') {
    return (
      <li className="elemento elemento-editando">
        <FormularioAlta inicial={inicial} campos={formulario.campos} textoBoton="Guardar" limpiar={false}
          alCancelar={volver}
          enviar={async (valores, confirmar) => {
            await formulario.guardar(valores, confirmar);
            setModo('ver');
          }} />
      </li>
    );
  }

  return (
    <li className="elemento">
      <div className="elemento-fila">
        <div className="elemento-texto">{children}</div>
        {!publicado && modo === 'ver' && (
          <div className="acciones-elemento">
            {formulario && (
              <button type="button" className="boton-chico boton-secundario" disabled={enCurso} onClick={abrirEdicion}>
                Editar
              </button>
            )}
            <button type="button" className="boton-chico boton-secundario" disabled={enCurso} onClick={() => setModo('eliminar')}>
              Eliminar
            </button>
          </div>
        )}
      </div>
      {modo === 'eliminar' && !error && (
        <ConfirmacionEnLinea texto={`¿Eliminar ${descripcion}?`} textoConfirmar="Sí, eliminar" textoEnCurso="Eliminando…"
          enCurso={enCurso} alConfirmar={() => confirmarEliminacion(false)} alCancelar={volver} />
      )}
      {error?.requiereConfirmacion ? (
        <ConfirmarInvalidacion error={error} enCurso={enCurso} alConfirmar={() => confirmarEliminacion(true)}
          alCancelar={volver} />
      ) : error && (
        <>
          <MensajeError error={error} />
          <button type="button" className="boton-chico boton-secundario" onClick={volver}>Cerrar</button>
        </>
      )}
    </li>
  );
}

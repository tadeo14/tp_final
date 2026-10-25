import { useState } from 'react';
import MensajeError from '../MensajeError.jsx';
import ConfirmarInvalidacion from './ConfirmarInvalidacion.jsx';

/**
 * Formulario chico para dar de alta o editar algo (una franja, una sede, un equipo…).
 * Maneja el estado de los campos, el envío y el error del backend; cada sección define sus campos.
 * `campos(valores, cambiar)` dibuja los inputs; `enviar(valores, confirmar)` hace el pedido (si falla, se muestra
 * el error). Si el backend pide confirmación (D66), se muestra su mensaje y, al confirmar, se repite con confirmar=true.
 * Para editar: `alCancelar` agrega el botón Cancelar y `limpiar={false}` deja los valores después de guardar.
 */
export default function FormularioAlta({
  titulo, inicial, campos, enviar, deshabilitado, textoBoton = 'Agregar', alCancelar, limpiar = true,
}) {
  const [valores, setValores] = useState(inicial);
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);

  function cambiar(campo, valor) {
    setValores((anteriores) => ({ ...anteriores, [campo]: valor }));
  }

  async function mandar(confirmar) {
    setEnviando(true);
    setError(null);
    try {
      await enviar(valores, confirmar);
      if (limpiar) setValores(inicial);
    } catch (e) {
      setError(e);
    } finally {
      setEnviando(false);
    }
  }

  function alEnviar(evento) {
    evento.preventDefault();
    mandar(false);
  }

  const pideConfirmacion = error?.requiereConfirmacion;

  return (
    <form className="formulario formulario-alta" onSubmit={alEnviar}>
      {titulo && <h3>{titulo}</h3>}
      <fieldset disabled={deshabilitado || enviando || pideConfirmacion}>
        {campos(valores, cambiar)}
        {!pideConfirmacion && <MensajeError error={error} />}
        <div className="botones">
          <button type="submit">{enviando ? 'Guardando…' : textoBoton}</button>
          {alCancelar && <button type="button" className="boton-secundario" onClick={alCancelar}>Cancelar</button>}
        </div>
      </fieldset>
      {pideConfirmacion && (
        <ConfirmarInvalidacion error={error} enCurso={enviando} alConfirmar={() => mandar(true)}
          alCancelar={() => setError(null)} />
      )}
    </form>
  );
}

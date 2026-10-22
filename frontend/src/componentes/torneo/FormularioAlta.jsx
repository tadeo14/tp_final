import { useState } from 'react';
import MensajeError from '../MensajeError.jsx';

/**
 * Formulario chico para dar de alta algo (una franja, una sede, un equipo…).
 * Maneja el estado de los campos, el envío y el error del backend; cada sección define sus campos.
 * `campos(valores, cambiar)` dibuja los inputs; `enviar(valores)` hace el pedido (si falla, se muestra el error).
 */
export default function FormularioAlta({ titulo, inicial, campos, enviar, deshabilitado, textoBoton = 'Agregar' }) {
  const [valores, setValores] = useState(inicial);
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);

  function cambiar(campo, valor) {
    setValores((anteriores) => ({ ...anteriores, [campo]: valor }));
  }

  async function alEnviar(evento) {
    evento.preventDefault();
    setEnviando(true);
    setError(null);
    try {
      await enviar(valores);
      setValores(inicial);
    } catch (e) {
      setError(e);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form className="formulario formulario-alta" onSubmit={alEnviar}>
      {titulo && <h3>{titulo}</h3>}
      <fieldset disabled={deshabilitado || enviando}>
        {campos(valores, cambiar)}
        <MensajeError error={error} />
        <button type="submit">{enviando ? 'Guardando…' : textoBoton}</button>
      </fieldset>
    </form>
  );
}

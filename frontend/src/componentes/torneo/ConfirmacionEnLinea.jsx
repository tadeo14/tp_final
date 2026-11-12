/**
 * Pregunta de confirmación dentro de la página, sin ventanas del navegador (D68): un aviso con el texto y los
 * botones para confirmar o cancelar. La usan Eliminar, Reabrir, Volver a sortear y la confirmación del backend.
 */
export default function ConfirmacionEnLinea({ texto, textoConfirmar, textoEnCurso = 'Un momento…', enCurso, alConfirmar, alCancelar }) {
  return (
    <div className="aviso aviso-advertencia" role="alert">
      <p>{texto}</p>
      <div className="botones">
        <button type="button" className="boton-peligro" disabled={enCurso} onClick={alConfirmar}>
          {enCurso ? textoEnCurso : textoConfirmar}
        </button>
        <button type="button" className="boton-secundario" disabled={enCurso} onClick={alCancelar}>Cancelar</button>
      </div>
    </div>
  );
}

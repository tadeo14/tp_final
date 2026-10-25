/**
 * El backend avisó que el cambio invalida el fixture (409 con requiereConfirmacion, D66): muestra su mensaje tal cual
 * y deja confirmar o cancelar, dentro de la página.
 */
export default function ConfirmarInvalidacion({ error, enCurso, alConfirmar, alCancelar }) {
  return (
    <div className="aviso aviso-advertencia" role="alert">
      <p>{error.message}</p>
      <div className="botones">
        <button type="button" className="boton-peligro" disabled={enCurso} onClick={alConfirmar}>
          {enCurso ? 'Un momento…' : 'Confirmar el cambio'}
        </button>
        <button type="button" className="boton-secundario" disabled={enCurso} onClick={alCancelar}>Cancelar</button>
      </div>
    </div>
  );
}

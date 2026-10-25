import ConfirmacionEnLinea from './ConfirmacionEnLinea.jsx';

/**
 * El backend avisó que el cambio invalida el fixture (409 con requiereConfirmacion, D66): muestra su mensaje tal cual
 * y deja confirmar o cancelar, dentro de la página.
 */
export default function ConfirmarInvalidacion({ error, enCurso, alConfirmar, alCancelar }) {
  return (
    <ConfirmacionEnLinea texto={error.message} textoConfirmar="Confirmar el cambio" enCurso={enCurso}
      alConfirmar={alConfirmar} alCancelar={alCancelar} />
  );
}

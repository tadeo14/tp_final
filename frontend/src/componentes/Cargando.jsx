import { useEffect, useState } from 'react';

/**
 * Indicador de carga. Si tarda, avisa que el servidor puede estar "despertando": en el plan gratis de Render
 * el primer pedido después de un rato sin uso tarda alrededor de un minuto.
 */
export default function Cargando({ texto = 'Cargando…' }) {
  const [demora, setDemora] = useState(false);
  useEffect(() => {
    const temporizador = setTimeout(() => setDemora(true), 5000);
    return () => clearTimeout(temporizador);
  }, []);
  return (
    <div className="cargando" role="status">
      <span className="ruedita" aria-hidden="true" />
      <span>{texto}</span>
      {demora && <p className="nota">El servidor se está despertando: puede tardar hasta un minuto.</p>}
    </div>
  );
}

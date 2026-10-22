import { Link, useNavigate } from 'react-router-dom';
import { borrarSesion, leerOrganizador } from '../sesion.js';

/**
 * Barra superior. Para el organizador: nombre de la app, quién está conectado y salir.
 * En la vista pública ({@code publico}) solo muestra el nombre, sin enlaces a las pantallas del organizador.
 */
export default function Encabezado({ publico = false }) {
  const navegar = useNavigate();
  const organizador = publico ? null : leerOrganizador();

  function salir() {
    borrarSesion();
    navegar('/login');
  }

  return (
    <header className="encabezado">
      {publico ? <span className="marca">🏀 Fixture</span> : <Link to="/torneos" className="marca">🏀 Fixture</Link>}
      {organizador && (
        <div className="usuario">
          <span className="nombre-usuario">{organizador.nombre}</span>
          <button type="button" className="boton-chico boton-secundario" onClick={salir}>Salir</button>
        </div>
      )}
    </header>
  );
}

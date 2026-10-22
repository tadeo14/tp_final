import { Link, useNavigate } from 'react-router-dom';
import { borrarSesion, leerOrganizador } from '../sesion.js';

/** Barra superior del organizador: nombre de la app, quién está conectado y salir. */
export default function Encabezado() {
  const navegar = useNavigate();
  const organizador = leerOrganizador();

  function salir() {
    borrarSesion();
    navegar('/login');
  }

  return (
    <header className="encabezado">
      <Link to="/torneos" className="marca">🏀 Fixture</Link>
      {organizador && (
        <div className="usuario">
          <span className="nombre-usuario">{organizador.nombre}</span>
          <button type="button" className="boton-chico boton-secundario" onClick={salir}>Salir</button>
        </div>
      )}
    </header>
  );
}

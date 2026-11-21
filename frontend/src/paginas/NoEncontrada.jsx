import { Link } from 'react-router-dom';

export default function NoEncontrada() {
  return (
    <main className="contenedor contenedor-angosto">
      <h1>No encontramos esta página</h1>
      <p><Link to="/">Volver al inicio</Link></p>
    </main>
  );
}

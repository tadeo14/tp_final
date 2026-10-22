import { useState } from 'react';
import { Navigate, useNavigate, useSearchParams } from 'react-router-dom';
import { api } from '../api.js';
import MensajeError from '../componentes/MensajeError.jsx';
import { guardarSesion, leerToken } from '../sesion.js';

/** Pantalla 1: ingreso y registro del organizador. */
export default function Login() {
  const navegar = useNavigate();
  const [parametros] = useSearchParams();
  const [modo, setModo] = useState('ingresar');
  const [nombre, setNombre] = useState('');
  const [email, setEmail] = useState('');
  const [contrasenia, setContrasenia] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);

  if (leerToken()) return <Navigate to="/torneos" replace />;

  async function enviar(evento) {
    evento.preventDefault();
    setEnviando(true);
    setError(null);
    try {
      if (modo === 'registrarse') {
        await api.post('/api/auth/registro', { nombre, email, contrasenia }, { autenticado: false });
      }
      const sesion = await api.post('/api/auth/login', { email, contrasenia }, { autenticado: false });
      guardarSesion(sesion);
      navegar('/torneos');
    } catch (e) {
      setError(e);
    } finally {
      setEnviando(false);
    }
  }

  function cambiarModo(nuevo) {
    setModo(nuevo);
    setError(null);
  }

  return (
    <main className="contenedor contenedor-angosto">
      <h1 className="titulo-app">🏀 Fixture de Básquet</h1>
      <p className="subtitulo">Organizá tu torneo: zonas, canchas y horarios sin superposiciones.</p>

      {parametros.get('sesion') === 'vencida' && (
        <div className="aviso aviso-info">Tu sesión venció. Volvé a ingresar.</div>
      )}

      <div className="pestanias" role="tablist">
        <button type="button" role="tab" aria-selected={modo === 'ingresar'}
          className={modo === 'ingresar' ? 'pestania activa' : 'pestania'} onClick={() => cambiarModo('ingresar')}>
          Ingresar
        </button>
        <button type="button" role="tab" aria-selected={modo === 'registrarse'}
          className={modo === 'registrarse' ? 'pestania activa' : 'pestania'} onClick={() => cambiarModo('registrarse')}>
          Crear cuenta
        </button>
      </div>

      <form className="tarjeta formulario" onSubmit={enviar}>
        {modo === 'registrarse' && (
          <label>
            Nombre
            <input value={nombre} onChange={(e) => setNombre(e.target.value)} required autoComplete="name" />
          </label>
        )}
        <label>
          Email
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoComplete="email" />
        </label>
        <label>
          Contraseña
          <input type="password" value={contrasenia} onChange={(e) => setContrasenia(e.target.value)} required
            minLength={modo === 'registrarse' ? 8 : undefined}
            autoComplete={modo === 'registrarse' ? 'new-password' : 'current-password'} />
          {modo === 'registrarse' && <span className="ayuda">Al menos 8 caracteres.</span>}
        </label>
        <MensajeError error={error} />
        <button type="submit" disabled={enviando}>
          {enviando ? 'Un momento…' : modo === 'ingresar' ? 'Ingresar' : 'Crear cuenta e ingresar'}
        </button>
      </form>
    </main>
  );
}

import { Navigate } from 'react-router-dom';
import { leerToken } from '../sesion.js';

/** Las pantallas del organizador exigen sesión: sin token, al login. */
export default function RutaProtegida({ children }) {
  return leerToken() ? children : <Navigate to="/login" replace />;
}

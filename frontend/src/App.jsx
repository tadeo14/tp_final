import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import RutaProtegida from './componentes/RutaProtegida.jsx';
import Login from './paginas/Login.jsx';
import MisTorneos from './paginas/MisTorneos.jsx';
import NoEncontrada from './paginas/NoEncontrada.jsx';
import { leerToken } from './sesion.js';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to={leerToken() ? '/torneos' : '/login'} replace />} />
        <Route path="/login" element={<Login />} />
        <Route path="/torneos" element={<RutaProtegida><MisTorneos /></RutaProtegida>} />
        <Route path="*" element={<NoEncontrada />} />
      </Routes>
    </BrowserRouter>
  );
}

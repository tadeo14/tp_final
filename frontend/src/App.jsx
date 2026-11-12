import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import RutaProtegida from './componentes/RutaProtegida.jsx';
import Login from './paginas/Login.jsx';
import MisTorneos from './paginas/MisTorneos.jsx';
import NoEncontrada from './paginas/NoEncontrada.jsx';
import Torneo from './paginas/Torneo.jsx';
import EquiposPublicos from './paginas/publico/EquiposPublicos.jsx';
import FixturePublico from './paginas/publico/FixturePublico.jsx';
import { leerToken } from './sesion.js';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to={leerToken() ? '/torneos' : '/login'} replace />} />
        <Route path="/login" element={<Login />} />
        <Route path="/torneos" element={<RutaProtegida><MisTorneos /></RutaProtegida>} />
        <Route path="/torneos/:id" element={<RutaProtegida><Torneo /></RutaProtegida>} />
        {/* Vista pública, sin login: es el enlace que se comparte por WhatsApp (D57). */}
        <Route path="/torneo/:id" element={<EquiposPublicos />} />
        <Route path="/torneo/:id/equipo/:equipoId" element={<FixturePublico />} />
        <Route path="*" element={<NoEncontrada />} />
      </Routes>
    </BrowserRouter>
  );
}

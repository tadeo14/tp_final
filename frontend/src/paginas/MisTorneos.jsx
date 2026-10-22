import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api.js';
import Cargando from '../componentes/Cargando.jsx';
import Encabezado from '../componentes/Encabezado.jsx';
import EstadoTorneo from '../componentes/EstadoTorneo.jsx';
import MensajeError from '../componentes/MensajeError.jsx';

const TORNEO_VACIO = { nombre: '', turnosCadaMin: '', recambioMin: '', descansoMinimoMin: '' };

/** Pantalla 2: los torneos del organizador y el formulario para crear uno nuevo. */
export default function MisTorneos() {
  const [torneos, setTorneos] = useState(null);
  const [errorLista, setErrorLista] = useState(null);
  const [nuevo, setNuevo] = useState(TORNEO_VACIO);
  const [creando, setCreando] = useState(false);
  const [errorNuevo, setErrorNuevo] = useState(null);
  const [mostrarFormulario, setMostrarFormulario] = useState(false);

  useEffect(() => {
    api.get('/api/torneos').then(setTorneos).catch(setErrorLista);
  }, []);

  function cambiar(campo, valor) {
    setNuevo((anterior) => ({ ...anterior, [campo]: valor }));
  }

  async function crear(evento) {
    evento.preventDefault();
    setCreando(true);
    setErrorNuevo(null);
    try {
      const creado = await api.post('/api/torneos', {
        nombre: nuevo.nombre,
        turnosCadaMin: Number(nuevo.turnosCadaMin),
        recambioMin: Number(nuevo.recambioMin),
        descansoMinimoMin: Number(nuevo.descansoMinimoMin),
      });
      setTorneos((anteriores) => [...(anteriores ?? []), creado]);
      setNuevo(TORNEO_VACIO);
      setMostrarFormulario(false);
    } catch (e) {
      setErrorNuevo(e);
    } finally {
      setCreando(false);
    }
  }

  return (
    <>
      <Encabezado />
      <main className="contenedor">
        <h1>Mis torneos</h1>

        <MensajeError error={errorLista} />
        {!torneos && !errorLista && <Cargando />}
        {torneos?.length === 0 && <p className="vacio">Todavía no tenés torneos. Creá el primero.</p>}
        {torneos?.length > 0 && (
          <ul className="lista lista-enlaces">
            {torneos.map((t) => (
              <li key={t.id}>
                <Link to={`/torneos/${t.id}`}>
                  <span>
                    <strong>{t.nombre}</strong>
                    <br />
                    <span className="nota">
                      Turnos cada {t.turnosCadaMin} min · recambio {t.recambioMin} min · descanso {t.descansoMinimoMin} min
                    </span>
                  </span>
                  <EstadoTorneo estado={t.estado} />
                </Link>
              </li>
            ))}
          </ul>
        )}

        {!mostrarFormulario ? (
          <button type="button" onClick={() => setMostrarFormulario(true)}>+ Nuevo torneo</button>
        ) : (
          <form className="tarjeta formulario" onSubmit={crear}>
            <h2>Nuevo torneo</h2>
            <label>
              Nombre
              <input value={nuevo.nombre} onChange={(e) => cambiar('nombre', e.target.value)} required />
            </label>
            <div className="fila">
              <label>
                Turnos cada (min)
                <input type="number" min="1" inputMode="numeric" value={nuevo.turnosCadaMin}
                  onChange={(e) => cambiar('turnosCadaMin', e.target.value)} required />
                <span className="ayuda">Los partidos empiezan en múltiplos de este valor.</span>
              </label>
              <label>
                Recambio (min)
                <input type="number" min="0" inputMode="numeric" value={nuevo.recambioMin}
                  onChange={(e) => cambiar('recambioMin', e.target.value)} required />
                <span className="ayuda">Salida y entrada de equipos entre partidos.</span>
              </label>
              <label>
                Descanso mínimo (min)
                <input type="number" min="0" inputMode="numeric" value={nuevo.descansoMinimoMin}
                  onChange={(e) => cambiar('descansoMinimoMin', e.target.value)} required />
                <span className="ayuda">Entre dos partidos del mismo equipo.</span>
              </label>
            </div>
            <MensajeError error={errorNuevo} />
            <div className="botones">
              <button type="submit" disabled={creando}>{creando ? 'Creando…' : 'Crear torneo'}</button>
              <button type="button" className="boton-secundario" onClick={() => setMostrarFormulario(false)}>Cancelar</button>
            </div>
          </form>
        )}
      </main>
    </>
  );
}

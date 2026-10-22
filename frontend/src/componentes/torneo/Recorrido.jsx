import { useState } from 'react';
import { api } from '../../api.js';
import MensajeError from '../MensajeError.jsx';
import TablaFixture from './TablaFixture.jsx';

/**
 * Recorrido del fixture: sortear → generar → programar → validar. Cada paso muestra su resultado o el error
 * del backend tal cual (por ejemplo "No existe una programación válida con la configuración actual").
 */
export default function Recorrido({ torneoId, publicado, fixture, recargar }) {
  const base = `/api/torneos/${torneoId}`;
  const [enCurso, setEnCurso] = useState(null);
  const [resultados, setResultados] = useState({});
  const [errores, setErrores] = useState({});

  async function ejecutar(paso, accion) {
    setEnCurso(paso);
    setErrores((e) => ({ ...e, [paso]: null }));
    setResultados((r) => ({ ...r, [paso]: null }));
    try {
      const resultado = await accion();
      setResultados((r) => ({ ...r, [paso]: resultado }));
      await recargar();
    } catch (e) {
      setErrores((anteriores) => ({ ...anteriores, [paso]: e }));
    } finally {
      setEnCurso(null);
    }
  }

  function sortear() {
    if (fixture.length > 0 && !window.confirm('Volver a sortear borra los partidos generados. ¿Seguimos?')) return;
    ejecutar('sorteo', () => api.post(`${base}/sorteo`));
  }

  const ocupado = enCurso !== null;
  const sorteo = resultados.sorteo;
  const validacion = resultados.validacion;

  return (
    <>
      {publicado && (
        <div className="aviso aviso-info">
          El torneo está publicado: no se puede volver a sortear, generar ni programar. Para corregir, reabrilo en la
          pestaña Publicación.
        </div>
      )}

      <section className="tarjeta paso">
        <h2>1. Sortear zonas</h2>
        <p className="nota">Reparte al azar los equipos de cada categoría y rama entre sus zonas.</p>
        <button type="button" disabled={publicado || ocupado} onClick={sortear}>
          {enCurso === 'sorteo' ? 'Sorteando…' : 'Sortear'}
        </button>
        <MensajeError error={errores.sorteo} />
        {sorteo && (
          <div className="aviso aviso-ok">
            <p>Zonas sorteadas.</p>
            <ul>{sorteo.map((z) => <li key={z.zona}><strong>{z.zona}:</strong> {z.equipos.join(', ')}</li>)}</ul>
          </div>
        )}
      </section>

      <section className="tarjeta paso">
        <h2>2. Generar enfrentamientos</h2>
        <p className="nota">Todos contra todos dentro de cada zona.</p>
        <button type="button" disabled={publicado || ocupado}
          onClick={() => ejecutar('generar', () => api.post(`${base}/fixture/generar`))}>
          {enCurso === 'generar' ? 'Generando…' : 'Generar'}
        </button>
        <MensajeError error={errores.generar} />
        {resultados.generar && (
          <div className="aviso aviso-ok"><p>Se generaron {resultados.generar.cantidadPartidos} partidos.</p></div>
        )}
      </section>

      <section className="tarjeta paso">
        <h2>3. Programar canchas y horarios</h2>
        <p className="nota">Asigna a cada partido una cancha y un horario respetando las reglas R1–R8.</p>
        <button type="button" disabled={publicado || ocupado}
          onClick={() => ejecutar('programar', () => api.post(`${base}/fixture/programar`))}>
          {enCurso === 'programar' ? 'Programando…' : 'Programar'}
        </button>
        <MensajeError error={errores.programar} />
        {resultados.programar && (
          <div className="aviso aviso-ok"><p>Se programaron {resultados.programar.length} partidos.</p></div>
        )}
      </section>

      <section className="tarjeta paso">
        <h2>4. Validar</h2>
        <p className="nota">Revisa que el fixture cumpla todas las reglas obligatorias R1–R8.</p>
        <button type="button" disabled={ocupado} onClick={() => ejecutar('validacion', () => api.get(`${base}/validacion`))}>
          {enCurso === 'validacion' ? 'Validando…' : 'Validar'}
        </button>
        <MensajeError error={errores.validacion} />
        {validacion?.valido && <div className="aviso aviso-ok"><p>El fixture cumple todas las reglas R1–R8.</p></div>}
        {validacion && !validacion.valido && (
          <div className="aviso aviso-error">
            <p>El fixture no cumple {validacion.violaciones.length === 1 ? '1 regla' : `${validacion.violaciones.length} reglas`}:</p>
            <ul>
              {validacion.violaciones.map((v, i) => (
                <li key={i}><strong>{v.regla} – {v.descripcion}:</strong> {v.detalle}</li>
              ))}
            </ul>
          </div>
        )}
      </section>

      <section className="tarjeta">
        <h2>Fixture ({fixture.length} partidos)</h2>
        <TablaFixture partidos={fixture} />
      </section>
    </>
  );
}

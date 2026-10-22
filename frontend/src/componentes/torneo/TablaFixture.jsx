import { dia, hora, porDia } from '../../formato.js';

/** Fixture completo del organizador, agrupado por día. Los partidos sin programar van al final. */
export default function TablaFixture({ partidos }) {
  if (!partidos?.length) return <p className="vacio">Todavía no hay partidos generados.</p>;
  return porDia(partidos).map(([clave, delDia]) => (
    <section key={clave}>
      <h3>{clave === 'sin-programar' ? 'Sin cancha ni horario' : dia(clave)}</h3>
      <div className="tabla-scroll">
        <table>
          <thead>
            <tr>
              <th>Hora</th>
              <th>Cancha</th>
              <th>Partido</th>
              <th>Zona</th>
            </tr>
          </thead>
          <tbody>
            {[...delDia].sort((a, b) => (a.inicio ?? '').localeCompare(b.inicio ?? '')).map((p) => (
              <tr key={p.id}>
                <td>{p.inicio ? `${hora(p.inicio)}–${hora(p.fin)}` : '—'}</td>
                <td>{p.cancha ? `${p.sede} · ${p.cancha}` : '—'}</td>
                <td>{p.local} vs {p.visitante}</td>
                <td>{p.zona} · R{p.ronda}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  ));
}

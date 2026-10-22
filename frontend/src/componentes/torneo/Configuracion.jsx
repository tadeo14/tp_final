import { api } from '../../api.js';
import { dia, hora } from '../../formato.js';
import FormularioAlta from './FormularioAlta.jsx';

/**
 * Configuración del torneo: franjas, sedes y canchas, categorías, ramas, clubes, zonas y equipos.
 * Muestra lo cargado (GET /configuracion y /equipos) y un formulario para agregar cada cosa.
 * Con el torneo PUBLICADO los formularios quedan deshabilitados: hay que reabrirlo (D39, D47).
 */
export default function Configuracion({ torneoId, configuracion, equipos, publicado, recargar }) {
  const base = `/api/torneos/${torneoId}`;
  const { franjas, sedes, categorias, ramas, clubes, zonas } = configuracion;

  async function agregar(ruta, cuerpo) {
    await api.post(ruta, cuerpo);
    await recargar();
  }

  return (
    <>
      {publicado && (
        <div className="aviso aviso-info">
          El torneo está publicado: no se puede cambiar la configuración. Para corregir algo, reabrilo en la pestaña
          Publicación.
        </div>
      )}

      <section className="tarjeta">
        <h2>Días y franjas horarias</h2>
        {franjas.length === 0 ? <p className="vacio">Sin franjas.</p> : (
          <ul className="lista-simple">
            {franjas.map((f) => <li key={f.id}>{dia(f.fecha)}: de {hora(f.desde)} a {hora(f.hasta)}</li>)}
          </ul>
        )}
        <FormularioAlta deshabilitado={publicado} inicial={{ fecha: '', desde: '', hasta: '' }}
          enviar={(v) => agregar(`${base}/franjas`, v)}
          campos={(v, cambiar) => (
            <div className="fila">
              <label>Día<input type="date" value={v.fecha} onChange={(e) => cambiar('fecha', e.target.value)} required /></label>
              <label>Desde<input type="time" value={v.desde} onChange={(e) => cambiar('desde', e.target.value)} required /></label>
              <label>Hasta<input type="time" value={v.hasta} onChange={(e) => cambiar('hasta', e.target.value)} required /></label>
            </div>
          )} />
      </section>

      <section className="tarjeta">
        <h2>Sedes y canchas</h2>
        {sedes.length === 0 && <p className="vacio">Sin sedes.</p>}
        {sedes.map((s) => (
          <div key={s.id} className="subtarjeta">
            <strong>{s.nombre}</strong>{s.direccion && <span className="nota"> · {s.direccion}</span>}
            {s.canchas.length === 0 ? <p className="vacio">Sin canchas.</p> : (
              <ul className="lista-simple">
                {s.canchas.map((c) => <li key={c.id}>{c.nombre}{!c.habilitada && ' (no habilitada)'}</li>)}
              </ul>
            )}
            <FormularioAlta deshabilitado={publicado} textoBoton="Agregar cancha" inicial={{ nombre: '', habilitada: true }}
              enviar={(v) => agregar(`/api/sedes/${s.id}/canchas`, v)}
              campos={(v, cambiar) => (
                <div className="fila">
                  <label>Cancha<input value={v.nombre} onChange={(e) => cambiar('nombre', e.target.value)} required /></label>
                  <label className="casilla">
                    <input type="checkbox" checked={v.habilitada} onChange={(e) => cambiar('habilitada', e.target.checked)} />
                    Habilitada
                  </label>
                </div>
              )} />
          </div>
        ))}
        <FormularioAlta titulo="Nueva sede" deshabilitado={publicado} textoBoton="Agregar sede" inicial={{ nombre: '', direccion: '' }}
          enviar={(v) => agregar(`${base}/sedes`, { nombre: v.nombre, direccion: v.direccion || null })}
          campos={(v, cambiar) => (
            <div className="fila">
              <label>Nombre<input value={v.nombre} onChange={(e) => cambiar('nombre', e.target.value)} required /></label>
              <label>Dirección (opcional)<input value={v.direccion} onChange={(e) => cambiar('direccion', e.target.value)} /></label>
            </div>
          )} />
      </section>

      <section className="tarjeta">
        <h2>Categorías</h2>
        {categorias.length === 0 ? <p className="vacio">Sin categorías.</p> : (
          <ul className="lista-simple">
            {categorias.map((c) => (
              <li key={c.id}>
                <strong>{c.nombre}</strong>: {c.cantidadCuartos} cuartos, {c.minutosNetos} min de juego, {c.pausasMin} min de
                pausas, {c.margenDetencionesMin} min de margen, hasta {c.maxPartidosDia} partidos por día
              </li>
            ))}
          </ul>
        )}
        <FormularioAlta deshabilitado={publicado}
          inicial={{ nombre: '', minutosNetos: '', cantidadCuartos: '', pausasMin: '', margenDetencionesMin: '', maxPartidosDia: '' }}
          enviar={(v) => agregar(`${base}/categorias`, {
            nombre: v.nombre, minutosNetos: Number(v.minutosNetos), cantidadCuartos: Number(v.cantidadCuartos),
            pausasMin: Number(v.pausasMin), margenDetencionesMin: Number(v.margenDetencionesMin),
            maxPartidosDia: Number(v.maxPartidosDia),
          })}
          campos={(v, cambiar) => (
            <div className="fila">
              <label>Nombre<input value={v.nombre} onChange={(e) => cambiar('nombre', e.target.value)} required /></label>
              <Numero etiqueta="Minutos de juego" campo="minutosNetos" min={1} v={v} cambiar={cambiar} />
              <Numero etiqueta="Cuartos" campo="cantidadCuartos" min={1} v={v} cambiar={cambiar} />
              <Numero etiqueta="Pausas (min, en total)" campo="pausasMin" min={0} v={v} cambiar={cambiar} />
              <Numero etiqueta="Margen por detenciones (min)" campo="margenDetencionesMin" min={0} v={v} cambiar={cambiar} />
              <Numero etiqueta="Máx. partidos por día" campo="maxPartidosDia" min={1} v={v} cambiar={cambiar} />
            </div>
          )} />
      </section>

      <section className="tarjeta">
        <h2>Ramas</h2>
        {ramas.length === 0 ? <p className="vacio">Sin ramas.</p> : <p>{ramas.map((r) => r.nombre).join(' · ')}</p>}
        <FormularioAlta deshabilitado={publicado} inicial={{ nombre: '' }} enviar={(v) => agregar(`${base}/ramas`, v)}
          campos={(v, cambiar) => (
            <label>Nombre<input value={v.nombre} onChange={(e) => cambiar('nombre', e.target.value)} required /></label>
          )} />
      </section>

      <section className="tarjeta">
        <h2>Clubes</h2>
        {clubes.length === 0 ? <p className="vacio">Sin clubes.</p> : <p>{clubes.map((c) => c.nombre).join(' · ')}</p>}
        <FormularioAlta deshabilitado={publicado} inicial={{ nombre: '' }} enviar={(v) => agregar(`${base}/clubes`, v)}
          campos={(v, cambiar) => (
            <label>Nombre<input value={v.nombre} onChange={(e) => cambiar('nombre', e.target.value)} required /></label>
          )} />
      </section>

      <section className="tarjeta">
        <h2>Zonas</h2>
        <p className="nota">Cada categoría y rama se divide en zonas; el sorteo reparte sus equipos entre ellas.</p>
        {zonas.length === 0 ? <p className="vacio">Sin zonas.</p> : (
          <ul className="lista-simple">{zonas.map((z) => <li key={z.id}>{z.etiqueta}</li>)}</ul>
        )}
        <FormularioAlta deshabilitado={publicado} inicial={{ categoriaId: '', ramaId: '', nombre: '' }}
          enviar={(v) => agregar(`${base}/zonas`, { categoriaId: Number(v.categoriaId), ramaId: Number(v.ramaId), nombre: v.nombre })}
          campos={(v, cambiar) => (
            <div className="fila">
              <Opciones etiqueta="Categoría" campo="categoriaId" opciones={categorias} v={v} cambiar={cambiar} />
              <Opciones etiqueta="Rama" campo="ramaId" opciones={ramas} v={v} cambiar={cambiar} />
              <label>Nombre<input value={v.nombre} onChange={(e) => cambiar('nombre', e.target.value)} required placeholder="Zona A" /></label>
            </div>
          )} />
      </section>

      <section className="tarjeta">
        <h2>Equipos ({equipos.length})</h2>
        {equipos.length === 0 ? <p className="vacio">Sin equipos.</p> : (
          <ul className="lista-simple">
            {equipos.map((e) => (
              <li key={e.id}>
                <strong>{e.nombre}</strong> · {e.club} · {e.categoria} {e.rama}{e.zona && ` · ${e.zona}`}
              </li>
            ))}
          </ul>
        )}
        <FormularioAlta deshabilitado={publicado}
          inicial={{ clubId: '', categoriaId: '', ramaId: '', nombre: '', responsableNombre: '', responsableTelefono: '' }}
          enviar={(v) => agregar(`${base}/equipos`, {
            ...v, clubId: Number(v.clubId), categoriaId: Number(v.categoriaId), ramaId: Number(v.ramaId),
          })}
          campos={(v, cambiar) => (
            <div className="fila">
              <label>Nombre<input value={v.nombre} onChange={(e) => cambiar('nombre', e.target.value)} required /></label>
              <Opciones etiqueta="Club" campo="clubId" opciones={clubes} v={v} cambiar={cambiar} />
              <Opciones etiqueta="Categoría" campo="categoriaId" opciones={categorias} v={v} cambiar={cambiar} />
              <Opciones etiqueta="Rama" campo="ramaId" opciones={ramas} v={v} cambiar={cambiar} />
              <label>Responsable<input value={v.responsableNombre} onChange={(e) => cambiar('responsableNombre', e.target.value)} required /></label>
              <label>Teléfono del responsable
                <input type="tel" value={v.responsableTelefono} onChange={(e) => cambiar('responsableTelefono', e.target.value)} required />
              </label>
            </div>
          )} />
      </section>
    </>
  );
}

function Numero({ etiqueta, campo, min, v, cambiar }) {
  return (
    <label>
      {etiqueta}
      <input type="number" inputMode="numeric" min={min} value={v[campo]} onChange={(e) => cambiar(campo, e.target.value)} required />
    </label>
  );
}

function Opciones({ etiqueta, campo, opciones, v, cambiar }) {
  return (
    <label>
      {etiqueta}
      <select value={v[campo]} onChange={(e) => cambiar(campo, e.target.value)} required>
        <option value="">Elegir…</option>
        {opciones.map((o) => <option key={o.id} value={o.id}>{o.nombre}</option>)}
      </select>
    </label>
  );
}

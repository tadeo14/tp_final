import { api, conConfirmacion } from '../../api.js';
import { dia, hora } from '../../formato.js';
import ElementoConfig from './ElementoConfig.jsx';
import FormularioAlta from './FormularioAlta.jsx';
import ProponerZonas from './ProponerZonas.jsx';

/**
 * Configuración del torneo: franjas, sedes y canchas, categorías, ramas, clubes, zonas y equipos.
 * Muestra lo cargado (GET /configuracion y /equipos), un formulario para agregar cada cosa y, en cada elemento,
 * Editar (el mismo formulario del alta, con los datos cargados) y Eliminar (D66).
 * Con el torneo PUBLICADO no se puede cambiar nada: hay que reabrirlo (D47).
 */
export default function Configuracion({ torneoId, configuracion, equipos, publicado, recargar }) {
  const base = `/api/torneos/${torneoId}`;
  const { franjas, sedes, categorias, ramas, clubes, zonas } = configuracion;

  async function agregar(ruta, cuerpo) {
    await api.post(ruta, cuerpo);
    await recargar();
  }

  async function guardar(ruta, cuerpo, confirmar) {
    await api.put(conConfirmacion(ruta, confirmar), cuerpo);
    await recargar();
  }

  async function eliminar(ruta, confirmar) {
    await api.borrar(conConfirmacion(ruta, confirmar));
    await recargar();
  }

  /** Lo común a cada elemento: el texto que se muestra, la descripción para "¿Eliminar…?" y las rutas. */
  function elemento({ id, descripcion, ruta, inicial, campos, cuerpo, contenido }) {
    return (
      <ElementoConfig key={id} descripcion={descripcion} publicado={publicado}
        formulario={campos && { inicial, campos, guardar: (v, confirmar) => guardar(ruta, cuerpo(v), confirmar) }}
        eliminar={(confirmar) => eliminar(ruta, confirmar)}>
        {contenido}
      </ElementoConfig>
    );
  }

  const camposEquipo = (v, cambiar) => (
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
  );
  const cuerpoEquipo = (v) => ({
    ...v, clubId: Number(v.clubId), categoriaId: Number(v.categoriaId), ramaId: Number(v.ramaId),
  });

  const camposZona = (v, cambiar) => (
    <div className="fila">
      <Opciones etiqueta="Categoría" campo="categoriaId" opciones={categorias} v={v} cambiar={cambiar} />
      <Opciones etiqueta="Rama" campo="ramaId" opciones={ramas} v={v} cambiar={cambiar} />
      <label>Nombre<input value={v.nombre} onChange={(e) => cambiar('nombre', e.target.value)} required placeholder="Zona A" /></label>
    </div>
  );
  const cuerpoZona = (v) => ({ categoriaId: Number(v.categoriaId), ramaId: Number(v.ramaId), nombre: v.nombre });

  return (
    <>
      {publicado && (
        <div className="aviso aviso-info">
          Para modificar la configuración, reabrí el torneo (en la pestaña Publicación).
        </div>
      )}

      <section className="tarjeta">
        <h2>Días y franjas horarias</h2>
        {franjas.length === 0 ? <p className="vacio">Sin franjas.</p> : (
          <ul className="lista-config">
            {franjas.map((f) => elemento({
              id: f.id, descripcion: `la franja del ${dia(f.fecha)}`, ruta: `${base}/franjas/${f.id}`,
              inicial: { fecha: f.fecha, desde: hora(f.desde), hasta: hora(f.hasta) }, campos: camposFranja,
              cuerpo: (v) => v, contenido: <>{dia(f.fecha)}: de {hora(f.desde)} a {hora(f.hasta)}</>,
            }))}
          </ul>
        )}
        <FormularioAlta deshabilitado={publicado} inicial={{ fecha: '', desde: '', hasta: '' }}
          enviar={(v) => agregar(`${base}/franjas`, v)} campos={camposFranja} />
      </section>

      <section className="tarjeta">
        <h2>Sedes y canchas</h2>
        {sedes.length === 0 && <p className="vacio">Sin sedes.</p>}
        {sedes.map((s) => (
          <div key={s.id} className="subtarjeta">
            <ul className="lista-config">
              {elemento({
                id: s.id, descripcion: `la sede ${s.nombre} y sus canchas`, ruta: `${base}/sedes/${s.id}`,
                inicial: { nombre: s.nombre, direccion: s.direccion ?? '' }, campos: camposSede, cuerpo: cuerpoSede,
                contenido: <><strong>{s.nombre}</strong>{s.direccion && <span className="nota"> · {s.direccion}</span>}</>,
              })}
            </ul>
            {s.canchas.length === 0 ? <p className="vacio">Sin canchas.</p> : (
              <ul className="lista-config lista-canchas">
                {s.canchas.map((c) => elemento({
                  id: c.id, descripcion: `la cancha ${c.nombre} de ${s.nombre}`, ruta: `/api/sedes/${s.id}/canchas/${c.id}`,
                  inicial: { nombre: c.nombre, habilitada: c.habilitada }, campos: camposCancha, cuerpo: (v) => v,
                  contenido: <>{c.nombre}{!c.habilitada && <span className="nota"> (no habilitada)</span>}</>,
                }))}
              </ul>
            )}
            <FormularioAlta deshabilitado={publicado} textoBoton="Agregar cancha" inicial={{ nombre: '', habilitada: true }}
              enviar={(v) => agregar(`/api/sedes/${s.id}/canchas`, v)} campos={camposCancha} />
          </div>
        ))}
        <FormularioAlta titulo="Nueva sede" deshabilitado={publicado} textoBoton="Agregar sede" inicial={{ nombre: '', direccion: '' }}
          enviar={(v) => agregar(`${base}/sedes`, cuerpoSede(v))} campos={camposSede} />
      </section>

      <section className="tarjeta">
        <h2>Categorías</h2>
        {categorias.length === 0 ? <p className="vacio">Sin categorías.</p> : (
          <ul className="lista-config">
            {categorias.map((c) => elemento({
              id: c.id, descripcion: `la categoría ${c.nombre}`, ruta: `${base}/categorias/${c.id}`,
              inicial: {
                nombre: c.nombre, minutosNetos: String(c.minutosNetos), cantidadCuartos: String(c.cantidadCuartos),
                pausasMin: String(c.pausasMin), margenDetencionesMin: String(c.margenDetencionesMin),
                maxPartidosDia: String(c.maxPartidosDia),
              },
              campos: camposCategoria, cuerpo: cuerpoCategoria,
              contenido: (
                <>
                  <strong>{c.nombre}</strong>: {c.cantidadCuartos} cuartos, {c.minutosNetos} min de juego, {c.pausasMin} min
                  de pausas, {c.margenDetencionesMin} min de margen, hasta {c.maxPartidosDia} partidos por día
                </>
              ),
            }))}
          </ul>
        )}
        <FormularioAlta deshabilitado={publicado}
          inicial={{ nombre: '', minutosNetos: '', cantidadCuartos: '', pausasMin: '', margenDetencionesMin: '', maxPartidosDia: '' }}
          enviar={(v) => agregar(`${base}/categorias`, cuerpoCategoria(v))} campos={camposCategoria} />
      </section>

      <section className="tarjeta">
        <h2>Ramas</h2>
        {ramas.length === 0 ? <p className="vacio">Sin ramas.</p> : (
          <ul className="lista-config">
            {ramas.map((r) => elemento({
              id: r.id, descripcion: `la rama ${r.nombre}`, ruta: `${base}/ramas/${r.id}`, inicial: { nombre: r.nombre },
              campos: camposNombre, cuerpo: (v) => v, contenido: r.nombre,
            }))}
          </ul>
        )}
        <FormularioAlta deshabilitado={publicado} inicial={{ nombre: '' }} enviar={(v) => agregar(`${base}/ramas`, v)}
          campos={camposNombre} />
      </section>

      <section className="tarjeta">
        <h2>Clubes</h2>
        {clubes.length === 0 ? <p className="vacio">Sin clubes.</p> : (
          <ul className="lista-config">
            {clubes.map((c) => elemento({
              id: c.id, descripcion: `el club ${c.nombre}`, ruta: `${base}/clubes/${c.id}`, inicial: { nombre: c.nombre },
              campos: camposNombre, cuerpo: (v) => v, contenido: c.nombre,
            }))}
          </ul>
        )}
        <FormularioAlta deshabilitado={publicado} inicial={{ nombre: '' }} enviar={(v) => agregar(`${base}/clubes`, v)}
          campos={camposNombre} />
      </section>

      <section className="tarjeta">
        <h2>Zonas</h2>
        <p className="nota">Cada categoría y rama se divide en zonas; el sorteo reparte sus equipos entre ellas.</p>
        {!publicado && <ProponerZonas torneoId={torneoId} recargar={recargar} />}
        {zonas.length === 0 ? <p className="vacio">Sin zonas.</p> : (
          <ul className="lista-config">
            {zonas.map((z) => elemento({
              id: z.id, descripcion: `la zona ${z.etiqueta}`, ruta: `${base}/zonas/${z.id}`,
              inicial: { categoriaId: String(z.categoriaId), ramaId: String(z.ramaId), nombre: z.nombre },
              campos: camposZona, cuerpo: cuerpoZona, contenido: z.etiqueta,
            }))}
          </ul>
        )}
        <FormularioAlta deshabilitado={publicado} inicial={{ categoriaId: '', ramaId: '', nombre: '' }}
          enviar={(v) => agregar(`${base}/zonas`, cuerpoZona(v))} campos={camposZona} />
      </section>

      <section className="tarjeta">
        <h2>Equipos ({equipos.length})</h2>
        {equipos.length === 0 ? <p className="vacio">Sin equipos.</p> : (
          <ul className="lista-config">
            {equipos.map((e) => elemento({
              id: e.id, descripcion: `el equipo ${e.nombre} (${e.categoria} ${e.rama})`, ruta: `${base}/equipos/${e.id}`,
              // Responsable y teléfono no vienen en la lista: se piden al abrir el formulario (D67).
              inicial: async () => {
                const d = await api.get(`${base}/equipos/${e.id}`);
                return {
                  nombre: d.nombre, clubId: String(d.clubId), categoriaId: String(d.categoriaId), ramaId: String(d.ramaId),
                  responsableNombre: d.responsableNombre, responsableTelefono: d.responsableTelefono,
                };
              },
              campos: camposEquipo, cuerpo: cuerpoEquipo,
              contenido: <><strong>{e.nombre}</strong> · {e.club} · {e.categoria} {e.rama}{e.zona && ` · ${e.zona}`}</>,
            }))}
          </ul>
        )}
        <FormularioAlta deshabilitado={publicado}
          inicial={{ clubId: '', categoriaId: '', ramaId: '', nombre: '', responsableNombre: '', responsableTelefono: '' }}
          enviar={(v) => agregar(`${base}/equipos`, cuerpoEquipo(v))} campos={camposEquipo} />
      </section>
    </>
  );
}

// Campos de cada formulario: los mismos para el alta y para editar.

function camposFranja(v, cambiar) {
  return (
    <div className="fila">
      <label>Día<input type="date" value={v.fecha} onChange={(e) => cambiar('fecha', e.target.value)} required /></label>
      <label>Desde<input type="time" value={v.desde} onChange={(e) => cambiar('desde', e.target.value)} required /></label>
      <label>Hasta<input type="time" value={v.hasta} onChange={(e) => cambiar('hasta', e.target.value)} required /></label>
    </div>
  );
}

function camposSede(v, cambiar) {
  return (
    <div className="fila">
      <label>Nombre<input value={v.nombre} onChange={(e) => cambiar('nombre', e.target.value)} required /></label>
      <label>Dirección (opcional)<input value={v.direccion} onChange={(e) => cambiar('direccion', e.target.value)} /></label>
    </div>
  );
}

function cuerpoSede(v) {
  return { nombre: v.nombre, direccion: v.direccion || null };
}

function camposCancha(v, cambiar) {
  return (
    <div className="fila">
      <label>Cancha<input value={v.nombre} onChange={(e) => cambiar('nombre', e.target.value)} required /></label>
      <label className="casilla">
        <input type="checkbox" checked={v.habilitada} onChange={(e) => cambiar('habilitada', e.target.checked)} />
        Habilitada
      </label>
    </div>
  );
}

function camposCategoria(v, cambiar) {
  return (
    <div className="fila">
      <label>Nombre<input value={v.nombre} onChange={(e) => cambiar('nombre', e.target.value)} required /></label>
      <Numero etiqueta="Minutos de juego" campo="minutosNetos" min={1} v={v} cambiar={cambiar} />
      <Numero etiqueta="Cuartos" campo="cantidadCuartos" min={1} v={v} cambiar={cambiar} />
      <Numero etiqueta="Pausas (min, en total)" campo="pausasMin" min={0} v={v} cambiar={cambiar} />
      <Numero etiqueta="Margen por detenciones (min)" campo="margenDetencionesMin" min={0} v={v} cambiar={cambiar} />
      <Numero etiqueta="Máx. partidos por día" campo="maxPartidosDia" min={1} v={v} cambiar={cambiar} />
    </div>
  );
}

function cuerpoCategoria(v) {
  return {
    nombre: v.nombre, minutosNetos: Number(v.minutosNetos), cantidadCuartos: Number(v.cantidadCuartos),
    pausasMin: Number(v.pausasMin), margenDetencionesMin: Number(v.margenDetencionesMin),
    maxPartidosDia: Number(v.maxPartidosDia),
  };
}

function camposNombre(v, cambiar) {
  return <label>Nombre<input value={v.nombre} onChange={(e) => cambiar('nombre', e.target.value)} required /></label>;
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

import { useState } from 'react';
import { api, conConfirmacion } from '../../api.js';
import FormularioAlta from './FormularioAlta.jsx';

/**
 * Parámetros generales del torneo (nombre, turnos cada X minutos, recambio y descanso) con el botón para editarlos
 * (D66). Cambiar los tiempos con partidos programados pide confirmación. Con el torneo publicado no se editan.
 */
export default function ParametrosTorneo({ torneo, publicado, recargar }) {
  const [editando, setEditando] = useState(false);

  if (editando) {
    return (
      <div className="tarjeta elemento-editando">
        <FormularioAlta titulo="Parámetros del torneo" textoBoton="Guardar" limpiar={false} alCancelar={() => setEditando(false)}
          inicial={{
            nombre: torneo.nombre, turnosCadaMin: String(torneo.turnosCadaMin), recambioMin: String(torneo.recambioMin),
            descansoMinimoMin: String(torneo.descansoMinimoMin),
          }}
          enviar={async (v, confirmar) => {
            await api.put(conConfirmacion(`/api/torneos/${torneo.id}`, confirmar), {
              nombre: v.nombre, turnosCadaMin: Number(v.turnosCadaMin), recambioMin: Number(v.recambioMin),
              descansoMinimoMin: Number(v.descansoMinimoMin),
            });
            await recargar();
            setEditando(false);
          }}
          campos={(v, cambiar) => (
            <>
              <label>Nombre<input value={v.nombre} onChange={(e) => cambiar('nombre', e.target.value)} required /></label>
              <div className="fila">
                <Numero etiqueta="Turnos cada (min)" campo="turnosCadaMin" min={1} v={v} cambiar={cambiar} />
                <Numero etiqueta="Recambio (min)" campo="recambioMin" min={0} v={v} cambiar={cambiar} />
                <Numero etiqueta="Descanso mínimo (min)" campo="descansoMinimoMin" min={0} v={v} cambiar={cambiar} />
              </div>
            </>
          )} />
      </div>
    );
  }

  return (
    <div className="parametros">
      <p className="nota">
        Turnos cada {torneo.turnosCadaMin} min · recambio {torneo.recambioMin} min · descanso mínimo{' '}
        {torneo.descansoMinimoMin} min
      </p>
      {!publicado && (
        <button type="button" className="boton-chico boton-secundario" onClick={() => setEditando(true)}>
          Editar parámetros
        </button>
      )}
    </div>
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

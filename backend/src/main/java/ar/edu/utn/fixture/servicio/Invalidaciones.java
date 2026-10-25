package ar.edu.utn.fixture.servicio;

import org.springframework.stereotype.Component;

import ar.edu.utn.fixture.repositorio.EquipoRepositorio;
import ar.edu.utn.fixture.repositorio.PartidoRepositorio;

/**
 * Qué pasa con el fixture cuando se edita o se borra la configuración (D66). Nunca quedan partidos que contradigan
 * la configuración (regla 5): o el organizador confirma y se borran, o el cambio no se hace.
 *
 * <p>Confirmar siempre borra todos los partidos del torneo: volver a generarlos da lo mismo, porque la generación
 * es determinista a partir del sorteo (D27). Las publicaciones (la foto, D46) no se tocan nunca.
 */
@Component
class Invalidaciones {

    private final PartidoRepositorio partidos;
    private final EquipoRepositorio equipos;

    Invalidaciones(PartidoRepositorio partidos, EquipoRepositorio equipos) {
        this.partidos = partidos;
        this.equipos = equipos;
    }

    /**
     * Cambios que afectan solo horarios y canchas (franjas, parámetros de tiempo, cancha usada...): piden
     * confirmación solo si hay partidos programados. Los enfrentamientos sin programar siguen sirviendo.
     */
    void siHayProgramados(Long torneoId, boolean confirmar, String cambio) {
        long programados = partidos.contarProgramadosDelTorneo(torneoId);
        if (programados == 0) {
            return;
        }
        if (!confirmar) {
            throw new ConfirmacionRequeridaException(cambio + " invalida el fixture: hay " + programados
                    + " partidos programados. Si confirmás, se borran los partidos generados y hay que volver a "
                    + "generarlos y programarlos.");
        }
        partidos.borrarDelTorneo(torneoId);
    }

    /** Cambios que afectan quién juega contra quién (dar de baja un equipo): piden confirmación si hay partidos. */
    void siHayPartidos(Long torneoId, boolean confirmar, String cambio) {
        long generados = partidos.contarDelTorneo(torneoId);
        if (generados == 0) {
            return;
        }
        if (!confirmar) {
            throw new ConfirmacionRequeridaException(cambio + " invalida el fixture: hay " + generados
                    + " partidos generados. Si confirmás, se borran y hay que volver a generarlos y programarlos.");
        }
        partidos.borrarDelTorneo(torneoId);
    }

    /**
     * Cambios que invalidan el sorteo de zonas (borrar una zona con equipos, cambiar la categoría o la rama de un
     * equipo sorteado...): al confirmar se deshace el sorteo de todo el torneo, porque sortear siempre sortea todo.
     */
    void siHaySorteo(Long torneoId, boolean confirmar, String cambio) {
        long sorteados = equipos.countByTorneoIdAndZonaIsNotNull(torneoId);
        if (sorteados == 0) {
            return;
        }
        if (!confirmar) {
            long generados = partidos.contarDelTorneo(torneoId);
            throw new ConfirmacionRequeridaException(cambio + " invalida el sorteo de zonas: hay " + sorteados
                    + " equipos sorteados" + (generados > 0 ? " y " + generados + " partidos generados" : "")
                    + ". Si confirmás, se deshace el sorteo de todo el torneo, se borran los partidos y hay que volver "
                    + "a sortear, generar y programar.");
        }
        partidos.borrarDelTorneo(torneoId);
        equipos.deshacerSorteo(torneoId);
    }
}

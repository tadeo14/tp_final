package ar.edu.utn.fixture.servicio;

import org.springframework.stereotype.Component;

import ar.edu.utn.fixture.dto.Respuestas.TorneoDetalle;
import ar.edu.utn.fixture.entidad.EstadoTorneo;
import ar.edu.utn.fixture.entidad.Torneo;
import ar.edu.utn.fixture.repositorio.TorneoRepositorio;

/**
 * Búsqueda de torneos compartida por los servicios, con el control de estado en un solo lugar.
 */
@Component
public class Torneos {

    private final TorneoRepositorio repositorio;

    public Torneos(TorneoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Torneo buscar(Long torneoId) {
        return repositorio.findById(torneoId)
                .orElseThrow(() -> new NoEncontradoException("No existe el torneo " + torneoId));
    }

    /** El torneo, solo si se puede modificar: en PUBLICADO no se cambia nada (D20, D39). */
    public Torneo editable(Long torneoId) {
        Torneo t = buscar(torneoId);
        if (t.getEstado() == EstadoTorneo.PUBLICADO) {
            throw new ConflictoException("El torneo " + torneoId + " está publicado: no se puede modificar (D20)");
        }
        return t;
    }

    /** El torneo, solo si está publicado. Si no, para el público no existe. */
    public Torneo publicado(Long torneoId) {
        Torneo t = repositorio.findById(torneoId).orElse(null);
        if (t == null || t.getEstado() != EstadoTorneo.PUBLICADO) {
            throw new NoEncontradoException("No hay un torneo publicado con id " + torneoId);
        }
        return t;
    }

    public void guardar(Torneo torneo) {
        repositorio.save(torneo);
    }

    static TorneoDetalle detalle(Torneo t) {
        return new TorneoDetalle(t.getId(), t.getNombre(), t.getEstado(),
                t.getTurnosCadaMin(), t.getRecambioMin(), t.getDescansoMinimoMin());
    }
}

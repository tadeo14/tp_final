package ar.edu.utn.fixture.servicio;

import java.util.List;

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

    /**
     * El torneo, solo si es del organizador. Si es de otro, responde lo mismo que si no existiera (404):
     * así no se revela que el torneo existe (D45).
     */
    public Torneo delOrganizador(Long torneoId, Long organizadorId) {
        return repositorio.findByIdAndOrganizadorId(torneoId, organizadorId)
                .orElseThrow(() -> new NoEncontradoException("No existe el torneo " + torneoId));
    }

    /** El torneo del organizador, solo si se puede modificar: en PUBLICADO no se cambia nada hasta reabrirlo (D20, D47). */
    public Torneo editable(Long torneoId, Long organizadorId) {
        Torneo t = delOrganizador(torneoId, organizadorId);
        if (t.getEstado() == EstadoTorneo.PUBLICADO) {
            throw new ConflictoException("El torneo " + torneoId + " está publicado: para modificarlo, primero hay que reabrirlo (D20, D47)");
        }
        return t;
    }

    public List<Torneo> delOrganizador(Long organizadorId) {
        return repositorio.findByOrganizadorIdOrderByIdAsc(organizadorId);
    }

    public void guardar(Torneo torneo) {
        repositorio.save(torneo);
    }

    static TorneoDetalle detalle(Torneo t) {
        return new TorneoDetalle(t.getId(), t.getNombre(), t.getEstado(),
                t.getTurnosCadaMin(), t.getRecambioMin(), t.getDescansoMinimoMin());
    }
}

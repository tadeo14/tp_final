package ar.edu.utn.fixture.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.fixture.dto.Pedidos.NuevaFranja;
import ar.edu.utn.fixture.dto.Respuestas.FranjaDetalle;
import ar.edu.utn.fixture.entidad.FranjaHoraria;
import ar.edu.utn.fixture.repositorio.FranjaHorariaRepositorio;

/**
 * Edición y baja de la configuración del torneo (D66). Solo en BORRADOR y solo el dueño (D45): con el torneo
 * PUBLICADO responde 409 y hay que reabrirlo (D47). Si el cambio invalida el fixture, pide confirmación
 * ({@code confirmar}) y al confirmar borra los partidos ({@link Invalidaciones}). Lo publicado no se toca (D46).
 * Los pedidos son los mismos del alta: PUT reemplaza el elemento entero.
 */
@Service
@Transactional
public class EdicionServicio {

    private final Torneos torneos;
    private final Invalidaciones invalidaciones;
    private final FranjaHorariaRepositorio franjas;

    public EdicionServicio(Torneos torneos, Invalidaciones invalidaciones, FranjaHorariaRepositorio franjas) {
        this.torneos = torneos;
        this.invalidaciones = invalidaciones;
        this.franjas = franjas;
    }

    // ------------------------------------------------------------------ franjas

    /** Cambiar el día o el horario de una franja afecta la programación. */
    public FranjaDetalle editarFranja(Long organizadorId, Long torneoId, Long franjaId, NuevaFranja pedido, boolean confirmar) {
        torneos.editable(torneoId, organizadorId);
        FranjaHoraria f = franja(franjaId, torneoId);
        if (!pedido.hasta().isAfter(pedido.desde())) {
            throw new DatosInvalidosException("La franja tiene que terminar después de empezar");
        }
        boolean cambia = !f.getFecha().equals(pedido.fecha()) || !f.getDesde().equals(pedido.desde())
                || !f.getHasta().equals(pedido.hasta());
        if (cambia) {
            invalidaciones.siHayProgramados(torneoId, confirmar, "Cambiar la franja");
        }
        f.setFecha(pedido.fecha());
        f.setDesde(pedido.desde());
        f.setHasta(pedido.hasta());
        f = franjas.save(f);
        return new FranjaDetalle(f.getId(), f.getFecha(), f.getDesde(), f.getHasta());
    }

    public void borrarFranja(Long organizadorId, Long torneoId, Long franjaId, boolean confirmar) {
        torneos.editable(torneoId, organizadorId);
        FranjaHoraria f = franja(franjaId, torneoId);
        invalidaciones.siHayProgramados(torneoId, confirmar, "Borrar la franja");
        franjas.delete(f);
    }

    private FranjaHoraria franja(Long franjaId, Long torneoId) {
        return franjas.findByIdAndTorneoId(franjaId, torneoId)
                .orElseThrow(() -> new NoEncontradoException("No existe la franja " + franjaId + " en el torneo"));
    }
}

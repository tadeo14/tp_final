package ar.edu.utn.fixture.controlador;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.fixture.dto.Pedidos.NuevaFranja;
import ar.edu.utn.fixture.dto.Pedidos.NuevoEquipo;
import ar.edu.utn.fixture.dto.Respuestas.EquipoResumen;
import ar.edu.utn.fixture.dto.Respuestas.FranjaDetalle;
import ar.edu.utn.fixture.servicio.EdicionServicio;
import jakarta.validation.Valid;

/**
 * Editar (PUT) y eliminar (DELETE) la configuración del torneo, con las mismas rutas que el alta (D66).
 * Si el cambio invalida el fixture responde 409 con {@code requiereConfirmacion}; con {@code ?confirmar=true} se hace.
 */
@RestController
@RequestMapping("/api")
public class EdicionControlador {

    private final EdicionServicio servicio;

    public EdicionControlador(EdicionServicio servicio) {
        this.servicio = servicio;
    }

    @PutMapping("/torneos/{torneoId}/franjas/{franjaId}")
    public FranjaDetalle editarFranja(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId,
            @PathVariable Long franjaId, @Valid @RequestBody NuevaFranja pedido,
            @RequestParam(defaultValue = "false") boolean confirmar) {
        return servicio.editarFranja(organizadorId, torneoId, franjaId, pedido, confirmar);
    }

    @DeleteMapping("/torneos/{torneoId}/franjas/{franjaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrarFranja(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId,
            @PathVariable Long franjaId, @RequestParam(defaultValue = "false") boolean confirmar) {
        servicio.borrarFranja(organizadorId, torneoId, franjaId, confirmar);
    }

    @PutMapping("/torneos/{torneoId}/equipos/{equipoId}")
    public EquipoResumen editarEquipo(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId,
            @PathVariable Long equipoId, @Valid @RequestBody NuevoEquipo pedido,
            @RequestParam(defaultValue = "false") boolean confirmar) {
        return servicio.editarEquipo(organizadorId, torneoId, equipoId, pedido, confirmar);
    }

    @DeleteMapping("/torneos/{torneoId}/equipos/{equipoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrarEquipo(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId,
            @PathVariable Long equipoId, @RequestParam(defaultValue = "false") boolean confirmar) {
        servicio.borrarEquipo(organizadorId, torneoId, equipoId, confirmar);
    }
}

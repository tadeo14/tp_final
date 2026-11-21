package ar.edu.utn.fixture.controlador;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.fixture.dto.Pedidos.PedidoPropuestaZonas;
import ar.edu.utn.fixture.dto.Respuestas.PropuestaDeZonas;
import ar.edu.utn.fixture.dto.Respuestas.ZonaDetalle;
import ar.edu.utn.fixture.servicio.PropuestaZonasServicio;
import jakarta.validation.Valid;

/**
 * "Proponer zonas" (D69): vista previa y creación de las zonas según el máximo de equipos por zona.
 */
@RestController
@RequestMapping("/api/torneos/{torneoId}/zonas/propuesta")
public class PropuestaZonasControlador {

    private final PropuestaZonasServicio servicio;

    public PropuestaZonasControlador(PropuestaZonasServicio servicio) {
        this.servicio = servicio;
    }

    /** Vista previa: no guarda nada. */
    @PostMapping
    public PropuestaDeZonas vistaPrevia(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId,
            @Valid @RequestBody PedidoPropuestaZonas pedido) {
        return servicio.vistaPrevia(organizadorId, torneoId, pedido.maximoEquiposPorZona());
    }

    /** Crea las zonas propuestas. Reemplazar zonas existentes pide {@code confirmar=true}. */
    @PostMapping("/crear")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ZonaDetalle> crear(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId,
            @Valid @RequestBody PedidoPropuestaZonas pedido, @RequestParam(defaultValue = "false") boolean confirmar) {
        return servicio.crear(organizadorId, torneoId, pedido.maximoEquiposPorZona(), confirmar);
    }
}

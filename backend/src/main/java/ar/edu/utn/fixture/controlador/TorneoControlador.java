package ar.edu.utn.fixture.controlador;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.fixture.dto.Pedidos.NuevaCancha;
import ar.edu.utn.fixture.dto.Pedidos.NuevaCategoria;
import ar.edu.utn.fixture.dto.Pedidos.NuevaFranja;
import ar.edu.utn.fixture.dto.Pedidos.NuevaRama;
import ar.edu.utn.fixture.dto.Pedidos.NuevaSede;
import ar.edu.utn.fixture.dto.Pedidos.NuevaZona;
import ar.edu.utn.fixture.dto.Pedidos.NuevoClub;
import ar.edu.utn.fixture.dto.Pedidos.NuevoEquipo;
import ar.edu.utn.fixture.dto.Pedidos.NuevoTorneo;
import ar.edu.utn.fixture.dto.Respuestas.ConfiguracionDetalle;
import ar.edu.utn.fixture.dto.Respuestas.Creado;
import ar.edu.utn.fixture.dto.Respuestas.EquipoResumen;
import ar.edu.utn.fixture.dto.Respuestas.TorneoDetalle;
import ar.edu.utn.fixture.servicio.ConfiguracionServicio;
import jakarta.validation.Valid;

/**
 * Endpoints del organizador para configurar el torneo e inscribir equipos.
 * Exigen un organizador autenticado; el id sale del token, no del pedido (D44, D45).
 */
@RestController
@RequestMapping("/api")
public class TorneoControlador {

    private final ConfiguracionServicio servicio;

    public TorneoControlador(ConfiguracionServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/torneos")
    @ResponseStatus(HttpStatus.CREATED)
    public TorneoDetalle crearTorneo(@AuthenticationPrincipal Long organizadorId, @Valid @RequestBody NuevoTorneo pedido) {
        return servicio.crearTorneo(organizadorId, pedido);
    }

    /** Solo los torneos del organizador autenticado. */
    @GetMapping("/torneos")
    public List<TorneoDetalle> listarTorneos(@AuthenticationPrincipal Long organizadorId) {
        return servicio.listarTorneos(organizadorId);
    }

    @GetMapping("/torneos/{torneoId}")
    public TorneoDetalle obtenerTorneo(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId) {
        return servicio.obtenerTorneo(organizadorId, torneoId);
    }

    @PostMapping("/torneos/{torneoId}/franjas")
    @ResponseStatus(HttpStatus.CREATED)
    public Creado agregarFranja(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId, @Valid @RequestBody NuevaFranja pedido) {
        return servicio.agregarFranja(organizadorId, torneoId, pedido);
    }

    @PostMapping("/torneos/{torneoId}/sedes")
    @ResponseStatus(HttpStatus.CREATED)
    public Creado agregarSede(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId, @Valid @RequestBody NuevaSede pedido) {
        return servicio.agregarSede(organizadorId, torneoId, pedido);
    }

    @PostMapping("/sedes/{sedeId}/canchas")
    @ResponseStatus(HttpStatus.CREATED)
    public Creado agregarCancha(@AuthenticationPrincipal Long organizadorId, @PathVariable Long sedeId,
            @Valid @RequestBody NuevaCancha pedido) {
        return servicio.agregarCancha(organizadorId, sedeId, pedido);
    }

    @PostMapping("/torneos/{torneoId}/categorias")
    @ResponseStatus(HttpStatus.CREATED)
    public Creado agregarCategoria(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId, @Valid @RequestBody NuevaCategoria pedido) {
        return servicio.agregarCategoria(organizadorId, torneoId, pedido);
    }

    @PostMapping("/torneos/{torneoId}/ramas")
    @ResponseStatus(HttpStatus.CREATED)
    public Creado agregarRama(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId, @Valid @RequestBody NuevaRama pedido) {
        return servicio.agregarRama(organizadorId, torneoId, pedido);
    }

    @PostMapping("/torneos/{torneoId}/clubes")
    @ResponseStatus(HttpStatus.CREATED)
    public Creado agregarClub(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId, @Valid @RequestBody NuevoClub pedido) {
        return servicio.agregarClub(organizadorId, torneoId, pedido);
    }

    @PostMapping("/torneos/{torneoId}/zonas")
    @ResponseStatus(HttpStatus.CREATED)
    public Creado agregarZona(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId, @Valid @RequestBody NuevaZona pedido) {
        return servicio.agregarZona(organizadorId, torneoId, pedido);
    }

    @PostMapping("/torneos/{torneoId}/equipos")
    @ResponseStatus(HttpStatus.CREATED)
    public EquipoResumen inscribirEquipo(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId, @Valid @RequestBody NuevoEquipo pedido) {
        return servicio.inscribirEquipo(organizadorId, torneoId, pedido);
    }

    /** Toda la configuración del torneo (franjas, sedes y canchas, categorías, ramas, clubes y zonas), D56. */
    @GetMapping("/torneos/{torneoId}/configuracion")
    public ConfiguracionDetalle configuracion(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId) {
        return servicio.configuracion(organizadorId, torneoId);
    }

    @GetMapping("/torneos/{torneoId}/equipos")
    public List<EquipoResumen> listarEquipos(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId) {
        return servicio.listarEquipos(organizadorId, torneoId);
    }
}

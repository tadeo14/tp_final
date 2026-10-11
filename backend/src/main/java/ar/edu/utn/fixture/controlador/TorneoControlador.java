package ar.edu.utn.fixture.controlador;

import java.util.List;

import org.springframework.http.HttpStatus;
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
import ar.edu.utn.fixture.dto.Pedidos.NuevoOrganizador;
import ar.edu.utn.fixture.dto.Pedidos.NuevoTorneo;
import ar.edu.utn.fixture.dto.Respuestas.Creado;
import ar.edu.utn.fixture.dto.Respuestas.EquipoResumen;
import ar.edu.utn.fixture.dto.Respuestas.OrganizadorCreado;
import ar.edu.utn.fixture.dto.Respuestas.TorneoDetalle;
import ar.edu.utn.fixture.servicio.ConfiguracionServicio;
import jakarta.validation.Valid;

/**
 * Endpoints del organizador para configurar el torneo e inscribir equipos.
 */
@RestController
@RequestMapping("/api")
public class TorneoControlador {

    private final ConfiguracionServicio servicio;

    public TorneoControlador(ConfiguracionServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/organizadores")
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizadorCreado crearOrganizador(@Valid @RequestBody NuevoOrganizador pedido) {
        return servicio.crearOrganizador(pedido);
    }

    @PostMapping("/torneos")
    @ResponseStatus(HttpStatus.CREATED)
    public TorneoDetalle crearTorneo(@Valid @RequestBody NuevoTorneo pedido) {
        return servicio.crearTorneo(pedido);
    }

    @GetMapping("/torneos/{torneoId}")
    public TorneoDetalle obtenerTorneo(@PathVariable Long torneoId) {
        return servicio.obtenerTorneo(torneoId);
    }

    @PostMapping("/torneos/{torneoId}/franjas")
    @ResponseStatus(HttpStatus.CREATED)
    public Creado agregarFranja(@PathVariable Long torneoId, @Valid @RequestBody NuevaFranja pedido) {
        return servicio.agregarFranja(torneoId, pedido);
    }

    @PostMapping("/torneos/{torneoId}/sedes")
    @ResponseStatus(HttpStatus.CREATED)
    public Creado agregarSede(@PathVariable Long torneoId, @Valid @RequestBody NuevaSede pedido) {
        return servicio.agregarSede(torneoId, pedido);
    }

    @PostMapping("/sedes/{sedeId}/canchas")
    @ResponseStatus(HttpStatus.CREATED)
    public Creado agregarCancha(@PathVariable Long sedeId, @Valid @RequestBody NuevaCancha pedido) {
        return servicio.agregarCancha(sedeId, pedido);
    }

    @PostMapping("/torneos/{torneoId}/categorias")
    @ResponseStatus(HttpStatus.CREATED)
    public Creado agregarCategoria(@PathVariable Long torneoId, @Valid @RequestBody NuevaCategoria pedido) {
        return servicio.agregarCategoria(torneoId, pedido);
    }

    @PostMapping("/torneos/{torneoId}/ramas")
    @ResponseStatus(HttpStatus.CREATED)
    public Creado agregarRama(@PathVariable Long torneoId, @Valid @RequestBody NuevaRama pedido) {
        return servicio.agregarRama(torneoId, pedido);
    }

    @PostMapping("/torneos/{torneoId}/clubes")
    @ResponseStatus(HttpStatus.CREATED)
    public Creado agregarClub(@PathVariable Long torneoId, @Valid @RequestBody NuevoClub pedido) {
        return servicio.agregarClub(torneoId, pedido);
    }

    @PostMapping("/torneos/{torneoId}/zonas")
    @ResponseStatus(HttpStatus.CREATED)
    public Creado agregarZona(@PathVariable Long torneoId, @Valid @RequestBody NuevaZona pedido) {
        return servicio.agregarZona(torneoId, pedido);
    }

    @PostMapping("/torneos/{torneoId}/equipos")
    @ResponseStatus(HttpStatus.CREATED)
    public EquipoResumen inscribirEquipo(@PathVariable Long torneoId, @Valid @RequestBody NuevoEquipo pedido) {
        return servicio.inscribirEquipo(torneoId, pedido);
    }

    @GetMapping("/torneos/{torneoId}/equipos")
    public List<EquipoResumen> listarEquipos(@PathVariable Long torneoId) {
        return servicio.listarEquipos(torneoId);
    }
}

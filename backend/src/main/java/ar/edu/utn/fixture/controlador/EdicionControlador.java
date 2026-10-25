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

import ar.edu.utn.fixture.dto.Pedidos.NuevaCancha;
import ar.edu.utn.fixture.dto.Pedidos.NuevaCategoria;
import ar.edu.utn.fixture.dto.Pedidos.NuevaFranja;
import ar.edu.utn.fixture.dto.Pedidos.NuevaRama;
import ar.edu.utn.fixture.dto.Pedidos.NuevaSede;
import ar.edu.utn.fixture.dto.Pedidos.NuevoClub;
import ar.edu.utn.fixture.dto.Pedidos.NuevoEquipo;
import ar.edu.utn.fixture.dto.Respuestas.CanchaDetalle;
import ar.edu.utn.fixture.dto.Respuestas.CategoriaDetalle;
import ar.edu.utn.fixture.dto.Respuestas.Creado;
import ar.edu.utn.fixture.dto.Respuestas.EquipoResumen;
import ar.edu.utn.fixture.dto.Respuestas.FranjaDetalle;
import ar.edu.utn.fixture.dto.Respuestas.SedeDetalle;
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

    @PutMapping("/torneos/{torneoId}/sedes/{sedeId}")
    public SedeDetalle editarSede(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId,
            @PathVariable Long sedeId, @Valid @RequestBody NuevaSede pedido) {
        return servicio.editarSede(organizadorId, torneoId, sedeId, pedido);
    }

    @DeleteMapping("/torneos/{torneoId}/sedes/{sedeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrarSede(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId,
            @PathVariable Long sedeId, @RequestParam(defaultValue = "false") boolean confirmar) {
        servicio.borrarSede(organizadorId, torneoId, sedeId, confirmar);
    }

    @PutMapping("/sedes/{sedeId}/canchas/{canchaId}")
    public CanchaDetalle editarCancha(@AuthenticationPrincipal Long organizadorId, @PathVariable Long sedeId,
            @PathVariable Long canchaId, @Valid @RequestBody NuevaCancha pedido,
            @RequestParam(defaultValue = "false") boolean confirmar) {
        return servicio.editarCancha(organizadorId, sedeId, canchaId, pedido, confirmar);
    }

    @DeleteMapping("/sedes/{sedeId}/canchas/{canchaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrarCancha(@AuthenticationPrincipal Long organizadorId, @PathVariable Long sedeId,
            @PathVariable Long canchaId, @RequestParam(defaultValue = "false") boolean confirmar) {
        servicio.borrarCancha(organizadorId, sedeId, canchaId, confirmar);
    }

    @PutMapping("/torneos/{torneoId}/clubes/{clubId}")
    public Creado editarClub(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId,
            @PathVariable Long clubId, @Valid @RequestBody NuevoClub pedido) {
        return servicio.editarClub(organizadorId, torneoId, clubId, pedido);
    }

    @DeleteMapping("/torneos/{torneoId}/clubes/{clubId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrarClub(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId, @PathVariable Long clubId) {
        servicio.borrarClub(organizadorId, torneoId, clubId);
    }

    @PutMapping("/torneos/{torneoId}/categorias/{categoriaId}")
    public CategoriaDetalle editarCategoria(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId,
            @PathVariable Long categoriaId, @Valid @RequestBody NuevaCategoria pedido,
            @RequestParam(defaultValue = "false") boolean confirmar) {
        return servicio.editarCategoria(organizadorId, torneoId, categoriaId, pedido, confirmar);
    }

    @DeleteMapping("/torneos/{torneoId}/categorias/{categoriaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrarCategoria(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId,
            @PathVariable Long categoriaId) {
        servicio.borrarCategoria(organizadorId, torneoId, categoriaId);
    }

    @PutMapping("/torneos/{torneoId}/ramas/{ramaId}")
    public Creado editarRama(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId,
            @PathVariable Long ramaId, @Valid @RequestBody NuevaRama pedido) {
        return servicio.editarRama(organizadorId, torneoId, ramaId, pedido);
    }

    @DeleteMapping("/torneos/{torneoId}/ramas/{ramaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrarRama(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId, @PathVariable Long ramaId) {
        servicio.borrarRama(organizadorId, torneoId, ramaId);
    }
}

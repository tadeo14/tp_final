package ar.edu.utn.fixture.controlador;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.fixture.dto.Respuestas.Generacion;
import ar.edu.utn.fixture.dto.Respuestas.PartidoDetalle;
import ar.edu.utn.fixture.dto.Respuestas.Publicado;
import ar.edu.utn.fixture.dto.Respuestas.Validacion;
import ar.edu.utn.fixture.dto.Respuestas.ZonaSorteada;
import ar.edu.utn.fixture.servicio.FixtureServicio;

/**
 * Endpoints del organizador para armar el fixture: sortear → generar → programar → validar → publicar.
 * Exigen un organizador autenticado y que el torneo sea suyo (D44, D45).
 */
@RestController
@RequestMapping("/api/torneos/{torneoId}")
public class FixtureControlador {

    private final FixtureServicio servicio;

    public FixtureControlador(FixtureServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/sorteo")
    public List<ZonaSorteada> sortear(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId) {
        return servicio.sortear(organizadorId, torneoId);
    }

    @PostMapping("/fixture/generar")
    public Generacion generar(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId) {
        return servicio.generar(organizadorId, torneoId);
    }

    @PostMapping("/fixture/programar")
    public List<PartidoDetalle> programar(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId) {
        return servicio.programar(organizadorId, torneoId);
    }

    @GetMapping("/fixture")
    public List<PartidoDetalle> fixture(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId) {
        return servicio.fixture(organizadorId, torneoId);
    }

    @GetMapping("/validacion")
    public Validacion validar(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId) {
        return servicio.validar(organizadorId, torneoId);
    }

    @PostMapping("/publicar")
    public Publicado publicar(@AuthenticationPrincipal Long organizadorId, @PathVariable Long torneoId) {
        return servicio.publicar(organizadorId, torneoId);
    }
}

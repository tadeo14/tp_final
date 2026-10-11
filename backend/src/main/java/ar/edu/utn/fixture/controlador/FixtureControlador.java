package ar.edu.utn.fixture.controlador;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.fixture.dto.Respuestas.Generacion;
import ar.edu.utn.fixture.dto.Respuestas.PartidoDetalle;
import ar.edu.utn.fixture.dto.Respuestas.TorneoDetalle;
import ar.edu.utn.fixture.dto.Respuestas.Validacion;
import ar.edu.utn.fixture.dto.Respuestas.ZonaSorteada;
import ar.edu.utn.fixture.servicio.FixtureServicio;

/**
 * Endpoints del organizador para armar el fixture: sortear → generar → programar → validar → publicar.
 */
@RestController
@RequestMapping("/api/torneos/{torneoId}")
public class FixtureControlador {

    private final FixtureServicio servicio;

    public FixtureControlador(FixtureServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/sorteo")
    public List<ZonaSorteada> sortear(@PathVariable Long torneoId) {
        return servicio.sortear(torneoId);
    }

    @PostMapping("/fixture/generar")
    public Generacion generar(@PathVariable Long torneoId) {
        return servicio.generar(torneoId);
    }

    @PostMapping("/fixture/programar")
    public List<PartidoDetalle> programar(@PathVariable Long torneoId) {
        return servicio.programar(torneoId);
    }

    @GetMapping("/fixture")
    public List<PartidoDetalle> fixture(@PathVariable Long torneoId) {
        return servicio.fixture(torneoId);
    }

    @GetMapping("/validacion")
    public Validacion validar(@PathVariable Long torneoId) {
        return servicio.validar(torneoId);
    }

    @PostMapping("/publicar")
    public TorneoDetalle publicar(@PathVariable Long torneoId) {
        return servicio.publicar(torneoId);
    }
}

package ar.edu.utn.fixture.controlador;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.fixture.dto.Respuestas.EquipoResumen;
import ar.edu.utn.fixture.dto.Respuestas.FixtureDeEquipo;
import ar.edu.utn.fixture.servicio.ConsultaPublicaServicio;

/**
 * Vista pública de solo lectura: torneos publicados, sin login.
 */
@RestController
@RequestMapping("/api/publico/torneos/{torneoId}")
public class PublicoControlador {

    private final ConsultaPublicaServicio servicio;

    public PublicoControlador(ConsultaPublicaServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/equipos")
    public List<EquipoResumen> equipos(@PathVariable Long torneoId) {
        return servicio.equipos(torneoId);
    }

    @GetMapping("/equipos/{equipoId}/fixture")
    public FixtureDeEquipo fixtureDeEquipo(@PathVariable Long torneoId, @PathVariable Long equipoId) {
        return servicio.fixtureDeEquipo(torneoId, equipoId);
    }
}

package ar.edu.utn.fixture.controlador;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.fixture.dto.Respuestas.EquiposPublicados;
import ar.edu.utn.fixture.dto.Respuestas.FixtureDeEquipo;
import ar.edu.utn.fixture.servicio.ConsultaPublicaServicio;

/**
 * Vista pública de solo lectura, sin login: muestra la última versión publicada de cada torneo (D46).
 */
@RestController
@RequestMapping("/api/publico/torneos/{torneoId}")
public class PublicoControlador {

    private final ConsultaPublicaServicio servicio;

    public PublicoControlador(ConsultaPublicaServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/equipos")
    public EquiposPublicados equipos(@PathVariable Long torneoId) {
        return servicio.equipos(torneoId);
    }

    @GetMapping("/equipos/{equipoId}/fixture")
    public FixtureDeEquipo fixtureDeEquipo(@PathVariable Long torneoId, @PathVariable Long equipoId) {
        return servicio.fixtureDeEquipo(torneoId, equipoId);
    }
}

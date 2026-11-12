package ar.edu.utn.fixture.controlador;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint para comprobar que el backend responde y que llega a la base de datos.
 */
@RestController
public class SaludControlador {

    private final JdbcTemplate jdbc;

    public SaludControlador(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/api/salud")
    public Map<String, String> salud() {
        jdbc.queryForObject("SELECT 1", Integer.class);
        return Map.of("estado", "OK", "base", "conectada");
    }
}

package ar.edu.utn.fixture.controlador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

/**
 * Editar los parámetros del torneo (D66). Torneo chico de PRUEBA con 6 partidos programados.
 */
class EdicionTorneoApiTest extends EdicionApiTestBase {

    private static final String RENOMBRADO = """
            {"nombre": "Torneo de prueba corregido", "turnosCadaMin": 15, "recambioMin": 10, "descansoMinimoMin": 90}
            """;
    private static final String MAS_DESCANSO = """
            {"nombre": "Torneo de prueba", "turnosCadaMin": 15, "recambioMin": 10, "descansoMinimoMin": 120}
            """;

    private long torneo;
    private String url;

    private void conFixtureProgramado() throws Exception {
        torneo = torneoChicoProgramado();
        url = "/api/torneos/" + torneo;
    }

    @Test
    void renombrar_noAfectaElFixtureYElPublicoLoVeEnseguida() throws Exception {
        conFixtureProgramado();
        publicar(torneo);
        mvc.perform(post(url + "/reabrir")).andExpect(status().isOk());

        editar(url, RENOMBRADO).andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Torneo de prueba corregido"));
        assertThat(partidos(torneo)).isEqualTo(6);
        // D60: el nombre sale de la tabla viva, aunque el resto sea la versión 1.
        mvcSinToken.perform(get("/api/publico/torneos/" + torneo + "/equipos"))
                .andExpect(jsonPath("$.torneo").value("Torneo de prueba corregido"))
                .andExpect(jsonPath("$.version.numero").value(1));
    }

    @Test
    void cambiarElDescansoConPartidosProgramados_pideConfirmacion() throws Exception {
        conFixtureProgramado();

        editar(url, MAS_DESCANSO)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.requiereConfirmacion").value(true))
                .andExpect(jsonPath("$.error").value(containsString("Cambiar los turnos, el recambio o el descanso del torneo")));
        mvc.perform(get(url)).andExpect(jsonPath("$.descansoMinimoMin").value(90));

        editar(url + "?confirmar=true", MAS_DESCANSO).andExpect(status().isOk()).andExpect(jsonPath("$.descansoMinimoMin").value(120));
        assertThat(partidos(torneo)).isZero();
    }

    @Test
    void datosInvalidos_respondeBadRequestEnEspanol() throws Exception {
        conFixtureProgramado();

        editar(url, RENOMBRADO.replace("\"turnosCadaMin\": 15", "\"turnosCadaMin\": 0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles", hasItem("Turnos cada (min): tiene que ser mayor que 0")));
    }

    @Test
    void torneoAjeno_respondeNotFound() throws Exception {
        conFixtureProgramado();

        editar(ajeno(), url, RENOMBRADO).andExpect(status().isNotFound());
        mvc.perform(get(url)).andExpect(jsonPath("$.nombre").value("Torneo de prueba"));
    }

    @Test
    void torneoPublicado_respondeConflictYPideReabrir() throws Exception {
        conFixtureProgramado();
        publicar(torneo);

        editar(url, RENOMBRADO)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(containsString("primero hay que reabrirlo")));
    }
}

package ar.edu.utn.fixture.controlador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

/**
 * Editar y borrar franjas (D66). Torneo chico de PRUEBA: 4 equipos, 6 partidos programados el sábado de 9 a 20.
 */
class EdicionFranjasApiTest extends EdicionApiTestBase {

    private static final String SABADO_9_A_18 = "{\"fecha\":\"2030-01-05\",\"desde\":\"09:00\",\"hasta\":\"18:00\"}";

    private long torneo;
    private String franjaUrl;

    private void sinFixture() throws Exception {
        torneo = crearTorneoDePrueba();
        crear("/api/torneos/" + torneo + "/franjas", "{\"fecha\":\"2030-01-05\",\"desde\":\"09:00\",\"hasta\":\"20:00\"}");
        franjaUrl = "/api/torneos/" + torneo + "/franjas/" + configuracion(torneo).get("franjas").get(0).get("id").asLong();
    }

    private void conFixtureProgramado() throws Exception {
        torneo = torneoChicoProgramado();
        franjaUrl = "/api/torneos/" + torneo + "/franjas/" + configuracion(torneo).get("franjas").get(0).get("id").asLong();
    }

    @Test
    void editar_cambiaLaFranja() throws Exception {
        sinFixture();

        editar(franjaUrl, SABADO_9_A_18)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasta").value("18:00:00"));
        assertThat(configuracion(torneo).get("franjas").get(0).get("hasta").asText()).isEqualTo("18:00:00");
    }

    @Test
    void editar_conDatosInvalidos_respondeBadRequest() throws Exception {
        sinFixture();

        editar(franjaUrl, "{\"fecha\":\"2030-01-05\",\"desde\":\"18:00\",\"hasta\":\"09:00\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void borrar_quitaLaFranja() throws Exception {
        sinFixture();

        borrar(franjaUrl).andExpect(status().isNoContent());
        assertThat(configuracion(torneo).get("franjas")).isEmpty();
    }

    @Test
    void torneoAjeno_respondeNotFound() throws Exception {
        sinFixture();

        editar(ajeno(), franjaUrl, SABADO_9_A_18).andExpect(status().isNotFound());
        ajeno().perform(delete(franjaUrl))
                .andExpect(status().isNotFound());
        assertThat(configuracion(torneo).get("franjas")).hasSize(1);
    }

    @Test
    void franjaDeOtroTorneo_respondeNotFound() throws Exception {
        sinFixture();
        long otro = crearTorneoDePrueba();

        editar(franjaUrl.replace("/torneos/" + torneo + "/", "/torneos/" + otro + "/"), SABADO_9_A_18)
                .andExpect(status().isNotFound());
    }

    @Test
    void torneoPublicado_respondeConflictYPideReabrir() throws Exception {
        conFixtureProgramado();
        publicar(torneo);

        editar(franjaUrl, SABADO_9_A_18)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(containsString("primero hay que reabrirlo")))
                .andExpect(jsonPath("$.requiereConfirmacion").doesNotExist());
        borrar(franjaUrl + "?confirmar=true").andExpect(status().isConflict());
    }

    @Test
    void conElFixtureProgramado_editarPideConfirmacionYAlConfirmarBorraLosPartidos() throws Exception {
        conFixtureProgramado();

        editar(franjaUrl, SABADO_9_A_18)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.requiereConfirmacion").value(true))
                .andExpect(jsonPath("$.error").value(containsString("hay 6 partidos programados")));
        assertThat(partidos(torneo)).isEqualTo(6); // sin confirmar no cambia nada
        assertThat(configuracion(torneo).get("franjas").get(0).get("hasta").asText()).isEqualTo("20:00:00");

        editar(franjaUrl + "?confirmar=true", SABADO_9_A_18).andExpect(status().isOk());
        assertThat(partidos(torneo)).isZero();
        assertThat(configuracion(torneo).get("franjas").get(0).get("hasta").asText()).isEqualTo("18:00:00");
    }

    @Test
    void conElFixtureProgramado_borrarPideConfirmacion() throws Exception {
        conFixtureProgramado();

        borrar(franjaUrl).andExpect(status().isConflict()).andExpect(jsonPath("$.requiereConfirmacion").value(true));
        assertThat(partidos(torneo)).isEqualTo(6);

        borrar(franjaUrl + "?confirmar=true").andExpect(status().isNoContent());
        assertThat(partidos(torneo)).isZero();
        assertThat(configuracion(torneo).get("franjas")).isEmpty();
    }

    @Test
    void guardarSinCambios_noInvalidaElFixture() throws Exception {
        conFixtureProgramado();

        editar(franjaUrl, "{\"fecha\":\"2030-01-05\",\"desde\":\"09:00\",\"hasta\":\"20:00\"}").andExpect(status().isOk());
        assertThat(partidos(torneo)).isEqualTo(6);
    }
}

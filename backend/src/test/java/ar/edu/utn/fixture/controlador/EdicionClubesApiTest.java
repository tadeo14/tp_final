package ar.edu.utn.fixture.controlador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

/**
 * Editar y borrar clubes (D66). Torneo chico de PRUEBA: un club con 4 equipos y 6 partidos programados.
 */
class EdicionClubesApiTest extends EdicionApiTestBase {

    private long torneo;
    private String clubUrl;

    private void conFixtureProgramado() throws Exception {
        torneo = torneoChicoProgramado();
        clubUrl = "/api/torneos/" + torneo + "/clubes/" + configuracion(torneo).get("clubes").get(0).get("id").asLong();
    }

    @Test
    void editar_renombraElClubSinAfectarElFixture() throws Exception {
        conFixtureProgramado();

        editar(clubUrl, "{\"nombre\":\"Club renombrado\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Club renombrado"));
        assertThat(equipos(torneo).get(0).get("club").asText()).isEqualTo("Club renombrado");
        assertThat(partidos(torneo)).isEqualTo(6);
    }

    @Test
    void borrar_unClubSinEquipos() throws Exception {
        conFixtureProgramado();
        long otro = crearId("/api/torneos/" + torneo + "/clubes", "{\"nombre\":\"Club sin equipos\"}");

        borrar("/api/torneos/" + torneo + "/clubes/" + otro).andExpect(status().isNoContent());
        assertThat(configuracion(torneo).get("clubes")).hasSize(1);
        assertThat(partidos(torneo)).isEqualTo(6);
    }

    @Test
    void borrar_unClubConEquipos_respondeConflictYDiceQueLoUsa() throws Exception {
        conFixtureProgramado();

        borrar(clubUrl)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El club Club de prueba tiene 4 equipos: para borrarlo, primero hay que "
                        + "dar de baja sus equipos o pasarlos a otro club"))
                .andExpect(jsonPath("$.requiereConfirmacion").doesNotExist());
        // Confirmar no cambia nada: no es un caso que se pueda confirmar.
        borrar(clubUrl + "?confirmar=true").andExpect(status().isConflict());
        assertThat(configuracion(torneo).get("clubes")).hasSize(1);
    }

    @Test
    void torneoAjeno_respondeNotFound() throws Exception {
        conFixtureProgramado();

        editar(ajeno(), clubUrl, "{\"nombre\":\"Otro\"}").andExpect(status().isNotFound());
        ajeno().perform(delete(clubUrl)).andExpect(status().isNotFound());
    }

    @Test
    void torneoPublicado_respondeConflict() throws Exception {
        conFixtureProgramado();
        publicar(torneo);

        editar(clubUrl, "{\"nombre\":\"Otro\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(containsString("primero hay que reabrirlo")));
    }
}

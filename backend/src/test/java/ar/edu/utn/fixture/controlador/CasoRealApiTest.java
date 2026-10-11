package ar.edu.utn.fixture.controlador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * "Terminado cuando" del paso 7: el recorrido completo con el caso real por la API, hasta consultar el fixture
 * de un equipo. Carga los datos con el mismo script que usa el organizador (database/caso_real.sql).
 */
@Sql(scripts = "file:../database/caso_real.sql")
class CasoRealApiTest extends ApiTestBase {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void recorridoConElCasoReal_programaLos91PartidosYSeConsultaUnEquipo() throws Exception {
        long torneo = jdbc.queryForObject("select currval(pg_get_serial_sequence('torneo', 'id'))", Long.class);
        String base = "/api/torneos/" + torneo;

        mvc.perform(post(base + "/sorteo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(13)));

        mvc.perform(post(base + "/fixture/generar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadPartidos").value(91));

        mvc.perform(post(base + "/fixture/programar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(91)));

        mvc.perform(get(base + "/validacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true));

        mvc.perform(post(base + "/publicar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PUBLICADO"));

        // Un equipo de U13 femenino: su zona puede ser de 4 (3 partidos) o de 3 (2 partidos y una fecha libre).
        long equipo = jdbc.queryForObject(
                "select min(id) from equipo where torneo_id = ? and nombre like 'U13 Femenino%'", Long.class, torneo);
        JsonNode fixture = json.readTree(mvc.perform(get("/api/publico/torneos/" + torneo + "/equipos/" + equipo + "/fixture"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipo").value("U13 Femenino 01"))
                .andReturn().getResponse().getContentAsString());

        int partidos = fixture.get("partidos").size();
        int libres = fixture.get("rondasLibres").size();
        assertThat(partidos + libres).isEqualTo(3); // las zonas de 3 y de 4 tienen 3 rondas
        assertThat(partidos).isIn(2, 3);
        assertThat(fixture.get("partidos").get(0).get("sede").asText()).isIn("Club A", "Club B");
    }
}

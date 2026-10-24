package ar.edu.utn.fixture.controlador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * "Terminado cuando" del paso 7: el recorrido completo con el caso real por la API, hasta consultar el fixture
 * de un equipo. Sigue los mismos pasos que el organizador: registrarse como ejemplo@fixture.test, cargar
 * database/caso_real.sql, iniciar sesión y hacer el recorrido. La consulta pública se hace sin token.
 */
class CasoRealApiTest extends ApiTestBase {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private DataSource dataSource;

    @Test
    void recorridoConElCasoReal_programaLos91PartidosYSeConsultaUnEquipo() throws Exception {
        // 1. Registro e inicio de sesión del dueño del torneo del caso real.
        crear(mvcSinToken, "/api/auth/registro",
                "{\"nombre\":\"Organizador de ejemplo\",\"email\":\"ejemplo@fixture.test\",\"contrasenia\":\"clave-de-prueba\"}");
        String login = mvcSinToken.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ejemplo@fixture.test\",\"contrasenia\":\"clave-de-prueba\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        MockMvc organizador = mvcCon(json.readTree(login).get("token").asText());

        // 2. Datos del caso real, con la misma conexión (y transacción) del test: se deshacen al terminar.
        ScriptUtils.executeSqlScript(DataSourceUtils.getConnection(dataSource),
                new FileSystemResource("../database/caso_real.sql"));
        long torneo = jdbc.queryForObject("select currval(pg_get_serial_sequence('torneo', 'id'))", Long.class);
        String base = "/api/torneos/" + torneo;

        // 3. Recorrido del organizador.
        organizador.perform(post(base + "/sorteo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(13)));
        organizador.perform(post(base + "/fixture/generar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadPartidos").value(91));
        organizador.perform(post(base + "/fixture/programar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(91)));
        organizador.perform(get(base + "/validacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true));
        organizador.perform(post(base + "/publicar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PUBLICADO"));

        // 4. Vista pública, sin token. El primer equipo de U13 femenino: su zona puede ser de 4 (3 partidos)
        //    o de 3 (2 partidos y una fecha libre). Lleva el nombre de su club (D59).
        long equipo = jdbc.queryForObject("""
                select min(e.id) from equipo e
                join categoria c on c.id = e.categoria_id join rama r on r.id = e.rama_id
                where e.torneo_id = ? and c.nombre = 'U13' and r.nombre = 'Femenino'""", Long.class, torneo);
        JsonNode fixture = json.readTree(mvcSinToken.perform(get("/api/publico/torneos/" + torneo + "/equipos/" + equipo + "/fixture"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipo").value("Náutico Sportivo Avellaneda"))
                .andReturn().getResponse().getContentAsString());

        int partidos = fixture.get("partidos").size();
        int libres = fixture.get("rondasLibres").size();
        assertThat(partidos + libres).isEqualTo(3); // las zonas de 3 y de 4 tienen 3 rondas
        assertThat(partidos).isIn(2, 3);
        assertThat(fixture.get("partidos").get(0).get("sede").asText()).isIn("Club A", "Club B");
    }

    @Test
    void elScriptDelCasoRealFallaSiElOrganizadorNoEstaRegistrado() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> ScriptUtils.executeSqlScript(
                        DataSourceUtils.getConnection(dataSource), new FileSystemResource("../database/caso_real.sql")))
                .hasStackTraceContaining("organizador_id");
    }
}

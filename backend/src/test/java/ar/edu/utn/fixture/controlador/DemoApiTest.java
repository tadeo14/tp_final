package ar.edu.utn.fixture.controlador;

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

/**
 * database/demo.sql (D72): el torneo de prueba de la cuenta demo@fixture.test se carga y recorre completo.
 * Todos los datos son DE PRUEBA. Los cambios se deshacen al terminar cada test.
 */
class DemoApiTest extends ApiTestBase {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private DataSource dataSource;

    private void ejecutarDemo() {
        ScriptUtils.executeSqlScript(DataSourceUtils.getConnection(dataSource), new FileSystemResource("../database/demo.sql"));
    }

    @Test
    void elTorneoDemoSeCargaYSeRecorreCompleto() throws Exception {
        crear(mvcSinToken, "/api/auth/registro",
                "{\"nombre\":\"Cuenta de demostración\",\"email\":\"demo@fixture.test\",\"contrasenia\":\"clave-de-prueba\"}");
        String login = mvcSinToken.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"demo@fixture.test\",\"contrasenia\":\"clave-de-prueba\"}"))
                .andReturn().getResponse().getContentAsString();
        MockMvc demo = mvcCon(json.readTree(login).get("token").asText());

        ejecutarDemo();
        long torneo = jdbc.queryForObject("select currval(pg_get_serial_sequence('torneo', 'id'))", Long.class);
        String base = "/api/torneos/" + torneo;

        demo.perform(get(base)).andExpect(jsonPath("$.nombre").value("[PRUEBA] Torneo demo"))
                .andExpect(jsonPath("$.estado").value("BORRADOR"));
        demo.perform(get(base + "/configuracion"))
                .andExpect(jsonPath("$.sedes", hasSize(2)))
                .andExpect(jsonPath("$.categorias", hasSize(2)))
                .andExpect(jsonPath("$.clubes", hasSize(4)))
                .andExpect(jsonPath("$.zonas", hasSize(3)));
        demo.perform(get(base + "/equipos")).andExpect(jsonPath("$", hasSize(10)));

        demo.perform(post(base + "/sorteo")).andExpect(status().isOk());
        demo.perform(post(base + "/fixture/generar")).andExpect(jsonPath("$.cantidadPartidos").value(12));
        demo.perform(post(base + "/fixture/programar")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(12)));
        demo.perform(get(base + "/validacion")).andExpect(jsonPath("$.valido").value(true));
        demo.perform(post(base + "/publicar")).andExpect(status().isOk());
    }

    @Test
    void elScriptFallaSiLaCuentaDemoNoEstaRegistrada() {
        org.assertj.core.api.Assertions.assertThatThrownBy(this::ejecutarDemo).hasStackTraceContaining("organizador_id");
    }
}

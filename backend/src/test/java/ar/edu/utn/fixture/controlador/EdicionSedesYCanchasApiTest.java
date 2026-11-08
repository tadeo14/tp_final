package ar.edu.utn.fixture.controlador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Editar y borrar sedes y canchas (D66). Torneo chico de PRUEBA: una sede con 2 canchas y 6 partidos programados.
 */
class EdicionSedesYCanchasApiTest extends EdicionApiTestBase {

    private long torneo;
    private long sede;
    private String sedeUrl;
    private String canchasUrl;

    private void conFixtureProgramado() throws Exception {
        torneo = torneoChicoProgramado();
        sede = configuracion(torneo).get("sedes").get(0).get("id").asLong();
        sedeUrl = "/api/torneos/" + torneo + "/sedes/" + sede;
        canchasUrl = "/api/sedes/" + sede + "/canchas/";
    }

    private long cancha(int i) throws Exception {
        return configuracion(torneo).get("sedes").get(0).get("canchas").get(i).get("id").asLong();
    }

    @Test
    void editarSede_noAfectaElFixture() throws Exception {
        conFixtureProgramado();

        editar(sedeUrl, "{\"nombre\":\"Sede renombrada\",\"direccion\":\"Calle de prueba 123\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Sede renombrada"))
                .andExpect(jsonPath("$.direccion").value("Calle de prueba 123"))
                .andExpect(jsonPath("$.canchas.length()").value(2));
        assertThat(partidos(torneo)).isEqualTo(6);
    }

    @Test
    void renombrarCancha_noAfectaElFixture() throws Exception {
        conFixtureProgramado();

        editar(canchasUrl + cancha(0), "{\"nombre\":\"Cancha central\",\"habilitada\":true}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Cancha central"));
        assertThat(partidos(torneo)).isEqualTo(6);
    }

    @Test
    void deshabilitarUnaCanchaConPartidos_pideConfirmacion() throws Exception {
        conFixtureProgramado();
        String url = canchasUrl + cancha(0);

        editar(url, "{\"nombre\":\"Cancha 1\",\"habilitada\":false}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.requiereConfirmacion").value(true))
                .andExpect(jsonPath("$.error").value(containsString("Deshabilitar la cancha Sede de prueba - Cancha 1 invalida el fixture")));
        assertThat(partidos(torneo)).isEqualTo(6);

        editar(url + "?confirmar=true", "{\"nombre\":\"Cancha 1\",\"habilitada\":false}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.habilitada").value(false));
        assertThat(partidos(torneo)).isZero();
    }

    @Test
    void borrarUnaCanchaSinPartidos_noPideConfirmacion() throws Exception {
        conFixtureProgramado();
        long nueva = crearId("/api/sedes/" + sede + "/canchas", "{\"nombre\":\"Cancha 3\"}");

        borrar(canchasUrl + nueva).andExpect(status().isNoContent());
        assertThat(configuracion(torneo).get("sedes").get(0).get("canchas")).hasSize(2);
        assertThat(partidos(torneo)).isEqualTo(6);
    }

    @Test
    void borrarUnaCanchaConPartidos_pideConfirmacion() throws Exception {
        conFixtureProgramado();
        String url = canchasUrl + cancha(0);

        borrar(url).andExpect(status().isConflict()).andExpect(jsonPath("$.requiereConfirmacion").value(true));
        borrar(url + "?confirmar=true").andExpect(status().isNoContent());
        assertThat(configuracion(torneo).get("sedes").get(0).get("canchas")).hasSize(1);
        assertThat(partidos(torneo)).isZero();
    }

    @Test
    void borrarUnaSedeConPartidosProgramados_pideConfirmacionYBorraSusCanchas() throws Exception {
        conFixtureProgramado();

        borrar(sedeUrl)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(containsString("Borrar la sede Sede de prueba invalida el fixture")));
        assertThat(configuracion(torneo).get("sedes")).hasSize(1);

        borrar(sedeUrl + "?confirmar=true").andExpect(status().isNoContent());
        JsonNode config = configuracion(torneo);
        assertThat(config.get("sedes")).isEmpty();
        assertThat(partidos(torneo)).isZero();
    }

    @Test
    void borrarUnaSedeSinFixture() throws Exception {
        torneo = crearTorneoDePrueba();
        long s = crearId("/api/torneos/" + torneo + "/sedes", "{\"nombre\":\"Sede de prueba\"}");
        crear("/api/sedes/" + s + "/canchas", "{\"nombre\":\"Cancha 1\"}");

        borrar("/api/torneos/" + torneo + "/sedes/" + s).andExpect(status().isNoContent());
        assertThat(configuracion(torneo).get("sedes")).isEmpty();
    }

    @Test
    void torneoAjeno_respondeNotFound() throws Exception {
        conFixtureProgramado();

        editar(ajeno(), sedeUrl, "{\"nombre\":\"Otra\"}").andExpect(status().isNotFound());
        editar(ajeno(), canchasUrl + cancha(0), "{\"nombre\":\"Otra\"}").andExpect(status().isNotFound());
        ajeno().perform(delete(canchasUrl + cancha(0) + "?confirmar=true")).andExpect(status().isNotFound());
        ajeno().perform(delete(sedeUrl + "?confirmar=true")).andExpect(status().isNotFound());
        assertThat(partidos(torneo)).isEqualTo(6);
    }

    @Test
    void canchaDeOtraSede_respondeNotFound() throws Exception {
        conFixtureProgramado();
        long otraSede = crearId("/api/torneos/" + torneo + "/sedes", "{\"nombre\":\"Otra sede de prueba\"}");

        editar("/api/sedes/" + otraSede + "/canchas/" + cancha(0), "{\"nombre\":\"Cancha 1\"}").andExpect(status().isNotFound());
    }

    @Test
    void torneoPublicado_respondeConflict() throws Exception {
        conFixtureProgramado();
        publicar(torneo);

        editar(sedeUrl, "{\"nombre\":\"Otra\"}").andExpect(status().isConflict());
        borrar(canchasUrl + cancha(0) + "?confirmar=true")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(containsString("primero hay que reabrirlo")));
    }
}

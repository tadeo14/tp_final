package ar.edu.utn.fixture.controlador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

/**
 * Editar y borrar categorías y ramas (D66). Torneo chico de PRUEBA: U9 masculino, una zona con 4 equipos y
 * 6 partidos programados.
 */
class EdicionCategoriasYRamasApiTest extends EdicionApiTestBase {

    private static final String U9_RENOMBRADA = """
            {"nombre":"Sub 9","minutosNetos":28,"cantidadCuartos":2,"pausasMin":4,"margenDetencionesMin":10,"maxPartidosDia":3}
            """;
    private static final String U9_MAS_LARGA = """
            {"nombre":"U9","minutosNetos":40,"cantidadCuartos":4,"pausasMin":4,"margenDetencionesMin":10,"maxPartidosDia":3}
            """;

    private long torneo;
    private String categoriaUrl;
    private String ramaUrl;

    private void conFixtureProgramado() throws Exception {
        torneo = torneoChicoProgramado();
        categoriaUrl = "/api/torneos/" + torneo + "/categorias/" + configuracion(torneo).get("categorias").get(0).get("id").asLong();
        ramaUrl = "/api/torneos/" + torneo + "/ramas/" + configuracion(torneo).get("ramas").get(0).get("id").asLong();
    }

    @Test
    void editarNombreYCuartos_noAfectaElFixture() throws Exception {
        conFixtureProgramado();

        editar(categoriaUrl, U9_RENOMBRADA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Sub 9"))
                .andExpect(jsonPath("$.cantidadCuartos").value(2));
        assertThat(partidos(torneo)).isEqualTo(6);
    }

    @Test
    void cambiarLaDuracionConPartidosProgramados_pideConfirmacion() throws Exception {
        conFixtureProgramado();

        editar(categoriaUrl, U9_MAS_LARGA)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.requiereConfirmacion").value(true))
                .andExpect(jsonPath("$.error").value(containsString("Cambiar la duración o el tope diario de la categoría U9")));
        assertThat(configuracion(torneo).get("categorias").get(0).get("minutosNetos").asInt()).isEqualTo(28);

        editar(categoriaUrl + "?confirmar=true", U9_MAS_LARGA).andExpect(status().isOk());
        assertThat(partidos(torneo)).isZero();
        assertThat(configuracion(torneo).get("categorias").get(0).get("minutosNetos").asInt()).isEqualTo(40);
    }

    @Test
    void borrarUnaCategoriaYUnaRamaSinUso() throws Exception {
        conFixtureProgramado();
        long u11 = crearId("/api/torneos/" + torneo + "/categorias", U9_RENOMBRADA.replace("Sub 9", "U11"));
        long femenino = crearId("/api/torneos/" + torneo + "/ramas", "{\"nombre\":\"Femenino\"}");

        borrar("/api/torneos/" + torneo + "/categorias/" + u11).andExpect(status().isNoContent());
        borrar("/api/torneos/" + torneo + "/ramas/" + femenino).andExpect(status().isNoContent());
        assertThat(configuracion(torneo).get("categorias")).hasSize(1);
        assertThat(configuracion(torneo).get("ramas")).hasSize(1);
        assertThat(partidos(torneo)).isEqualTo(6);
    }

    @Test
    void borrarUnaCategoriaOUnaRamaEnUso_respondeConflictYDiceQueLasUsa() throws Exception {
        conFixtureProgramado();

        borrar(categoriaUrl)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("La categoría U9 tiene 1 zona y 4 equipos: para borrarla, primero hay que "
                        + "borrar sus zonas y dar de baja sus equipos (o pasarlos a otra)"))
                .andExpect(jsonPath("$.requiereConfirmacion").doesNotExist());
        borrar(ramaUrl)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(containsString("La rama Masculino tiene 1 zona y 4 equipos")));
        assertThat(configuracion(torneo).get("categorias")).hasSize(1);
    }

    @Test
    void editarRama() throws Exception {
        conFixtureProgramado();

        editar(ramaUrl, "{\"nombre\":\"Varones\"}").andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Varones"));
        assertThat(equipos(torneo).get(0).get("rama").asText()).isEqualTo("Varones");
        assertThat(partidos(torneo)).isEqualTo(6);
    }

    @Test
    void torneoAjeno_respondeNotFound() throws Exception {
        conFixtureProgramado();

        editar(ajeno(), categoriaUrl, U9_RENOMBRADA).andExpect(status().isNotFound());
        editar(ajeno(), ramaUrl, "{\"nombre\":\"Otra\"}").andExpect(status().isNotFound());
        ajeno().perform(delete(categoriaUrl)).andExpect(status().isNotFound());
        ajeno().perform(delete(ramaUrl)).andExpect(status().isNotFound());
    }

    @Test
    void torneoPublicado_respondeConflict() throws Exception {
        conFixtureProgramado();
        publicar(torneo);

        editar(categoriaUrl, U9_RENOMBRADA)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(containsString("primero hay que reabrirlo")));
        editar(ramaUrl, "{\"nombre\":\"Otra\"}").andExpect(status().isConflict());
    }
}

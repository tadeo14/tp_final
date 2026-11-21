package ar.edu.utn.fixture.controlador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Editar y borrar zonas (D66). Torneo chico de PRUEBA: U9 masculino, una zona con 4 equipos y 6 partidos programados.
 */
class EdicionZonasApiTest extends EdicionApiTestBase {

    private long torneo;
    private long categoria;
    private long rama;
    private String zonaUrl;

    private void conFixtureProgramado() throws Exception {
        torneo = torneoChicoProgramado();
        JsonNode config = configuracion(torneo);
        categoria = config.get("categorias").get(0).get("id").asLong();
        rama = config.get("ramas").get(0).get("id").asLong();
        zonaUrl = "/api/torneos/" + torneo + "/zonas/" + config.get("zonas").get(0).get("id").asLong();
    }

    private String zona(long cat, long ram, String nombre) {
        return "{\"categoriaId\":%d,\"ramaId\":%d,\"nombre\":\"%s\"}".formatted(cat, ram, nombre);
    }

    @Test
    void renombrar_noAfectaElFixtureNiElSorteo() throws Exception {
        conFixtureProgramado();

        editar(zonaUrl, zona(categoria, rama, "Zona Única"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etiqueta").value("U9 Masculino - Zona Única"));
        assertThat(partidos(torneo)).isEqualTo(6);
        assertThat(equipos(torneo).get(0).get("zona").asText()).isEqualTo("U9 Masculino - Zona Única");
    }

    @Test
    void cambiarLaRamaDeUnaZonaConEquipos_pideConfirmacionYDeshaceElSorteo() throws Exception {
        conFixtureProgramado();
        long femenino = crearId("/api/torneos/" + torneo + "/ramas", "{\"nombre\":\"Femenino\"}");

        editar(zonaUrl, zona(categoria, femenino, "Zona A"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.requiereConfirmacion").value(true))
                .andExpect(jsonPath("$.error").value(containsString("invalida el sorteo de zonas")));
        assertThat(partidos(torneo)).isEqualTo(6);

        editar(zonaUrl + "?confirmar=true", zona(categoria, femenino, "Zona A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etiqueta").value("U9 Femenino - Zona A"));
        assertThat(partidos(torneo)).isZero();
        equipos(torneo).forEach(e -> assertThat(e.get("zona").isNull()).isTrue());
    }

    @Test
    void borrarUnaZonaVacia() throws Exception {
        conFixtureProgramado();
        long zonaB = crearId("/api/torneos/" + torneo + "/zonas", zona(categoria, rama, "Zona B"));

        borrar("/api/torneos/" + torneo + "/zonas/" + zonaB).andExpect(status().isNoContent());
        assertThat(configuracion(torneo).get("zonas")).hasSize(1);
        assertThat(partidos(torneo)).isEqualTo(6);
    }

    @Test
    void borrarUnaZonaConEquipos_pideConfirmacionYDeshaceElSorteo() throws Exception {
        conFixtureProgramado();

        borrar(zonaUrl)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(containsString(
                        "Borrar la zona U9 Masculino - Zona A invalida el sorteo de zonas: hay 4 equipos sorteados y 6 partidos generados")));
        assertThat(configuracion(torneo).get("zonas")).hasSize(1);

        borrar(zonaUrl + "?confirmar=true").andExpect(status().isNoContent());
        assertThat(configuracion(torneo).get("zonas")).isEmpty();
        assertThat(partidos(torneo)).isZero();
        assertThat(equipos(torneo)).hasSize(4); // los equipos siguen inscriptos, sin zona
    }

    @Test
    void torneoAjeno_respondeNotFound() throws Exception {
        conFixtureProgramado();

        editar(ajeno(), zonaUrl, zona(categoria, rama, "Otra")).andExpect(status().isNotFound());
        ajeno().perform(delete(zonaUrl + "?confirmar=true")).andExpect(status().isNotFound());
        assertThat(partidos(torneo)).isEqualTo(6);
    }

    @Test
    void torneoPublicado_respondeConflict() throws Exception {
        conFixtureProgramado();
        publicar(torneo);

        borrar(zonaUrl + "?confirmar=true")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(containsString("primero hay que reabrirlo")));
    }
}

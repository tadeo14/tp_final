package ar.edu.utn.fixture.controlador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.JsonNode;

import ar.edu.utn.fixture.repositorio.OrganizadorRepositorio;

/**
 * Endpoints de configuración del torneo. Los datos son DATOS DE PRUEBA.
 */
class ConfiguracionApiTest extends ApiTestBase {

    @Autowired
    private OrganizadorRepositorio organizadores;

    @Test
    void creaUnTorneoEnBorrador() throws Exception {
        long torneo = crearTorneoDePrueba();

        mvc.perform(get("/api/torneos/" + torneo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.turnosCadaMin").value(15));
    }

    @Test
    void laContraseniaSeGuardaComoHashYNoSeDevuelve() throws Exception {
        JsonNode creado = crear("/api/organizadores",
                "{\"nombre\":\"Org\",\"email\":\"hash@prueba.test\",\"contrasenia\":\"clave-de-prueba\"}");

        assertThat(creado.has("contrasenia")).isFalse();
        assertThat(creado.has("passwordHash")).isFalse();
        String guardado = organizadores.findById(creado.get("id").asLong()).orElseThrow().getPasswordHash();
        assertThat(guardado).startsWith("pbkdf2$").doesNotContain("clave-de-prueba");
    }

    @Test
    void pedidoConDatosInvalidos_respondeBadRequestConElDetalle() throws Exception {
        mvc.perform(post("/api/torneos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizadorId\": 1, \"nombre\": \"\", \"turnosCadaMin\": 0, \"recambioMin\": 10, \"descansoMinimoMin\": 90}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Hay datos inválidos en el pedido"))
                .andExpect(jsonPath("$.detalles.length()").value(2));
    }

    @Test
    void franjaQueTerminaAntesDeEmpezar_respondeBadRequest() throws Exception {
        long torneo = crearTorneoDePrueba();

        mvc.perform(post("/api/torneos/" + torneo + "/franjas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fecha\":\"2030-01-05\",\"desde\":\"20:00\",\"hasta\":\"09:00\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void equipoConCategoriaDeOtroTorneo_respondeNotFound() throws Exception {
        long torneo1 = crearTorneoDePrueba();
        long torneo2 = crearTorneoDePrueba();
        long categoriaDelTorneo2 = crearId("/api/torneos/" + torneo2 + "/categorias", CATEGORIA_U9);
        long rama = crearId("/api/torneos/" + torneo1 + "/ramas", "{\"nombre\":\"Masculino\"}");
        long club = crearId("/api/torneos/" + torneo1 + "/clubes", "{\"nombre\":\"Club de prueba\"}");

        mvc.perform(post("/api/torneos/" + torneo1 + "/equipos").contentType(MediaType.APPLICATION_JSON)
                        .content(equipo(club, categoriaDelTorneo2, rama, "Equipo 1")))
                .andExpect(status().isNotFound());
    }

    @Test
    void listaDeEquipos_noMuestraDatosDeContacto() throws Exception {
        long torneo = crearTorneoDePrueba();
        long categoria = crearId("/api/torneos/" + torneo + "/categorias", CATEGORIA_U9);
        long rama = crearId("/api/torneos/" + torneo + "/ramas", "{\"nombre\":\"Masculino\"}");
        long club = crearId("/api/torneos/" + torneo + "/clubes", "{\"nombre\":\"Club de prueba\"}");
        crear("/api/torneos/" + torneo + "/equipos", equipo(club, categoria, rama, "Equipo 1"));

        String respuesta = mvc.perform(get("/api/torneos/" + torneo + "/equipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Equipo 1"))
                .andExpect(jsonPath("$[0].zona").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        assertThat(respuesta).doesNotContain("0000-0000").doesNotContain("Responsable");
    }

    @Test
    void nombreRepetido_respondeConflict() throws Exception {
        long torneo = crearTorneoDePrueba();
        crear("/api/torneos/" + torneo + "/ramas", "{\"nombre\":\"Masculino\"}");

        mvc.perform(post("/api/torneos/" + torneo + "/ramas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Masculino\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void torneoInexistente_respondeNotFound() throws Exception {
        mvc.perform(get("/api/torneos/999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No existe el torneo 999999999"));
    }

    static final String CATEGORIA_U9 = """
            {"nombre":"U9","minutosNetos":28,"cantidadCuartos":4,"pausasMin":4,"margenDetencionesMin":10,"maxPartidosDia":3}
            """;

    static String equipo(long club, long categoria, long rama, String nombre) {
        return """
                {"clubId": %d, "categoriaId": %d, "ramaId": %d, "nombre": "%s",
                 "responsableNombre": "Responsable de prueba", "responsableTelefono": "0000-0000"}
                """.formatted(club, categoria, rama, nombre);
    }
}

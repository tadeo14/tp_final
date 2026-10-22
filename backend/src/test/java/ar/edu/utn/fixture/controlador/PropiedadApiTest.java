package ar.edu.utn.fixture.controlador;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Cada organizador solo ve y edita sus torneos (D45). Un torneo ajeno responde 404, igual que uno que no existe,
 * para no revelar que existe. Datos de PRUEBA.
 */
class PropiedadApiTest extends ApiTestBase {

    /** El organizador A es el de ApiTestBase ({@code mvc}); B es otro organizador. */
    private MockMvc organizadorB;

    private long torneoDeA;
    private long sedeDeA;

    @BeforeEach
    void crearTorneoDeAYOrganizadorB() throws Exception {
        torneoDeA = crearTorneoDePrueba();
        sedeDeA = crearId("/api/torneos/" + torneoDeA + "/sedes", "{\"nombre\":\"Sede de A\"}");
        organizadorB = mvcCon(nuevoOrganizador().token());
    }

    @Test
    void elOrganizadorBNoVeElTorneoDeA_yRecibeElMismo404QueUnoInexistente() throws Exception {
        organizadorB.perform(get("/api/torneos/" + torneoDeA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No existe el torneo " + torneoDeA));
        organizadorB.perform(get("/api/torneos/999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No existe el torneo 999999999"));
    }

    @Test
    void elOrganizadorBNoPuedeConfigurarElTorneoDeA() throws Exception {
        organizadorB.perform(post("/api/torneos/" + torneoDeA + "/franjas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fecha\":\"2030-01-05\",\"desde\":\"09:00\",\"hasta\":\"20:00\"}"))
                .andExpect(status().isNotFound());
        organizadorB.perform(post("/api/torneos/" + torneoDeA + "/ramas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Masculino\"}"))
                .andExpect(status().isNotFound());
        organizadorB.perform(post("/api/sedes/" + sedeDeA + "/canchas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Cancha intrusa\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No existe la sede " + sedeDeA));
        organizadorB.perform(get("/api/torneos/" + torneoDeA + "/equipos"))
                .andExpect(status().isNotFound());
    }

    @Test
    void elOrganizadorBNoPuedeArmarNiPublicarElFixtureDeA() throws Exception {
        String base = "/api/torneos/" + torneoDeA;
        organizadorB.perform(post(base + "/sorteo")).andExpect(status().isNotFound());
        organizadorB.perform(post(base + "/fixture/generar")).andExpect(status().isNotFound());
        organizadorB.perform(post(base + "/fixture/programar")).andExpect(status().isNotFound());
        organizadorB.perform(get(base + "/fixture")).andExpect(status().isNotFound());
        organizadorB.perform(get(base + "/validacion")).andExpect(status().isNotFound());
        organizadorB.perform(post(base + "/publicar")).andExpect(status().isNotFound());
    }

    @Test
    void cadaOrganizadorListaSoloSusTorneos() throws Exception {
        crearTorneoDePrueba(); // A tiene 2

        mvc.perform(get("/api/torneos")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
        organizadorB.perform(get("/api/torneos")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void elDuenioSiAccedeASuTorneo() throws Exception {
        mvc.perform(get("/api/torneos/" + torneoDeA)).andExpect(status().isOk());
        mvc.perform(post("/api/sedes/" + sedeDeA + "/canchas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Cancha 1\"}"))
                .andExpect(status().isCreated());
    }
}

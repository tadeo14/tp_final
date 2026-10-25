package ar.edu.utn.fixture.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import ar.edu.utn.fixture.PropiedadesDePrueba;

/**
 * Rutas públicas y protegidas (D44).
 */
class SeguridadApiTest extends ApiTestBase {

    @Test
    void rutaProtegida_sinToken_responde401() throws Exception {
        long torneo = crearTorneoDePrueba();

        mvcSinToken.perform(get("/api/torneos/" + torneo))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Hay que iniciar sesión: falta el token o no es válido"));
        mvcSinToken.perform(post("/api/torneos/" + torneo + "/sorteo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rutaProtegida_conTokenInvalido_responde401() throws Exception {
        long torneo = crearTorneoDePrueba();

        mvcSinToken.perform(get("/api/torneos/" + torneo).header(HttpHeaders.AUTHORIZATION, "Bearer no-es-un-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rutaProtegida_conToken_responde200() throws Exception {
        long torneo = crearTorneoDePrueba();

        mvc.perform(get("/api/torneos/" + torneo)).andExpect(status().isOk());
    }

    @Test
    void rutasPublicas_noPidenToken() throws Exception {
        mvcSinToken.perform(get("/api/salud")).andExpect(status().isOk());
        // Llega al controlador: el torneo no existe (404), no se rechaza por falta de token (401).
        mvcSinToken.perform(get("/api/publico/torneos/999999999/equipos")).andExpect(status().isNotFound());
    }

    @Test
    void cors_aceptaElDominioDelFrontendConfigurado() throws Exception {
        mvcSinToken.perform(options("/api/torneos")
                        .header(HttpHeaders.ORIGIN, PropiedadesDePrueba.ORIGEN_FRONTEND)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, PropiedadesDePrueba.ORIGEN_FRONTEND));
    }

    @Test
    void cors_rechazaOtroDominio() throws Exception {
        mvcSinToken.perform(options("/api/torneos")
                        .header(HttpHeaders.ORIGIN, "https://otro-sitio.test")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden());
    }
}

package ar.edu.utn.fixture.controlador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.JsonNode;

import ar.edu.utn.fixture.repositorio.OrganizadorRepositorio;
import ar.edu.utn.fixture.servicio.JwtServicio;

/**
 * Registro y login del organizador. Datos de PRUEBA.
 */
class AuthApiTest extends ApiTestBase {

    @Autowired
    private OrganizadorRepositorio organizadores;

    @Autowired
    private JwtServicio jwt;

    private static final String REGISTRO = """
            {"nombre": "Organizador de prueba", "email": "ana@prueba.test", "contrasenia": "clave-de-prueba"}
            """;

    @Test
    void registro_guardaLaContraseniaComoHashYNoLaDevuelve() throws Exception {
        JsonNode creado = crear(mvcSinToken, "/api/auth/registro", REGISTRO);

        assertThat(creado.get("email").asText()).isEqualTo("ana@prueba.test");
        assertThat(creado.has("contrasenia")).isFalse();
        assertThat(creado.has("passwordHash")).isFalse();
        String guardado = organizadores.findById(creado.get("id").asLong()).orElseThrow().getPasswordHash();
        assertThat(guardado).startsWith("pbkdf2$").doesNotContain("clave-de-prueba");
    }

    @Test
    void registro_conEmailYaUsado_respondeConflict() throws Exception {
        crear(mvcSinToken, "/api/auth/registro", REGISTRO);

        mvcSinToken.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTRO.replace("ana@prueba.test", "ANA@Prueba.test")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Ya existe un organizador con ese email"));
    }

    @Test
    void registro_conContraseniaCorta_respondeBadRequest() throws Exception {
        mvcSinToken.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTRO.replace("clave-de-prueba", "corta")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_conLosDatosCorrectos_devuelveUnTokenDelOrganizador() throws Exception {
        long id = crear(mvcSinToken, "/api/auth/registro", REGISTRO).get("id").asLong();

        String respuesta = mvcSinToken.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"ana@prueba.test\", \"contrasenia\": \"clave-de-prueba\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiraEn").exists())
                .andExpect(jsonPath("$.organizador.id").value(id))
                .andReturn().getResponse().getContentAsString();

        String token = json.readTree(respuesta).get("token").asText();
        assertThat(jwt.organizadorDe(token)).contains(id);
    }

    @Test
    void login_conContraseniaIncorrecta_respondeUnauthorized() throws Exception {
        crear(mvcSinToken, "/api/auth/registro", REGISTRO);

        mvcSinToken.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"ana@prueba.test\", \"contrasenia\": \"otra-clave\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Email o contraseña incorrectos"));
    }

    @Test
    void login_conEmailInexistente_respondeElMismoMensaje() throws Exception {
        mvcSinToken.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"nadie@prueba.test\", \"contrasenia\": \"clave-de-prueba\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Email o contraseña incorrectos"));
    }
}

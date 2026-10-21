package ar.edu.utn.fixture.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.utn.fixture.PropiedadesDePrueba;
import jakarta.servlet.Filter;

/**
 * Base de los tests de la API: arranca la aplicación contra la base de DB_URL y cada test
 * hace rollback al terminar (@Transactional), así que no deja datos.
 *
 * <p>Antes de cada test registra un organizador de prueba e inicia sesión: {@code mvc} manda su token en
 * todos los pedidos. Para probar pedidos sin token se usa {@code mvcSinToken}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
abstract class ApiTestBase {

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) {
        PropiedadesDePrueba.registrar(registro);
    }

    /** Pedidos sin token (con los filtros de seguridad). */
    @Autowired
    protected MockMvc mvcSinToken;

    @Autowired
    protected ObjectMapper json;

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter seguridad;

    /** Pedidos con el token del organizador de prueba de este test. */
    protected MockMvc mvc;

    protected long organizadorId;

    @BeforeEach
    void iniciarSesion() throws Exception {
        Sesion sesion = nuevoOrganizador();
        organizadorId = sesion.organizadorId();
        mvc = mvcCon(sesion.token());
    }

    protected record Sesion(long organizadorId, String token) {
    }

    /** Registra un organizador de prueba nuevo e inicia sesión con él. */
    protected Sesion nuevoOrganizador() throws Exception {
        String email = "org" + System.nanoTime() + "@prueba.test";
        String registro = mvcSinToken.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Organizador de prueba\",\"email\":\"%s\",\"contrasenia\":\"clave-de-prueba\"}"
                                .formatted(email)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String login = mvcSinToken.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"contrasenia\":\"clave-de-prueba\"}".formatted(email)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return new Sesion(json.readTree(registro).get("id").asLong(), json.readTree(login).get("token").asText());
    }

    /** MockMvc que manda este token en todos los pedidos, pasando por los filtros de seguridad. */
    protected MockMvc mvcCon(String token) {
        return MockMvcBuilders.webAppContextSetup(contexto)
                .addFilters(seguridad)
                .defaultRequest(get("/").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .build();
    }

    /** POST que espera 201 y devuelve el cuerpo. */
    protected JsonNode crear(String url, String cuerpo) throws Exception {
        return crear(mvc, url, cuerpo);
    }

    protected JsonNode crear(MockMvc cliente, String url, String cuerpo) throws Exception {
        String respuesta = cliente.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(respuesta);
    }

    protected long crearId(String url, String cuerpo) throws Exception {
        return crear(url, cuerpo).get("id").asLong();
    }

    protected long crearTorneoDePrueba() throws Exception {
        return crearId("/api/torneos", """
                {"nombre": "Torneo de prueba", "turnosCadaMin": 15, "recambioMin": 10, "descansoMinimoMin": 90}
                """);
    }
}

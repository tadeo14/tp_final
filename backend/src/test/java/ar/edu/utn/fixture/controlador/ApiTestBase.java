package ar.edu.utn.fixture.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.utn.fixture.PropiedadesDePrueba;

/**
 * Base de los tests de la API: arranca la aplicación contra la base de DB_URL y cada test
 * hace rollback al terminar (@Transactional), así que no deja datos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
abstract class ApiTestBase {

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) {
        PropiedadesDePrueba.registrar(registro);
    }

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper json;

    /** POST que espera 201 y devuelve el cuerpo. */
    protected JsonNode crear(String url, String cuerpo) throws Exception {
        String respuesta = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(respuesta);
    }

    protected long crearId(String url, String cuerpo) throws Exception {
        return crear(url, cuerpo).get("id").asLong();
    }

    protected long crearTorneoDePrueba() throws Exception {
        long organizador = crearId("/api/auth/registro",
                "{\"nombre\":\"Organizador de prueba\",\"email\":\"org" + System.nanoTime() + "@prueba.test\",\"contrasenia\":\"clave-de-prueba\"}");
        return crearId("/api/torneos", """
                {"organizadorId": %d, "nombre": "Torneo de prueba",
                 "turnosCadaMin": 15, "recambioMin": 10, "descansoMinimoMin": 90}
                """.formatted(organizador));
    }
}

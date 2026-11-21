package ar.edu.utn.fixture.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Ayudas para los tests de edición y baja de la configuración (D66). Los datos son DATOS DE PRUEBA.
 */
abstract class EdicionApiTestBase extends ApiTestBase {

    protected ResultActions editar(String url, String cuerpo) throws Exception {
        return editar(mvc, url, cuerpo);
    }

    protected ResultActions editar(MockMvc cliente, String url, String cuerpo) throws Exception {
        return cliente.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    protected ResultActions borrar(String url) throws Exception {
        return mvc.perform(delete(url));
    }

    /** Un organizador distinto: para él, el torneo es ajeno (D45). */
    protected MockMvc ajeno() throws Exception {
        return mvcCon(nuevoOrganizador().token());
    }

    protected JsonNode configuracion(long torneo) throws Exception {
        return leer(get("/api/torneos/" + torneo + "/configuracion"));
    }

    protected JsonNode equipos(long torneo) throws Exception {
        return leer(get("/api/torneos/" + torneo + "/equipos"));
    }

    protected int partidos(long torneo) throws Exception {
        return leer(get("/api/torneos/" + torneo + "/fixture")).size();
    }

    protected void publicar(long torneo) throws Exception {
        mvc.perform(post("/api/torneos/" + torneo + "/publicar")).andExpect(status().isOk());
    }

    private JsonNode leer(RequestBuilder pedido) throws Exception {
        return json.readTree(mvc.perform(pedido).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }
}

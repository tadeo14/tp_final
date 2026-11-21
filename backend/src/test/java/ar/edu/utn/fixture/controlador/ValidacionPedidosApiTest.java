package ar.edu.utn.fixture.controlador;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/**
 * Los errores de validación de los pedidos salen en español, con el campo nombrado como en la pantalla (D61).
 * Los datos son DATOS DE PRUEBA.
 */
class ValidacionPedidosApiTest extends ApiTestBase {

    @Test
    void torneoInvalido_detallesEnEspanolConElNombreDelCampoDeLaPantalla() throws Exception {
        mvc.perform(post("/api/torneos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"\", \"turnosCadaMin\": 0, \"recambioMin\": -1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Hay datos inválidos en el pedido"))
                .andExpect(jsonPath("$.detalles", contains(
                        "Descanso mínimo (min): es obligatorio",
                        "Nombre: es obligatorio",
                        "Recambio (min): no puede ser negativo",
                        "Turnos cada (min): tiene que ser mayor que 0")))
                .andExpect(content().string(not(containsString("must"))));
    }

    @Test
    void registroInvalido_emailYContraseniaEnEspanol() throws Exception {
        mvcSinToken.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Organizador de prueba\", \"email\": \"sin-arroba\", \"contrasenia\": \"corta\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles", contains(
                        "Contraseña: debe tener al menos 8 caracteres",
                        "Email: no es un email válido")));
    }

    @Test
    void equipoYCategoriaInvalidos_camposDeLaConfiguracion() throws Exception {
        long torneo = crearTorneoDePrueba();
        String base = "/api/torneos/" + torneo;

        mvc.perform(post(base + "/equipos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Equipo de prueba\", \"responsableNombre\": \" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles", contains(
                        "Categoría: es obligatorio",
                        "Club: es obligatorio",
                        "Rama: es obligatorio",
                        "Responsable: es obligatorio",
                        "Teléfono del responsable: es obligatorio")));

        mvc.perform(post(base + "/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "U9", "minutosNetos": 0, "cantidadCuartos": 4, "pausasMin": -1,
                                 "margenDetencionesMin": 10, "maxPartidosDia": 3}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles", contains(
                        "Minutos de juego: tiene que ser mayor que 0",
                        "Pausas (min, en total): no puede ser negativo")));

        mvc.perform(post(base + "/franjas").contentType(MediaType.APPLICATION_JSON).content("{\"desde\": \"09:00\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles", hasItem("Día: es obligatorio")))
                .andExpect(jsonPath("$.detalles", hasItem("Hasta: es obligatorio")));
    }
}

package ar.edu.utn.fixture.controlador;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/**
 * Recorrido completo de CLAUDE.md por la API, con un torneo chico de prueba:
 * crear torneo → categoría → equipos → canchas y franjas → sortear → generar → programar → validar → publicar
 * → consultar por equipo. Además, los estados BORRADOR/PUBLICADO y la regla D20. Datos de PRUEBA.
 *
 * <p>Los pasos del organizador van con su token ({@code mvc}); la consulta pública, sin token ({@code mvcSinToken}).
 */
class RecorridoApiTest extends ApiTestBase {

    private long torneo;
    private List<Long> equiposU9 = new ArrayList<>();

    /**
     * U9 masculino con 9 equipos en 2 zonas (5 + 4), 2 canchas, sábado de 9 a 20 y domingo de 9 a 16.
     * Hace falta el domingo: con tope 3 por día, los equipos de la zona de 5 no llegan a sus 4 partidos en un solo día.
     */
    private void configurarTorneoChico() throws Exception {
        configurarTorneoChico("20:00");
        crear("/api/torneos/" + torneo + "/franjas", "{\"fecha\":\"2030-01-06\",\"desde\":\"09:00\",\"hasta\":\"16:00\"}");
    }

    /** Solo el sábado, de 9 a {@code cierre}. */
    private void configurarTorneoChico(String cierre) throws Exception {
        torneo = crearTorneoDePrueba();
        String base = "/api/torneos/" + torneo;
        crear(base + "/franjas", "{\"fecha\":\"2030-01-05\",\"desde\":\"09:00\",\"hasta\":\"" + cierre + "\"}");
        long sede = crearId(base + "/sedes", "{\"nombre\":\"Sede de prueba\"}");
        crear("/api/sedes/" + sede + "/canchas", "{\"nombre\":\"Cancha 1\"}");
        crear("/api/sedes/" + sede + "/canchas", "{\"nombre\":\"Cancha 2\"}");
        long categoria = crearId(base + "/categorias", ConfiguracionApiTest.CATEGORIA_U9);
        long rama = crearId(base + "/ramas", "{\"nombre\":\"Masculino\"}");
        long club = crearId(base + "/clubes", "{\"nombre\":\"Club de prueba\"}");
        for (String zona : List.of("Zona A", "Zona B")) {
            crear(base + "/zonas", "{\"categoriaId\":%d,\"ramaId\":%d,\"nombre\":\"%s\"}".formatted(categoria, rama, zona));
        }
        for (int i = 1; i <= 9; i++) {
            equiposU9.add(crearId(base + "/equipos", ConfiguracionApiTest.equipo(club, categoria, rama, "Equipo " + i)));
        }
    }

    @Test
    void recorridoCompleto_hastaConsultarElFixtureDeUnEquipo() throws Exception {
        configurarTorneoChico();
        String base = "/api/torneos/" + torneo;

        mvc.perform(post(base + "/sorteo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].zona").value("U9 Masculino - Zona A"))
                .andExpect(jsonPath("$[0].equipos", hasSize(5)))
                .andExpect(jsonPath("$[1].equipos", hasSize(4)));

        // 5 equipos → 10 partidos, 4 equipos → 6 partidos.
        mvc.perform(post(base + "/fixture/generar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadPartidos").value(16));

        mvc.perform(post(base + "/fixture/programar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(16)))
                .andExpect(jsonPath("$[0].cancha").exists())
                .andExpect(jsonPath("$[0].fin").exists());

        mvc.perform(get(base + "/validacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true))
                .andExpect(jsonPath("$.violaciones", hasSize(0)));

        // Antes de publicar, el público (sin token) no ve el torneo.
        mvcSinToken.perform(get("/api/publico/torneos/" + torneo + "/equipos"))
                .andExpect(status().isNotFound());

        mvc.perform(post(base + "/publicar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PUBLICADO"));

        mvcSinToken.perform(get("/api/publico/torneos/" + torneo + "/equipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version.numero").value(1))
                .andExpect(jsonPath("$.equipos", hasSize(9)));

        String zonaDelPrimero = json.readTree(mvcSinToken.perform(get("/api/publico/torneos/" + torneo + "/equipos"))
                .andReturn().getResponse().getContentAsString()).get("equipos").get(0).get("zona").asText();
        boolean zonaDeCinco = zonaDelPrimero.endsWith("Zona A");
        mvcSinToken.perform(get("/api/publico/torneos/" + torneo + "/equipos/" + equiposU9.get(0) + "/fixture"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipo").value("Equipo 1"))
                .andExpect(jsonPath("$.version.numero").value(1))
                .andExpect(jsonPath("$.partidos", hasSize(zonaDeCinco ? 4 : 3)))
                .andExpect(jsonPath("$.rondasLibres", hasSize(zonaDeCinco ? 1 : 0)))
                .andExpect(jsonPath("$.partidos[0].rival").exists())
                .andExpect(jsonPath("$.partidos[0].sede").value("Sede de prueba"));
    }

    @Test
    void d20_conElTorneoPublicadoNoSePuedeSortearGenerarNiProgramar() throws Exception {
        configurarTorneoChico();
        String base = "/api/torneos/" + torneo;
        mvc.perform(post(base + "/sorteo")).andExpect(status().isOk());
        mvc.perform(post(base + "/fixture/generar")).andExpect(status().isOk());
        mvc.perform(post(base + "/fixture/programar")).andExpect(status().isOk());
        mvc.perform(post(base + "/publicar")).andExpect(status().isOk());

        mvc.perform(post(base + "/sorteo")).andExpect(status().isConflict());
        mvc.perform(post(base + "/fixture/generar")).andExpect(status().isConflict());
        mvc.perform(post(base + "/fixture/programar")).andExpect(status().isConflict());
        mvc.perform(post(base + "/publicar")).andExpect(status().isConflict());
        mvc.perform(post(base + "/ramas").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Femenino\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El torneo " + torneo + " está publicado: no se puede modificar (D20)"));
    }

    @Test
    void noSePuedePublicarSinProgramar() throws Exception {
        configurarTorneoChico();
        String base = "/api/torneos/" + torneo;
        mvc.perform(post(base + "/sorteo")).andExpect(status().isOk());
        mvc.perform(post(base + "/fixture/generar")).andExpect(status().isOk());

        // Partidos sin cancha ni horario: violan R8.
        mvc.perform(post(base + "/publicar"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detalles[0]").value(org.hamcrest.Matchers.startsWith("R8")));
        mvc.perform(get(base)).andExpect(jsonPath("$.estado").value("BORRADOR"));
    }

    @Test
    void generarSinSortear_respondeBadRequest() throws Exception {
        configurarTorneoChico();

        mvc.perform(post("/api/torneos/" + torneo + "/fixture/generar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Primero hay que sortear las zonas: hay 9 equipos sin zona"));
    }

    @Test
    void conUnaFranjaDeUnaHora_respondeQueNoExisteProgramacionValidaYNombraLaZona() throws Exception {
        configurarTorneoChico("10:00");
        String base = "/api/torneos/" + torneo;
        mvc.perform(post(base + "/sorteo")).andExpect(status().isOk());
        mvc.perform(post(base + "/fixture/generar")).andExpect(status().isOk());

        // En una hora cada equipo juega como máximo 1 partido y necesita 3 o 4.
        mvc.perform(post(base + "/fixture/programar"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("No existe una programación válida con la configuración actual"))
                .andExpect(jsonPath("$.detalles[0]").value(org.hamcrest.Matchers.startsWith("Zona U9 Masculino - Zona")));
        // No se guardó nada.
        mvc.perform(get(base + "/fixture")).andExpect(jsonPath("$[0].cancha").doesNotExist());
    }

    @Test
    void programarSinGenerar_respondeBadRequest() throws Exception {
        configurarTorneoChico();

        mvc.perform(post("/api/torneos/" + torneo + "/fixture/programar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Primero hay que generar los enfrentamientos"));
    }

    @Test
    void sortearSinZonasDefinidas_respondeBadRequest() throws Exception {
        long t = crearTorneoDePrueba();
        String base = "/api/torneos/" + t;
        long categoria = crearId(base + "/categorias", ConfiguracionApiTest.CATEGORIA_U9);
        long rama = crearId(base + "/ramas", "{\"nombre\":\"Femenino\"}");
        long club = crearId(base + "/clubes", "{\"nombre\":\"Club de prueba\"}");
        crear(base + "/equipos", ConfiguracionApiTest.equipo(club, categoria, rama, "Equipo 1"));

        mvc.perform(post(base + "/sorteo"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Falta definir las zonas de U9 Femenino"));
    }
}

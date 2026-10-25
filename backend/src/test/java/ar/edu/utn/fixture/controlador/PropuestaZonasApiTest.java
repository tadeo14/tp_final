package ar.edu.utn.fixture.controlador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * "Proponer zonas" (D69): vista previa y creación. Los datos son DATOS DE PRUEBA, salvo el caso real.
 */
class PropuestaZonasApiTest extends EdicionApiTestBase {

    private static final String MAXIMO_5 = "{\"maximoEquiposPorZona\": 5}";

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private DataSource dataSource;

    private ResultActions vistaPrevia(MockMvc cliente, long torneo, String cuerpo) throws Exception {
        return cliente.perform(post("/api/torneos/" + torneo + "/zonas/propuesta").contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    private ResultActions crearZonas(MockMvc cliente, long torneo, String cuerpo, boolean confirmar) throws Exception {
        return cliente.perform(post("/api/torneos/" + torneo + "/zonas/propuesta/crear" + (confirmar ? "?confirmar=true" : ""))
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    /** U9 masculino con 11 equipos, U9 femenino con 1 y la categoría U11 sin equipos. Sin zonas. */
    private long torneoSinZonas() throws Exception {
        long torneo = crearTorneoDePrueba();
        String base = "/api/torneos/" + torneo;
        long u9 = crearId(base + "/categorias", ConfiguracionApiTest.CATEGORIA_U9);
        crear(base + "/categorias", ConfiguracionApiTest.CATEGORIA_U9.replace("U9", "U11"));
        long masculino = crearId(base + "/ramas", "{\"nombre\":\"Masculino\"}");
        long femenino = crearId(base + "/ramas", "{\"nombre\":\"Femenino\"}");
        long club = crearId(base + "/clubes", "{\"nombre\":\"Club de prueba\"}");
        for (int i = 1; i <= 11; i++) {
            crear(base + "/equipos", ConfiguracionApiTest.equipo(club, u9, masculino, "Equipo " + i));
        }
        crear(base + "/equipos", ConfiguracionApiTest.equipo(club, u9, femenino, "Equipo solo"));
        return torneo;
    }

    @Test
    void vistaPrevia_proponeLasZonasYNoGuardaNada_avisaSiHayUnSoloEquipoYOmiteLosGruposVacios() throws Exception {
        long torneo = torneoSinZonas();

        vistaPrevia(mvc, torneo, MAXIMO_5)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maximoEquiposPorZona").value(5))
                .andExpect(jsonPath("$.totalZonas").value(3))
                .andExpect(jsonPath("$.reemplazaZonas").value(false))
                .andExpect(jsonPath("$.grupos", hasSize(2))) // U11 no tiene equipos: se omite
                .andExpect(jsonPath("$.grupos[0].categoria").value("U9"))
                .andExpect(jsonPath("$.grupos[0].rama").value("Masculino"))
                .andExpect(jsonPath("$.grupos[0].equipos").value(11))
                .andExpect(jsonPath("$.grupos[0].zonas[*].nombre", contains("Zona A", "Zona B", "Zona C")))
                .andExpect(jsonPath("$.grupos[0].zonas[*].equipos", contains(4, 4, 3)))
                .andExpect(jsonPath("$.grupos[1].rama").value("Femenino"))
                .andExpect(jsonPath("$.grupos[1].zonas", hasSize(0)))
                .andExpect(jsonPath("$.grupos[1].aviso").value(containsString("Hay un solo equipo")));
        assertThat(configuracion(torneo).get("zonas")).isEmpty();
    }

    @Test
    void crear_guardaLasZonasYElSorteoLasUsa() throws Exception {
        long torneo = torneoSinZonas();
        // Sin el equipo solo, para que el sorteo tenga zonas en todos los grupos.
        long solo = equipos(torneo).get(11).get("id").asLong();
        borrar("/api/torneos/" + torneo + "/equipos/" + solo).andExpect(status().isNoContent());

        crearZonas(mvc, torneo, MAXIMO_5, false)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[*].etiqueta", contains("U9 Masculino - Zona A", "U9 Masculino - Zona B", "U9 Masculino - Zona C")));
        assertThat(configuracion(torneo).get("zonas")).hasSize(3);

        // Las zonas son la estructura; el sorteo reparte: 4 + 4 + 3.
        JsonNode sorteo = json.readTree(mvc.perform(post("/api/torneos/" + torneo + "/sorteo"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        List<Integer> tamanios = new ArrayList<>();
        sorteo.forEach(z -> tamanios.add(z.get("equipos").size()));
        assertThat(tamanios).containsExactly(4, 4, 3);
    }

    @Test
    void casoReal_conMaximo5_proponeLas13ZonasDelCasoReal() throws Exception {
        crear(mvcSinToken, "/api/auth/registro",
                "{\"nombre\":\"Organizador de ejemplo\",\"email\":\"ejemplo@fixture.test\",\"contrasenia\":\"clave-de-prueba\"}");
        String login = mvcSinToken.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ejemplo@fixture.test\",\"contrasenia\":\"clave-de-prueba\"}"))
                .andReturn().getResponse().getContentAsString();
        MockMvc organizador = mvcCon(json.readTree(login).get("token").asText());
        ScriptUtils.executeSqlScript(DataSourceUtils.getConnection(dataSource), new FileSystemResource("../database/caso_real.sql"));
        long torneo = jdbc.queryForObject("select currval(pg_get_serial_sequence('torneo', 'id'))", Long.class);

        // El script ya carga las 13 zonas elegidas: la propuesta tiene que coincidir grupo por grupo.
        JsonNode propuesta = json.readTree(vistaPrevia(organizador, torneo, MAXIMO_5)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalZonas").value(13))
                .andExpect(jsonPath("$.reemplazaZonas").value(true))
                .andReturn().getResponse().getContentAsString());
        List<String> resumen = new ArrayList<>();
        propuesta.get("grupos").forEach(g -> {
            List<Integer> t = new ArrayList<>();
            g.get("zonas").forEach(z -> t.add(z.get("equipos").asInt()));
            resumen.add(g.get("categoria").asText() + " " + g.get("rama").asText() + " " + t);
            assertThat(g.get("zonas").size()).isEqualTo(g.get("zonasActuales").size());
        });
        assertThat(resumen).containsExactly("U9 Masculino [5, 5]", "U9 Femenino [5]", "U11 Masculino [4, 4, 4]",
                "U11 Femenino [5, 4]", "U13 Masculino [4, 4]", "U13 Femenino [4, 4, 3]");

        // Reemplazarlas pide confirmación; confirmando, el recorrido sigue dando 91 partidos.
        crearZonas(organizador, torneo, MAXIMO_5, false).andExpect(status().isConflict())
                .andExpect(jsonPath("$.requiereConfirmacion").value(true));
        crearZonas(organizador, torneo, MAXIMO_5, true).andExpect(status().isCreated()).andExpect(jsonPath("$", hasSize(13)));
        organizador.perform(post("/api/torneos/" + torneo + "/sorteo")).andExpect(status().isOk());
        organizador.perform(post("/api/torneos/" + torneo + "/fixture/generar"))
                .andExpect(jsonPath("$.cantidadPartidos").value(91));
    }

    @Test
    void reemplazarZonasConSorteoYPartidos_pideConfirmacionYAlConfirmarDeshaceElSorteo() throws Exception {
        long torneo = torneoChicoProgramado(); // U9 masculino: 1 zona con 4 equipos, 6 partidos
        long zonaVieja = configuracion(torneo).get("zonas").get(0).get("id").asLong();

        vistaPrevia(mvc, torneo, MAXIMO_5)
                .andExpect(jsonPath("$.reemplazaZonas").value(true))
                .andExpect(jsonPath("$.grupos[0].zonasActuales", contains("Zona A")));
        crearZonas(mvc, torneo, MAXIMO_5, false)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.requiereConfirmacion").value(true))
                .andExpect(jsonPath("$.error").value("Ya hay zonas en U9 Masculino (Zona A). Si confirmás, se reemplazan por las "
                        + "propuestas, se deshace el sorteo de todo el torneo (4 equipos sorteados) y se borran los 6 partidos generados."));
        assertThat(partidos(torneo)).isEqualTo(6);
        assertThat(configuracion(torneo).get("zonas").get(0).get("id").asLong()).isEqualTo(zonaVieja);

        crearZonas(mvc, torneo, MAXIMO_5, true).andExpect(status().isCreated());
        assertThat(partidos(torneo)).isZero();
        JsonNode zonas = configuracion(torneo).get("zonas");
        assertThat(zonas).hasSize(1);
        assertThat(zonas.get(0).get("id").asLong()).isNotEqualTo(zonaVieja);
        assertThat(equipos(torneo)).hasSize(4); // los equipos no se tocan
        equipos(torneo).forEach(e -> assertThat(e.get("zona").isNull()).isTrue());
    }

    @Test
    void maximoMenorQue3_respondeBadRequestEnEspanol() throws Exception {
        long torneo = torneoSinZonas();

        vistaPrevia(mvc, torneo, "{\"maximoEquiposPorZona\": 2}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0]").value(
                        "Máximo de equipos por zona: tiene que ser al menos 3 (con 2 podría quedar una zona de un solo equipo)"));
        vistaPrevia(mvc, torneo, "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0]").value("Máximo de equipos por zona: es obligatorio"));
    }

    @Test
    void torneoAjeno_respondeNotFound() throws Exception {
        long torneo = torneoSinZonas();
        MockMvc otro = ajeno();

        vistaPrevia(otro, torneo, MAXIMO_5).andExpect(status().isNotFound());
        crearZonas(otro, torneo, MAXIMO_5, true).andExpect(status().isNotFound());
        assertThat(configuracion(torneo).get("zonas")).isEmpty();
    }

    @Test
    void torneoPublicado_respondeConflictYPideReabrir() throws Exception {
        long torneo = torneoChicoProgramado();
        publicar(torneo);

        vistaPrevia(mvc, torneo, MAXIMO_5)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(containsString("primero hay que reabrirlo")));
        crearZonas(mvc, torneo, MAXIMO_5, true).andExpect(status().isConflict());
        mvc.perform(get("/api/torneos/" + torneo + "/fixture")).andExpect(jsonPath("$", hasSize(6)));
    }
}

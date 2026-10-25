package ar.edu.utn.fixture.controlador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Editar y dar de baja equipos (D66). Torneo chico de PRUEBA: U9 masculino, 4 equipos en una zona, 6 partidos
 * programados el sábado de 9 a 20.
 */
class EdicionEquiposApiTest extends EdicionApiTestBase {

    private long torneo;
    private String base;
    private long club;
    private long categoria;
    private long rama;

    private void conFixtureProgramado() throws Exception {
        torneo = torneoChicoProgramado();
        base = "/api/torneos/" + torneo;
        JsonNode config = configuracion(torneo);
        club = config.get("clubes").get(0).get("id").asLong();
        categoria = config.get("categorias").get(0).get("id").asLong();
        rama = config.get("ramas").get(0).get("id").asLong();
    }

    private long equipoId(int i) throws Exception {
        return equipos(torneo).get(i).get("id").asLong();
    }

    @Test
    void editarNombreClubYResponsable_noAfectaElFixture() throws Exception {
        conFixtureProgramado();
        long otroClub = crearId(base + "/clubes", "{\"nombre\":\"Otro club de prueba\"}");

        editar(base + "/equipos/" + equipoId(0), ConfiguracionApiTest.equipo(otroClub, categoria, rama, "Equipo 1 renombrado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Equipo 1 renombrado"))
                .andExpect(jsonPath("$.club").value("Otro club de prueba"))
                .andExpect(jsonPath("$.zona").value("U9 Masculino - Zona A"));
        assertThat(partidos(torneo)).isEqualTo(6);
    }

    @Test
    void borrar_sinFixture_daDeBajaElEquipo() throws Exception {
        long t = crearTorneoDePrueba();
        String b = "/api/torneos/" + t;
        long cat = crearId(b + "/categorias", ConfiguracionApiTest.CATEGORIA_U9);
        long ram = crearId(b + "/ramas", "{\"nombre\":\"Masculino\"}");
        long clu = crearId(b + "/clubes", "{\"nombre\":\"Club de prueba\"}");
        long equipo = crearId(b + "/equipos", ConfiguracionApiTest.equipo(clu, cat, ram, "Equipo 1"));

        borrar(b + "/equipos/" + equipo).andExpect(status().isNoContent());
        assertThat(equipos(t)).isEmpty();
    }

    @Test
    void torneoAjeno_respondeNotFound() throws Exception {
        conFixtureProgramado();
        String url = base + "/equipos/" + equipoId(0);

        editar(ajeno(), url, ConfiguracionApiTest.equipo(club, categoria, rama, "Otro nombre")).andExpect(status().isNotFound());
        ajeno().perform(delete(url + "?confirmar=true")).andExpect(status().isNotFound());
        assertThat(equipos(torneo)).hasSize(4);
    }

    @Test
    void torneoPublicado_respondeConflict() throws Exception {
        conFixtureProgramado();
        publicar(torneo);

        borrar(base + "/equipos/" + equipoId(0) + "?confirmar=true")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(containsString("primero hay que reabrirlo")));
        assertThat(equipos(torneo)).hasSize(4);
    }

    /** Pedido de la consigna: la zona queda con un equipo menos y generar y programar siguen funcionando. */
    @Test
    void darDeBajaConPartidos_pideConfirmacionYConservaElSorteo_laZonaQuedaConUnoMenos() throws Exception {
        conFixtureProgramado();
        String url = base + "/equipos/" + equipoId(0);

        borrar(url)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.requiereConfirmacion").value(true))
                .andExpect(jsonPath("$.error").value(containsString("Dar de baja el equipo Equipo 1 invalida el fixture: hay 6 partidos generados")));
        assertThat(equipos(torneo)).hasSize(4);

        borrar(url + "?confirmar=true").andExpect(status().isNoContent());
        assertThat(partidos(torneo)).isZero();
        JsonNode quedan = equipos(torneo);
        assertThat(quedan).hasSize(3);
        quedan.forEach(e -> assertThat(e.get("zona").asText()).isEqualTo("U9 Masculino - Zona A")); // sorteo conservado

        // Zona de 3: 3 rondas con un libre cada una, 3 partidos.
        mvc.perform(post(base + "/fixture/generar")).andExpect(status().isOk()).andExpect(jsonPath("$.cantidadPartidos").value(3));
        mvc.perform(post(base + "/fixture/programar")).andExpect(status().isOk());
        mvc.perform(get(base + "/validacion")).andExpect(jsonPath("$.valido").value(true));
        publicar(torneo);
    }

    @Test
    void darDeBaja_siLaZonaQuedaConUnSoloEquipo_generarLoInformaConClaridad() throws Exception {
        long t = crearTorneoDePrueba();
        String b = "/api/torneos/" + t;
        crear(b + "/franjas", "{\"fecha\":\"2030-01-05\",\"desde\":\"09:00\",\"hasta\":\"20:00\"}");
        long sede = crearId(b + "/sedes", "{\"nombre\":\"Sede de prueba\"}");
        crear("/api/sedes/" + sede + "/canchas", "{\"nombre\":\"Cancha 1\"}");
        long cat = crearId(b + "/categorias", ConfiguracionApiTest.CATEGORIA_U9);
        long ram = crearId(b + "/ramas", "{\"nombre\":\"Masculino\"}");
        long clu = crearId(b + "/clubes", "{\"nombre\":\"Club de prueba\"}");
        crear(b + "/zonas", "{\"categoriaId\":%d,\"ramaId\":%d,\"nombre\":\"Zona A\"}".formatted(cat, ram));
        long equipo1 = crearId(b + "/equipos", ConfiguracionApiTest.equipo(clu, cat, ram, "Equipo 1"));
        crear(b + "/equipos", ConfiguracionApiTest.equipo(clu, cat, ram, "Equipo 2"));
        mvc.perform(post(b + "/sorteo")).andExpect(status().isOk());
        mvc.perform(post(b + "/fixture/generar")).andExpect(status().isOk());

        borrar(b + "/equipos/" + equipo1 + "?confirmar=true").andExpect(status().isNoContent());

        mvc.perform(post(b + "/fixture/generar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Zona U9 Masculino - Zona A: Una zona necesita al menos 2 equipos"));
    }

    @Test
    void cambiarLaRamaDeUnEquipoSorteado_pideConfirmacionYDeshaceElSorteo() throws Exception {
        conFixtureProgramado();
        long femenino = crearId(base + "/ramas", "{\"nombre\":\"Femenino\"}");
        String url = base + "/equipos/" + equipoId(0);
        String pedido = ConfiguracionApiTest.equipo(club, categoria, femenino, "Equipo 1");

        editar(url, pedido)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.requiereConfirmacion").value(true))
                .andExpect(jsonPath("$.error").value(containsString("invalida el sorteo de zonas: hay 4 equipos sorteados y 6 partidos generados")));
        assertThat(partidos(torneo)).isEqualTo(6);

        editar(url + "?confirmar=true", pedido)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rama").value("Femenino"))
                .andExpect(jsonPath("$.zona").doesNotExist());
        assertThat(partidos(torneo)).isZero();
        equipos(torneo).forEach(e -> assertThat(e.get("zona").isNull()).isTrue());
    }

    @Test
    void darDeBajaDespuesDePublicar_noTocaLaVersionPublicada() throws Exception {
        conFixtureProgramado();
        publicar(torneo);
        mvc.perform(post(base + "/reabrir")).andExpect(status().isOk());

        borrar(base + "/equipos/" + equipoId(0) + "?confirmar=true").andExpect(status().isNoContent());

        mvcSinToken.perform(get("/api/publico/torneos/" + torneo + "/equipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version.numero").value(1))
                .andExpect(jsonPath("$.equipos.length()").value(4));
    }
}

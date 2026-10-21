package ar.edu.utn.fixture.controlador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.JsonNode;

import ar.edu.utn.fixture.entidad.Cancha;
import ar.edu.utn.fixture.entidad.Categoria;
import ar.edu.utn.fixture.entidad.Equipo;
import ar.edu.utn.fixture.repositorio.CanchaRepositorio;
import ar.edu.utn.fixture.repositorio.EquipoRepositorio;
import jakarta.persistence.EntityManager;

/**
 * Versiones publicadas del fixture (D46, D47). Datos de PRUEBA.
 */
class PublicacionApiTest extends ApiTestBase {

    @Autowired
    private EquipoRepositorio equipos;

    @Autowired
    private CanchaRepositorio canchas;

    @Autowired
    private EntityManager em;

    @Test
    void publicarCreaLaVersion1() throws Exception {
        long torneo = torneoChicoProgramado();

        mvc.perform(post("/api/torneos/" + torneo + "/publicar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PUBLICADO"))
                .andExpect(jsonPath("$.version.numero").value(1))
                .andExpect(jsonPath("$.version.publicadaEn").exists())
                .andExpect(jsonPath("$.version.motivo").doesNotExist());
    }

    @Test
    void laFotoPublicadaNoCambiaSiDespuesCambianLosEquiposLasCanchasOLaConfiguracion() throws Exception {
        long torneo = torneoChicoProgramado();
        mvc.perform(post("/api/torneos/" + torneo + "/publicar")).andExpect(status().isOk());
        Equipo equipo = equipos.findByTorneoIdOrderByIdAsc(torneo).get(0);
        String fixtureAntes = vistaPublica(torneo, equipo.getId());
        String equiposAntes = mvcSinToken.perform(get("/api/publico/torneos/" + torneo + "/equipos"))
                .andReturn().getResponse().getContentAsString();

        // Cambios en las tablas vivas, directo en la base (con el torneo publicado la API no los permite, D39).
        equipo.setNombre("Equipo con otro nombre");
        for (Cancha c : canchas.findBySedeTorneoIdAndHabilitadaTrueOrderByIdAsc(torneo)) {
            c.setNombre(c.getNombre() + " (renombrada)");
        }
        Categoria categoria = equipo.getCategoria();
        categoria.setMinutosNetos(40); // el bloque pasa de 60 a 75 minutos: el fin calculado cambiaría
        em.flush();

        // El organizador ve los cambios en las tablas vivas…
        JsonNode vivo = json.readTree(mvc.perform(get("/api/torneos/" + torneo + "/fixture"))
                .andReturn().getResponse().getContentAsString());
        assertThat(vivo.toString()).contains("Equipo con otro nombre").contains("(renombrada)");

        // …pero el público sigue viendo exactamente lo que se publicó.
        assertThat(vistaPublica(torneo, equipo.getId())).isEqualTo(fixtureAntes);
        assertThat(mvcSinToken.perform(get("/api/publico/torneos/" + torneo + "/equipos"))
                .andReturn().getResponse().getContentAsString()).isEqualTo(equiposAntes);
        assertThat(fixtureAntes).doesNotContain("Equipo con otro nombre").doesNotContain("(renombrada)");
    }

    @Test
    void unTorneoNuncaPublicadoNoSeVeEnLaVistaPublica() throws Exception {
        long torneo = torneoChicoProgramado();

        mvcSinToken.perform(get("/api/publico/torneos/" + torneo + "/equipos")).andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------- reabrir y republicar

    @Test
    void reabrir_elPublicoSigueViendoLaV1HastaQueSeRepublicaYDespuesVeLaV2() throws Exception {
        long torneo = torneoChicoProgramado();
        String base = "/api/torneos/" + torneo;
        mvc.perform(post(base + "/publicar")).andExpect(status().isOk());
        Equipo equipo = equipos.findByTorneoIdOrderByIdAsc(torneo).get(0);
        String nombreOriginal = equipo.getNombre();

        mvc.perform(post(base + "/reabrir"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("BORRADOR"));

        // Corrección del organizador: se corrige el nombre de un equipo y se vuelve a programar.
        equipo.setNombre("Equipo corregido");
        em.flush();
        mvc.perform(post(base + "/fixture/programar")).andExpect(status().isOk());

        // Mientras está en BORRADOR, el público sigue viendo la versión 1, sin la corrección.
        mvcSinToken.perform(get("/api/publico/torneos/" + torneo + "/equipos/" + equipo.getId() + "/fixture"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version.numero").value(1))
                .andExpect(jsonPath("$.equipo").value(nombreOriginal));

        mvc.perform(post(base + "/publicar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"Corrección del nombre de un equipo\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PUBLICADO"))
                .andExpect(jsonPath("$.version.numero").value(2))
                .andExpect(jsonPath("$.version.motivo").value("Corrección del nombre de un equipo"));

        // Después de republicar, el público ve la versión 2 con su motivo.
        mvcSinToken.perform(get("/api/publico/torneos/" + torneo + "/equipos/" + equipo.getId() + "/fixture"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version.numero").value(2))
                .andExpect(jsonPath("$.version.motivo").value("Corrección del nombre de un equipo"))
                .andExpect(jsonPath("$.equipo").value("Equipo corregido"));
    }

    @Test
    void reabrir_permiteVolverACambiarLaConfiguracion() throws Exception {
        long torneo = torneoChicoProgramado();
        String base = "/api/torneos/" + torneo;
        mvc.perform(post(base + "/publicar")).andExpect(status().isOk());
        mvc.perform(post(base + "/ramas").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Femenino\"}"))
                .andExpect(status().isConflict());

        mvc.perform(post(base + "/reabrir")).andExpect(status().isOk());

        mvc.perform(post(base + "/ramas").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Femenino\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void republicarSinMotivo_respondeBadRequest() throws Exception {
        long torneo = torneoChicoProgramado();
        String base = "/api/torneos/" + torneo;
        mvc.perform(post(base + "/publicar")).andExpect(status().isOk());
        mvc.perform(post(base + "/reabrir")).andExpect(status().isOk());

        mvc.perform(post(base + "/publicar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La versión 2 corrige un fixture ya publicado: hay que indicar el motivo"));
        mvc.perform(post(base + "/publicar").contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"   \"}"))
                .andExpect(status().isBadRequest());
        // No se publicó nada: sigue en BORRADOR y el público ve la versión 1.
        mvc.perform(get(base)).andExpect(jsonPath("$.estado").value("BORRADOR"));
        mvcSinToken.perform(get("/api/publico/torneos/" + torneo + "/equipos"))
                .andExpect(jsonPath("$.version.numero").value(1));
    }

    @Test
    void reabrirUnTorneoAjeno_respondeNotFound() throws Exception {
        long torneo = torneoChicoProgramado();
        mvc.perform(post("/api/torneos/" + torneo + "/publicar")).andExpect(status().isOk());

        mvcCon(nuevoOrganizador().token()).perform(post("/api/torneos/" + torneo + "/reabrir"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No existe el torneo " + torneo));
        mvc.perform(get("/api/torneos/" + torneo)).andExpect(jsonPath("$.estado").value("PUBLICADO"));
    }

    @Test
    void reabrirUnTorneoEnBorrador_respondeConflict() throws Exception {
        long torneo = torneoChicoProgramado();

        mvc.perform(post("/api/torneos/" + torneo + "/reabrir"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El torneo " + torneo + " no está publicado: no hay nada que reabrir"));
    }

    @Test
    void reabrirSinToken_respondeUnauthorized() throws Exception {
        long torneo = torneoChicoProgramado();
        mvc.perform(post("/api/torneos/" + torneo + "/publicar")).andExpect(status().isOk());

        mvcSinToken.perform(post("/api/torneos/" + torneo + "/reabrir")).andExpect(status().isUnauthorized());
    }

    private String vistaPublica(long torneo, long equipo) throws Exception {
        return mvcSinToken.perform(get("/api/publico/torneos/" + torneo + "/equipos/" + equipo + "/fixture"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }
}

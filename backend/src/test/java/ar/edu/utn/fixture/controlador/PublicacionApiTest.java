package ar.edu.utn.fixture.controlador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

    private String vistaPublica(long torneo, long equipo) throws Exception {
        return mvcSinToken.perform(get("/api/publico/torneos/" + torneo + "/equipos/" + equipo + "/fixture"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }
}

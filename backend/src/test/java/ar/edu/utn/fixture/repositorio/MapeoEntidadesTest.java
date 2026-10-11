package ar.edu.utn.fixture.repositorio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import ar.edu.utn.fixture.entidad.Cancha;
import ar.edu.utn.fixture.entidad.Categoria;
import ar.edu.utn.fixture.entidad.Club;
import ar.edu.utn.fixture.entidad.Equipo;
import ar.edu.utn.fixture.entidad.EstadoTorneo;
import ar.edu.utn.fixture.entidad.FranjaHoraria;
import ar.edu.utn.fixture.entidad.Organizador;
import ar.edu.utn.fixture.entidad.Partido;
import ar.edu.utn.fixture.entidad.Rama;
import ar.edu.utn.fixture.entidad.Sede;
import ar.edu.utn.fixture.entidad.Torneo;
import ar.edu.utn.fixture.entidad.Zona;

/**
 * Guarda una cadena completa de entidades (de organizador a partido) y la vuelve a leer desde la base.
 * Usa la base real de DB_URL; cada test hace rollback al terminar, así que no deja datos.
 * Los valores son DATOS DE PRUEBA, no de un torneo real.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MapeoEntidadesTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PartidoRepositorio partidoRepositorio;

    @Test
    void guardaYLeeUnPartidoProgramadoConTodasSusRelaciones() {
        Organizador organizador = new Organizador();
        organizador.setNombre("Organizador de prueba");
        organizador.setEmail("prueba@ejemplo.test");
        organizador.setPasswordHash("hash-de-prueba");
        em.persist(organizador);

        Torneo torneo = new Torneo();
        torneo.setOrganizador(organizador);
        torneo.setNombre("Torneo de prueba");
        torneo.setTurnosCadaMin(15);
        torneo.setRecambioMin(10);
        torneo.setDescansoMinimoMin(90);
        em.persist(torneo);

        FranjaHoraria franja = new FranjaHoraria();
        franja.setTorneo(torneo);
        franja.setFecha(LocalDate.of(2030, 1, 5));
        franja.setDesde(LocalTime.of(9, 0));
        franja.setHasta(LocalTime.of(20, 0));
        em.persist(franja);

        Sede sede = new Sede();
        sede.setTorneo(torneo);
        sede.setNombre("Sede de prueba");
        em.persist(sede);

        Cancha cancha = new Cancha();
        cancha.setSede(sede);
        cancha.setNombre("Cancha 1");
        em.persist(cancha);

        Categoria categoria = new Categoria();
        categoria.setTorneo(torneo);
        categoria.setNombre("U9");
        categoria.setMinutosNetos(28);
        categoria.setCantidadCuartos(4);
        categoria.setPausasMin(4);
        categoria.setMargenDetencionesMin(10);
        categoria.setMaxPartidosDia(3);
        em.persist(categoria);

        Rama rama = new Rama();
        rama.setTorneo(torneo);
        rama.setNombre("Masculino");
        em.persist(rama);

        Zona zona = new Zona();
        zona.setTorneo(torneo);
        zona.setCategoria(categoria);
        zona.setRama(rama);
        zona.setNombre("Zona A");
        em.persist(zona);

        Club club = new Club();
        club.setTorneo(torneo);
        club.setNombre("Club de prueba");
        em.persist(club);

        Equipo local = nuevoEquipo(torneo, club, categoria, rama, zona, "Equipo 1", 1);
        Equipo visitante = nuevoEquipo(torneo, club, categoria, rama, zona, "Equipo 2", 2);

        Partido partido = new Partido();
        partido.setZona(zona);
        partido.setEquipoLocal(local);
        partido.setEquipoVisitante(visitante);
        partido.setRonda(1);
        partido.setCancha(cancha);
        partido.setInicio(LocalDateTime.of(2030, 1, 5, 9, 0));
        em.persist(partido);

        em.flush();
        em.clear(); // obliga a leer desde la base y no desde la memoria

        Partido leido = partidoRepositorio.findById(partido.getId()).orElseThrow();
        assertThat(leido.getEquipoLocal().getNombre()).isEqualTo("Equipo 1");
        assertThat(leido.getEquipoVisitante().getNombre()).isEqualTo("Equipo 2");
        assertThat(leido.getCancha().getSede().getNombre()).isEqualTo("Sede de prueba");
        assertThat(leido.getZona().getCategoria().getMinutosNetos()).isEqualTo(28);
        assertThat(leido.getZona().getTorneo().getEstado()).isEqualTo(EstadoTorneo.BORRADOR);
        assertThat(leido.getInicio()).isEqualTo(LocalDateTime.of(2030, 1, 5, 9, 0));
    }

    private Equipo nuevoEquipo(Torneo torneo, Club club, Categoria categoria, Rama rama, Zona zona,
            String nombre, int orden) {
        Equipo equipo = new Equipo();
        equipo.setTorneo(torneo);
        equipo.setClub(club);
        equipo.setCategoria(categoria);
        equipo.setRama(rama);
        equipo.setNombre(nombre);
        equipo.setResponsableNombre("Responsable de prueba");
        equipo.setResponsableTelefono("0000-0000");
        equipo.setZona(zona);
        equipo.setOrdenEnZona(orden);
        em.persist(equipo);
        return equipo;
    }
}

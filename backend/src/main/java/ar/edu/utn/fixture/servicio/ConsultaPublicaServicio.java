package ar.edu.utn.fixture.servicio;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.fixture.dominio.ConfiguracionTorneo;
import ar.edu.utn.fixture.dto.Respuestas.EquipoResumen;
import ar.edu.utn.fixture.dto.Respuestas.FixtureDeEquipo;
import ar.edu.utn.fixture.dto.Respuestas.PartidoDeEquipo;
import ar.edu.utn.fixture.entidad.Equipo;
import ar.edu.utn.fixture.entidad.Partido;
import ar.edu.utn.fixture.entidad.Torneo;
import ar.edu.utn.fixture.repositorio.EquipoRepositorio;
import ar.edu.utn.fixture.repositorio.PartidoRepositorio;
import ar.edu.utn.fixture.servicio.AdaptadorDominio.CanchaRef;

/**
 * Consultas de la vista pública, sin login. Solo muestran torneos PUBLICADOS (D11) y nunca datos de contacto (regla 7).
 */
@Service
@Transactional(readOnly = true)
public class ConsultaPublicaServicio {

    private final Torneos torneos;
    private final EquipoRepositorio equipos;
    private final PartidoRepositorio partidos;

    public ConsultaPublicaServicio(Torneos torneos, EquipoRepositorio equipos, PartidoRepositorio partidos) {
        this.torneos = torneos;
        this.equipos = equipos;
        this.partidos = partidos;
    }

    public List<EquipoResumen> equipos(Long torneoId) {
        torneos.publicado(torneoId);
        return equipos.findByTorneoIdOrderByIdAsc(torneoId).stream().map(Etiquetas::resumen).toList();
    }

    /** Partidos de un equipo ordenados por horario, y las rondas en las que tiene fecha libre (se deducen, D18). */
    public FixtureDeEquipo fixtureDeEquipo(Long torneoId, Long equipoId) {
        Torneo torneo = torneos.publicado(torneoId);
        Equipo equipo = equipos.findByIdAndTorneoId(equipoId, torneoId)
                .orElseThrow(() -> new NoEncontradoException("No existe el equipo " + equipoId + " en el torneo"));
        if (equipo.getZona() == null) {
            throw new NoEncontradoException("El equipo " + equipoId + " no tiene partidos en el fixture publicado");
        }
        ConfiguracionTorneo<CanchaRef> tiempos = AdaptadorDominio.parametrosDeTiempo(torneo);

        List<Partido> deLaZona = partidos.buscarFixture(torneoId).stream()
                .filter(p -> p.getZona().getId().equals(equipo.getZona().getId()))
                .toList();
        List<Partido> delEquipo = deLaZona.stream()
                .filter(p -> p.getEquipoLocal().getId().equals(equipoId) || p.getEquipoVisitante().getId().equals(equipoId))
                .sorted(Comparator.comparing(Partido::getInicio))
                .toList();

        List<PartidoDeEquipo> lista = delEquipo.stream().map(p -> {
            boolean local = p.getEquipoLocal().getId().equals(equipoId);
            return new PartidoDeEquipo(p.getRonda(),
                    (local ? p.getEquipoVisitante() : p.getEquipoLocal()).getNombre(),
                    local ? "Local" : "Visitante",
                    p.getCancha().getSede().getNombre(), p.getCancha().getNombre(), p.getInicio(),
                    tiempos.fin(p.getInicio(), AdaptadorDominio.parametros(p.getZona().getCategoria())));
        }).toList();

        int rondas = deLaZona.stream().mapToInt(Partido::getRonda).max().orElse(0);
        Set<Integer> jugadas = delEquipo.stream().map(Partido::getRonda).collect(Collectors.toSet());
        List<Integer> libres = IntStream.rangeClosed(1, rondas).filter(r -> !jugadas.contains(r)).boxed().toList();

        return new FixtureDeEquipo(equipo.getId(), equipo.getNombre(), equipo.getClub().getNombre(),
                Etiquetas.zona(equipo.getZona()), lista, libres);
    }
}

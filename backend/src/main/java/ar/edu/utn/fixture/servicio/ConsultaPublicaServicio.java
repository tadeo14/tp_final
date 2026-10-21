package ar.edu.utn.fixture.servicio;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.fixture.dto.Respuestas.EquipoPublicado;
import ar.edu.utn.fixture.dto.Respuestas.EquiposPublicados;
import ar.edu.utn.fixture.dto.Respuestas.FixtureDeEquipo;
import ar.edu.utn.fixture.dto.Respuestas.PartidoDeEquipo;
import ar.edu.utn.fixture.entidad.PartidoPublicado;
import ar.edu.utn.fixture.entidad.Publicacion;
import ar.edu.utn.fixture.repositorio.PartidoPublicadoRepositorio;

/**
 * Consultas de la vista pública, sin login. Leen SIEMPRE la última versión publicada (la foto), nunca las tablas
 * vivas: lo que ve el público es exactamente lo que se publicó, aunque después se reabra y se corrija el
 * torneo (D46, D47). Nunca muestran datos de contacto (regla 7).
 */
@Service
@Transactional(readOnly = true)
public class ConsultaPublicaServicio {

    private final Publicaciones publicaciones;
    private final PartidoPublicadoRepositorio partidosPublicados;

    public ConsultaPublicaServicio(Publicaciones publicaciones, PartidoPublicadoRepositorio partidosPublicados) {
        this.publicaciones = publicaciones;
        this.partidosPublicados = partidosPublicados;
    }

    /** Equipos que figuran en la última versión publicada, ordenados por id. */
    public EquiposPublicados equipos(Long torneoId) {
        Publicacion vigente = publicaciones.vigente(torneoId);
        Map<Long, EquipoPublicado> equipos = new LinkedHashMap<>();
        for (PartidoPublicado p : partidosPublicados.findByPublicacionIdOrderByInicioAscIdAsc(vigente.getId())) {
            equipos.putIfAbsent(p.getEquipoLocalId(), new EquipoPublicado(p.getEquipoLocalId(), p.getEquipoLocal(),
                    p.getCategoria(), p.getRama(), p.etiquetaZona()));
            equipos.putIfAbsent(p.getEquipoVisitanteId(), new EquipoPublicado(p.getEquipoVisitanteId(), p.getEquipoVisitante(),
                    p.getCategoria(), p.getRama(), p.etiquetaZona()));
        }
        List<EquipoPublicado> ordenados = equipos.values().stream().sorted(Comparator.comparing(EquipoPublicado::id)).toList();
        return new EquiposPublicados(Publicaciones.version(vigente), ordenados);
    }

    /** Partidos de un equipo ordenados por horario, y las rondas en las que tiene fecha libre (se deducen, D18). */
    public FixtureDeEquipo fixtureDeEquipo(Long torneoId, Long equipoId) {
        Publicacion vigente = publicaciones.vigente(torneoId);
        List<PartidoPublicado> foto = partidosPublicados.findByPublicacionIdOrderByInicioAscIdAsc(vigente.getId());

        List<PartidoPublicado> delEquipo = foto.stream().filter(p -> p.juega(equipoId)).toList();
        if (delEquipo.isEmpty()) {
            throw new NoEncontradoException("No existe el equipo " + equipoId + " en el fixture publicado");
        }
        PartidoPublicado alguno = delEquipo.get(0);
        String zona = alguno.etiquetaZona();
        String nombre = alguno.getEquipoLocalId().equals(equipoId) ? alguno.getEquipoLocal() : alguno.getEquipoVisitante();

        List<PartidoDeEquipo> lista = delEquipo.stream().map(p -> {
            boolean local = p.getEquipoLocalId().equals(equipoId);
            return new PartidoDeEquipo(p.getRonda(), local ? p.getEquipoVisitante() : p.getEquipoLocal(),
                    local ? "Local" : "Visitante", p.getSede(), p.getCancha(), p.getInicio(), p.getFin());
        }).toList();

        int rondas = foto.stream().filter(p -> p.etiquetaZona().equals(zona)).mapToInt(PartidoPublicado::getRonda).max().orElse(0);
        Set<Integer> jugadas = delEquipo.stream().map(PartidoPublicado::getRonda).collect(Collectors.toSet());
        List<Integer> libres = IntStream.rangeClosed(1, rondas).filter(r -> !jugadas.contains(r)).boxed().toList();

        return new FixtureDeEquipo(Publicaciones.version(vigente), equipoId, nombre, zona, lista, libres);
    }
}

package ar.edu.utn.fixture.servicio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.fixture.dominio.PropuestaZonas;
import ar.edu.utn.fixture.dominio.PropuestaZonas.Propuesta;
import ar.edu.utn.fixture.dto.Respuestas.GrupoPropuesto;
import ar.edu.utn.fixture.dto.Respuestas.PropuestaDeZonas;
import ar.edu.utn.fixture.dto.Respuestas.ZonaDetalle;
import ar.edu.utn.fixture.dto.Respuestas.ZonaPropuesta;
import ar.edu.utn.fixture.entidad.Equipo;
import ar.edu.utn.fixture.entidad.Torneo;
import ar.edu.utn.fixture.entidad.Zona;
import ar.edu.utn.fixture.repositorio.CategoriaRepositorio;
import ar.edu.utn.fixture.repositorio.EquipoRepositorio;
import ar.edu.utn.fixture.repositorio.PartidoRepositorio;
import ar.edu.utn.fixture.repositorio.RamaRepositorio;
import ar.edu.utn.fixture.repositorio.ZonaRepositorio;

/**
 * "Proponer zonas" (D69): calcula con {@link PropuestaZonas} cuántas zonas armar en cada categoría y rama según
 * la cantidad de equipos inscriptos. Primero una vista previa, que no guarda nada; después crear, que las guarda.
 * Solo en BORRADOR y solo el dueño (D45, D47). No mueve equipos: el reparto lo sigue haciendo el sorteo.
 */
@Service
@Transactional
public class PropuestaZonasServicio {

    private final Torneos torneos;
    private final EquipoRepositorio equipos;
    private final ZonaRepositorio zonas;
    private final PartidoRepositorio partidos;
    private final CategoriaRepositorio categorias;
    private final RamaRepositorio ramas;

    public PropuestaZonasServicio(Torneos torneos, EquipoRepositorio equipos, ZonaRepositorio zonas,
            PartidoRepositorio partidos, CategoriaRepositorio categorias, RamaRepositorio ramas) {
        this.torneos = torneos;
        this.equipos = equipos;
        this.zonas = zonas;
        this.partidos = partidos;
        this.categorias = categorias;
        this.ramas = ramas;
    }

    /** Vista previa: qué zonas se crearían y cuántos equipos quedarían en cada una. No guarda nada. */
    @Transactional(readOnly = true)
    public PropuestaDeZonas vistaPrevia(Long organizadorId, Long torneoId, int maximoEquiposPorZona) {
        torneos.editable(torneoId, organizadorId);
        return calcular(torneoId, maximoEquiposPorZona);
    }

    /**
     * Crea las zonas propuestas. Si alguna categoría y rama ya tiene zonas, no se pisan en silencio: sin
     * {@code confirmar} responde 409 con requiereConfirmacion; al confirmar se reemplazan y, como el sorteo ya no
     * corresponde, se deshace en todo el torneo y se borran los partidos (D66).
     */
    public List<ZonaDetalle> crear(Long organizadorId, Long torneoId, int maximoEquiposPorZona, boolean confirmar) {
        torneos.editable(torneoId, organizadorId);
        PropuestaDeZonas propuesta = calcular(torneoId, maximoEquiposPorZona);
        List<GrupoPropuesto> aReemplazar = propuesta.grupos().stream()
                .filter(g -> !g.zonas().isEmpty() && !g.zonasActuales().isEmpty()).toList();

        if (!aReemplazar.isEmpty()) {
            if (!confirmar) {
                throw new ConfirmacionRequeridaException(mensajeReemplazo(torneoId, aReemplazar));
            }
            partidos.borrarDelTorneo(torneoId);
            equipos.deshacerSorteo(torneoId);
            for (GrupoPropuesto g : aReemplazar) {
                zonas.deleteAll(zonas.findByTorneoIdOrderByIdAsc(torneoId).stream()
                        .filter(z -> z.getCategoria().getId().equals(g.categoriaId()) && z.getRama().getId().equals(g.ramaId()))
                        .toList());
            }
            zonas.flush(); // antes de crear "Zona A" otra vez (nombre único por categoría y rama)
        }

        Torneo torneo = torneos.editable(torneoId, organizadorId); // se vuelve a leer: la base pudo cambiar arriba
        List<ZonaDetalle> creadas = new ArrayList<>();
        for (GrupoPropuesto g : propuesta.grupos()) {
            for (ZonaPropuesta zp : g.zonas()) {
                Zona z = new Zona();
                z.setTorneo(torneo);
                z.setCategoria(categorias.getReferenceById(g.categoriaId()));
                z.setRama(ramas.getReferenceById(g.ramaId()));
                z.setNombre(zp.nombre());
                zonas.save(z);
                creadas.add(new ZonaDetalle(z.getId(), z.getNombre(), g.categoriaId(), g.ramaId(),
                        g.categoria() + " " + g.rama() + " - " + z.getNombre()));
            }
        }
        return creadas;
    }

    private PropuestaDeZonas calcular(Long torneoId, int maximoEquiposPorZona) {
        PropuestaZonas regla;
        try {
            regla = new PropuestaZonas(maximoEquiposPorZona);
        } catch (IllegalArgumentException e) {
            throw new DatosInvalidosException(e.getMessage());
        }

        // Equipos por categoría y rama, en el orden de carga de las categorías y ramas.
        Map<String, List<Equipo>> porGrupo = new LinkedHashMap<>();
        equipos.findByTorneoIdOrderByIdAsc(torneoId).stream()
                .sorted((a, b) -> a.getCategoria().getId().equals(b.getCategoria().getId())
                        ? a.getRama().getId().compareTo(b.getRama().getId())
                        : a.getCategoria().getId().compareTo(b.getCategoria().getId()))
                .forEach(e -> porGrupo.computeIfAbsent(clave(e.getCategoria().getId(), e.getRama().getId()), k -> new ArrayList<>()).add(e));
        Map<String, List<String>> zonasActuales = new LinkedHashMap<>();
        for (Zona z : zonas.findByTorneoIdOrderByIdAsc(torneoId)) {
            zonasActuales.computeIfAbsent(clave(z.getCategoria().getId(), z.getRama().getId()), k -> new ArrayList<>()).add(z.getNombre());
        }

        List<GrupoPropuesto> grupos = new ArrayList<>();
        for (Map.Entry<String, List<Equipo>> entrada : porGrupo.entrySet()) { // un grupo sin equipos no aparece
            Equipo alguno = entrada.getValue().get(0);
            Propuesta p = regla.proponer(entrada.getValue().size());
            grupos.add(new GrupoPropuesto(alguno.getCategoria().getId(), alguno.getCategoria().getNombre(),
                    alguno.getRama().getId(), alguno.getRama().getNombre(), entrada.getValue().size(),
                    p.zonas().stream().map(z -> new ZonaPropuesta(z.nombre(), z.equipos())).toList(),
                    zonasActuales.getOrDefault(entrada.getKey(), List.of()), p.aviso()));
        }
        int total = grupos.stream().mapToInt(g -> g.zonas().size()).sum();
        boolean reemplaza = grupos.stream().anyMatch(g -> !g.zonas().isEmpty() && !g.zonasActuales().isEmpty());
        return new PropuestaDeZonas(maximoEquiposPorZona, grupos, total, reemplaza);
    }

    /** "Ya hay zonas en U9 Masculino (Zona A, Zona B). Si confirmás, se reemplazan…" con lo que se pierde. */
    private String mensajeReemplazo(Long torneoId, List<GrupoPropuesto> aReemplazar) {
        List<String> grupos = aReemplazar.stream()
                .map(g -> g.categoria() + " " + g.rama() + " (" + String.join(", ", g.zonasActuales()) + ")").toList();
        long sorteados = equipos.countByTorneoIdAndZonaIsNotNull(torneoId);
        long generados = partidos.contarDelTorneo(torneoId);
        StringBuilder m = new StringBuilder("Ya hay zonas en " + String.join(", ", grupos)
                + ". Si confirmás, se reemplazan por las propuestas");
        if (sorteados > 0) {
            m.append(", se deshace el sorteo de todo el torneo (").append(sorteados).append(" equipos sorteados)");
        }
        if (generados > 0) {
            m.append(" y se borran los ").append(generados).append(" partidos generados");
        }
        return m.append('.').toString();
    }

    private static String clave(Long categoriaId, Long ramaId) {
        return categoriaId + "/" + ramaId;
    }
}

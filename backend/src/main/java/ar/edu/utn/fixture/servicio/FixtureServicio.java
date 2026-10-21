package ar.edu.utn.fixture.servicio;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.fixture.dominio.ConfiguracionTorneo;
import ar.edu.utn.fixture.dominio.Enfrentamiento;
import ar.edu.utn.fixture.dominio.EstadoProgramacion;
import ar.edu.utn.fixture.dominio.GeneradorEnfrentamientos;
import ar.edu.utn.fixture.dominio.PartidoAProgramar;
import ar.edu.utn.fixture.dominio.PartidoProgramado;
import ar.edu.utn.fixture.dominio.Programador;
import ar.edu.utn.fixture.dominio.ResultadoProgramacion;
import ar.edu.utn.fixture.dominio.ResultadoValidacion;
import ar.edu.utn.fixture.dominio.SorteoZonas;
import ar.edu.utn.fixture.dominio.ValidadorRestricciones;
import ar.edu.utn.fixture.dto.Respuestas.Generacion;
import ar.edu.utn.fixture.dto.Respuestas.PartidoDetalle;
import ar.edu.utn.fixture.dto.Respuestas.Publicado;
import ar.edu.utn.fixture.dto.Respuestas.TorneoDetalle;
import ar.edu.utn.fixture.dto.Respuestas.Validacion;
import ar.edu.utn.fixture.dto.Respuestas.VersionPublicada;
import ar.edu.utn.fixture.dto.Respuestas.ViolacionDetalle;
import ar.edu.utn.fixture.dto.Respuestas.ZonaSorteada;
import ar.edu.utn.fixture.entidad.Cancha;
import ar.edu.utn.fixture.entidad.Equipo;
import ar.edu.utn.fixture.entidad.EstadoTorneo;
import ar.edu.utn.fixture.entidad.Partido;
import ar.edu.utn.fixture.entidad.PartidoPublicado;
import ar.edu.utn.fixture.entidad.Publicacion;
import ar.edu.utn.fixture.entidad.Torneo;
import ar.edu.utn.fixture.entidad.Zona;
import ar.edu.utn.fixture.repositorio.CanchaRepositorio;
import ar.edu.utn.fixture.repositorio.EquipoRepositorio;
import ar.edu.utn.fixture.repositorio.FranjaHorariaRepositorio;
import ar.edu.utn.fixture.repositorio.PartidoPublicadoRepositorio;
import ar.edu.utn.fixture.repositorio.PartidoRepositorio;
import ar.edu.utn.fixture.repositorio.PublicacionRepositorio;
import ar.edu.utn.fixture.repositorio.ZonaRepositorio;
import ar.edu.utn.fixture.servicio.AdaptadorDominio.CanchaRef;
import ar.edu.utn.fixture.servicio.AdaptadorDominio.EquipoRef;

/**
 * Recorrido del fixture: sortear zonas → generar enfrentamientos → programar → validar → publicar.
 * Carga los datos, llama a las clases del dominio y guarda el resultado. Todo se hace con el torneo en
 * BORRADOR; con el torneo PUBLICADO se rechaza (D20).
 */
@Service
@Transactional
public class FixtureServicio {

    private final Torneos torneos;
    private final ZonaRepositorio zonas;
    private final EquipoRepositorio equipos;
    private final PartidoRepositorio partidos;
    private final FranjaHorariaRepositorio franjas;
    private final CanchaRepositorio canchas;
    private final PublicacionRepositorio publicaciones;
    private final PartidoPublicadoRepositorio partidosPublicados;
    private final ValidadorRestricciones validador = new ValidadorRestricciones();
    private final GeneradorEnfrentamientos generador = new GeneradorEnfrentamientos();
    private final Programador programador = new Programador(validador);

    public FixtureServicio(Torneos torneos, ZonaRepositorio zonas, EquipoRepositorio equipos, PartidoRepositorio partidos,
            FranjaHorariaRepositorio franjas, CanchaRepositorio canchas, PublicacionRepositorio publicaciones,
            PartidoPublicadoRepositorio partidosPublicados) {
        this.torneos = torneos;
        this.zonas = zonas;
        this.equipos = equipos;
        this.partidos = partidos;
        this.franjas = franjas;
        this.canchas = canchas;
        this.publicaciones = publicaciones;
        this.partidosPublicados = partidosPublicados;
    }

    // ------------------------------------------------------------------ sortear zonas

    /**
     * Sortea los equipos de cada categoría y rama entre sus zonas y guarda el resultado (D5, D16).
     * Si ya había un sorteo, lo rehace a pedido del organizador, y borra los partidos generados con el anterior.
     */
    public List<ZonaSorteada> sortear(Long organizadorId, Long torneoId) {
        torneos.editable(torneoId, organizadorId);
        partidos.borrarDelTorneo(torneoId);

        List<Equipo> todos = equipos.findByTorneoIdOrderByIdAsc(torneoId);
        for (Equipo e : todos) { // primero se vacía el sorteo anterior, para no chocar con orden_en_zona único
            e.setZona(null);
            e.setOrdenEnZona(null);
        }
        equipos.flush();

        Map<String, List<Equipo>> equiposPorGrupo = new LinkedHashMap<>();
        for (Equipo e : todos) {
            equiposPorGrupo.computeIfAbsent(grupo(e.getCategoria().getId(), e.getRama().getId()), g -> new ArrayList<>()).add(e);
        }
        Map<String, List<Zona>> zonasPorGrupo = new LinkedHashMap<>();
        for (Zona z : zonas.findByTorneoIdOrderByIdAsc(torneoId)) {
            zonasPorGrupo.computeIfAbsent(grupo(z.getCategoria().getId(), z.getRama().getId()), g -> new ArrayList<>()).add(z);
        }

        SorteoZonas sorteo = new SorteoZonas(new Random());
        for (List<Equipo> delGrupo : equiposPorGrupo.values()) {
            Equipo alguno = delGrupo.get(0);
            String nombreGrupo = alguno.getCategoria().getNombre() + " " + alguno.getRama().getNombre();
            List<Zona> zonasDelGrupo = zonasPorGrupo.get(grupo(alguno.getCategoria().getId(), alguno.getRama().getId()));
            if (zonasDelGrupo == null) {
                throw new DatosInvalidosException("Falta definir las zonas de " + nombreGrupo);
            }
            List<List<Equipo>> resultado;
            try {
                resultado = sorteo.sortear(delGrupo, zonasDelGrupo.size());
            } catch (IllegalArgumentException e) {
                throw new DatosInvalidosException(nombreGrupo + ": " + e.getMessage());
            }
            for (int z = 0; z < resultado.size(); z++) {
                List<Equipo> enLaZona = resultado.get(z);
                for (int orden = 0; orden < enLaZona.size(); orden++) {
                    enLaZona.get(orden).setZona(zonasDelGrupo.get(z));
                    enLaZona.get(orden).setOrdenEnZona(orden + 1);
                }
            }
        }
        equipos.flush();

        List<ZonaSorteada> respuesta = new ArrayList<>();
        for (Zona z : zonas.findByTorneoIdOrderByIdAsc(torneoId)) {
            List<String> nombres = equipos.findByZonaIdOrderByOrdenEnZonaAsc(z.getId()).stream().map(Equipo::getNombre).toList();
            respuesta.add(new ZonaSorteada(Etiquetas.zona(z), nombres));
        }
        return respuesta;
    }

    private static String grupo(Long categoriaId, Long ramaId) {
        return categoriaId + "/" + ramaId;
    }

    // ------------------------------------------------------------------ generar enfrentamientos

    /** Todos contra todos dentro de cada zona, en el orden del sorteo. Reemplaza los partidos anteriores. */
    public Generacion generar(Long organizadorId, Long torneoId) {
        torneos.editable(torneoId, organizadorId);
        long sinZona = equipos.findByTorneoIdOrderByIdAsc(torneoId).stream().filter(e -> e.getZona() == null).count();
        if (sinZona > 0) {
            throw new DatosInvalidosException("Primero hay que sortear las zonas: hay " + sinZona + " equipos sin zona");
        }
        partidos.borrarDelTorneo(torneoId);

        int cantidad = 0;
        for (Zona zona : zonas.findByTorneoIdOrderByIdAsc(torneoId)) {
            List<Equipo> delaZona = equipos.findByZonaIdOrderByOrdenEnZonaAsc(zona.getId());
            if (delaZona.isEmpty()) {
                continue; // zona definida pero sin equipos: no juega
            }
            List<Enfrentamiento<Equipo>> enfrentamientos;
            try {
                enfrentamientos = generador.todosContraTodos(delaZona);
            } catch (IllegalArgumentException e) {
                throw new DatosInvalidosException("Zona " + Etiquetas.zona(zona) + ": " + e.getMessage());
            }
            for (Enfrentamiento<Equipo> enf : enfrentamientos) {
                Partido p = new Partido();
                p.setZona(zona);
                p.setEquipoLocal(enf.local());
                p.setEquipoVisitante(enf.visitante());
                p.setRonda(enf.ronda());
                partidos.save(p);
                cantidad++;
            }
        }
        return new Generacion(cantidad);
    }

    // ------------------------------------------------------------------ programar

    /**
     * Asigna cancha y horario con el Programador. Si no hay solución responde 422 con el mensaje y el detalle,
     * y no guarda nada (la transacción se deshace).
     */
    public List<PartidoDetalle> programar(Long organizadorId, Long torneoId) {
        Torneo torneo = torneos.editable(torneoId, organizadorId);
        List<Partido> fixture = partidosGenerados(torneoId);
        List<Cancha> habilitadas = canchas.findBySedeTorneoIdAndHabilitadaTrueOrderByIdAsc(torneoId);
        ConfiguracionTorneo<CanchaRef> config = AdaptadorDominio.configuracion(
                torneo, franjas.findByTorneoIdOrderByFechaAscDesdeAsc(torneoId), habilitadas);

        List<PartidoAProgramar<EquipoRef>> pedido = fixture.stream()
                .map(p -> new PartidoAProgramar<>(AdaptadorDominio.equipo(p.getEquipoLocal()),
                        AdaptadorDominio.equipo(p.getEquipoVisitante()), p.getRonda(), Etiquetas.zona(p.getZona()),
                        AdaptadorDominio.parametros(p.getZona().getCategoria())))
                .toList();
        ResultadoProgramacion<EquipoRef, CanchaRef> resultado = programador.programar(config, pedido);
        if (resultado.estado() != EstadoProgramacion.PROGRAMADO) {
            throw new FixtureInvalidoException(resultado.mensaje(), resultado.detalles());
        }

        Map<Long, Cancha> canchaPorId = new LinkedHashMap<>();
        habilitadas.forEach(c -> canchaPorId.put(c.getId(), c));
        for (int i = 0; i < fixture.size(); i++) { // el resultado viene en el mismo orden que el pedido
            PartidoProgramado<EquipoRef, CanchaRef> programado = resultado.partidos().get(i);
            fixture.get(i).setCancha(canchaPorId.get(programado.cancha().id()));
            fixture.get(i).setInicio(programado.inicio());
        }
        return detalles(torneo, fixture);
    }

    // ------------------------------------------------------------------ validar y publicar

    /** Verifica R1–R8 con el ValidadorRestricciones, el mismo que usa el programador (regla 3). */
    @Transactional(readOnly = true)
    public Validacion validar(Long organizadorId, Long torneoId) {
        Torneo torneo = torneos.delOrganizador(torneoId, organizadorId);
        ResultadoValidacion<EquipoRef, CanchaRef> resultado = validarFixture(torneo, partidosGenerados(torneoId));
        List<ViolacionDetalle> violaciones = resultado.violaciones().stream()
                .map(v -> new ViolacionDetalle(v.restriccion().name(), v.restriccion().descripcion(), v.detalle()))
                .toList();
        return new Validacion(resultado.esValido(), violaciones);
    }

    /**
     * Publica solo si el fixture cumple R1–R8. En la misma transacción crea la versión siguiente (1, 2, 3…)
     * con la foto de todos los partidos: es lo que ve el público desde ahora (D46).
     * Con el torneo publicado no se puede modificar nada hasta reabrirlo (D20, D47).
     */
    public Publicado publicar(Long organizadorId, Long torneoId, String motivo) {
        Torneo torneo = torneos.editable(torneoId, organizadorId);
        int numero = publicaciones.findFirstByTorneoIdOrderByNumeroDesc(torneoId).map(p -> p.getNumero() + 1).orElse(1);
        String motivoLimpio = motivo == null || motivo.isBlank() ? null : motivo.trim();
        if (numero > 1 && motivoLimpio == null) {
            throw new DatosInvalidosException("La versión " + numero + " corrige un fixture ya publicado: hay que indicar el motivo");
        }
        List<Partido> fixture = partidosGenerados(torneoId);
        ResultadoValidacion<EquipoRef, CanchaRef> resultado = validarFixture(torneo, fixture);
        if (!resultado.esValido()) {
            throw new FixtureInvalidoException("El fixture no cumple las restricciones obligatorias y no se puede publicar",
                    resultado.violaciones().stream().map(v -> v.restriccion().name() + ": " + v.detalle()).toList());
        }

        Publicacion publicacion = publicaciones.save(new Publicacion(torneo, numero, Instant.now(), motivoLimpio));
        ConfiguracionTorneo<CanchaRef> tiempos = AdaptadorDominio.parametrosDeTiempo(torneo);
        partidosPublicados.saveAll(fixture.stream()
                .map(p -> new PartidoPublicado(publicacion, p,
                        tiempos.fin(p.getInicio(), AdaptadorDominio.parametros(p.getZona().getCategoria()))))
                .toList());

        torneo.setEstado(EstadoTorneo.PUBLICADO);
        return new Publicado(torneo.getId(), torneo.getEstado(), Publicaciones.version(publicacion));
    }

    /**
     * Vuelve a BORRADOR un torneo publicado para corregirlo (D47). Lo publicado no se pierde: la vista pública
     * sigue mostrando la última versión hasta que se publique la siguiente, que exige motivo.
     */
    public TorneoDetalle reabrir(Long organizadorId, Long torneoId) {
        Torneo torneo = torneos.delOrganizador(torneoId, organizadorId);
        if (torneo.getEstado() != EstadoTorneo.PUBLICADO) {
            throw new ConflictoException("El torneo " + torneoId + " no está publicado: no hay nada que reabrir");
        }
        torneo.setEstado(EstadoTorneo.BORRADOR);
        return Torneos.detalle(torneo);
    }

    /** Historial de versiones publicadas, de la más nueva a la más vieja. Vacío si nunca se publicó. */
    @Transactional(readOnly = true)
    public List<VersionPublicada> publicaciones(Long organizadorId, Long torneoId) {
        torneos.delOrganizador(torneoId, organizadorId);
        return publicaciones.findByTorneoIdOrderByNumeroDesc(torneoId).stream().map(Publicaciones::version).toList();
    }

    private ResultadoValidacion<EquipoRef, CanchaRef> validarFixture(Torneo torneo, List<Partido> fixture) {
        Long torneoId = torneo.getId();
        ConfiguracionTorneo<CanchaRef> config = AdaptadorDominio.configuracion(torneo,
                franjas.findByTorneoIdOrderByFechaAscDesdeAsc(torneoId),
                canchas.findBySedeTorneoIdAndHabilitadaTrueOrderByIdAsc(torneoId));
        return validador.validar(config, fixture.stream().map(AdaptadorDominio::programado).toList());
    }

    // ------------------------------------------------------------------ consulta del organizador

    /** Fixture completo, en BORRADOR o PUBLICADO. */
    @Transactional(readOnly = true)
    public List<PartidoDetalle> fixture(Long organizadorId, Long torneoId) {
        Torneo torneo = torneos.delOrganizador(torneoId, organizadorId);
        return detalles(torneo, partidos.buscarFixture(torneoId));
    }

    private List<Partido> partidosGenerados(Long torneoId) {
        List<Partido> fixture = partidos.buscarFixture(torneoId);
        if (fixture.isEmpty()) {
            throw new DatosInvalidosException("Primero hay que generar los enfrentamientos");
        }
        return fixture;
    }

    private static List<PartidoDetalle> detalles(Torneo torneo, List<Partido> fixture) {
        ConfiguracionTorneo<CanchaRef> tiempos = AdaptadorDominio.parametrosDeTiempo(torneo);
        return fixture.stream().map(p -> new PartidoDetalle(p.getId(), Etiquetas.zona(p.getZona()), p.getRonda(),
                p.getEquipoLocal().getNombre(), p.getEquipoVisitante().getNombre(),
                p.getCancha() == null ? null : p.getCancha().getSede().getNombre(),
                p.getCancha() == null ? null : p.getCancha().getNombre(),
                p.getInicio(),
                p.getInicio() == null ? null : tiempos.fin(p.getInicio(), AdaptadorDominio.parametros(p.getZona().getCategoria()))))
                .toList();
    }
}

package ar.edu.utn.fixture.servicio;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.fixture.dto.Pedidos.NuevaCancha;
import ar.edu.utn.fixture.dto.Pedidos.NuevaCategoria;
import ar.edu.utn.fixture.dto.Pedidos.NuevaFranja;
import ar.edu.utn.fixture.dto.Pedidos.NuevaRama;
import ar.edu.utn.fixture.dto.Pedidos.NuevaSede;
import ar.edu.utn.fixture.dto.Pedidos.NuevaZona;
import ar.edu.utn.fixture.dto.Pedidos.NuevoClub;
import ar.edu.utn.fixture.dto.Pedidos.NuevoEquipo;
import ar.edu.utn.fixture.dto.Pedidos.NuevoTorneo;
import ar.edu.utn.fixture.dto.Respuestas.CanchaDetalle;
import ar.edu.utn.fixture.dto.Respuestas.CategoriaDetalle;
import ar.edu.utn.fixture.dto.Respuestas.ConfiguracionDetalle;
import ar.edu.utn.fixture.dto.Respuestas.Creado;
import ar.edu.utn.fixture.dto.Respuestas.FranjaDetalle;
import ar.edu.utn.fixture.dto.Respuestas.SedeDetalle;
import ar.edu.utn.fixture.dto.Respuestas.ZonaDetalle;
import ar.edu.utn.fixture.dto.Respuestas.EquipoDetalle;
import ar.edu.utn.fixture.dto.Respuestas.EquipoResumen;
import ar.edu.utn.fixture.dto.Respuestas.TorneoDetalle;
import ar.edu.utn.fixture.entidad.Cancha;
import ar.edu.utn.fixture.entidad.Categoria;
import ar.edu.utn.fixture.entidad.Club;
import ar.edu.utn.fixture.entidad.Equipo;
import ar.edu.utn.fixture.entidad.FranjaHoraria;
import ar.edu.utn.fixture.entidad.Organizador;
import ar.edu.utn.fixture.entidad.Rama;
import ar.edu.utn.fixture.entidad.Sede;
import ar.edu.utn.fixture.entidad.Torneo;
import ar.edu.utn.fixture.entidad.Zona;
import ar.edu.utn.fixture.repositorio.CanchaRepositorio;
import ar.edu.utn.fixture.repositorio.CategoriaRepositorio;
import ar.edu.utn.fixture.repositorio.ClubRepositorio;
import ar.edu.utn.fixture.repositorio.EquipoRepositorio;
import ar.edu.utn.fixture.repositorio.FranjaHorariaRepositorio;
import ar.edu.utn.fixture.repositorio.OrganizadorRepositorio;
import ar.edu.utn.fixture.repositorio.RamaRepositorio;
import ar.edu.utn.fixture.repositorio.SedeRepositorio;
import ar.edu.utn.fixture.repositorio.ZonaRepositorio;

/**
 * Configuración del torneo: crear el torneo y cargar franjas, sedes, canchas, categorías, ramas, clubes,
 * zonas y equipos. Con el torneo PUBLICADO no se puede cambiar nada (D20, D39).
 */
@Service
@Transactional
public class ConfiguracionServicio {

    private final Torneos torneos;
    private final OrganizadorRepositorio organizadores;
    private final FranjaHorariaRepositorio franjas;
    private final SedeRepositorio sedes;
    private final CanchaRepositorio canchas;
    private final CategoriaRepositorio categorias;
    private final RamaRepositorio ramas;
    private final ClubRepositorio clubes;
    private final ZonaRepositorio zonas;
    private final EquipoRepositorio equipos;

    public ConfiguracionServicio(Torneos torneos, OrganizadorRepositorio organizadores, FranjaHorariaRepositorio franjas,
            SedeRepositorio sedes, CanchaRepositorio canchas, CategoriaRepositorio categorias, RamaRepositorio ramas,
            ClubRepositorio clubes, ZonaRepositorio zonas, EquipoRepositorio equipos) {
        this.torneos = torneos;
        this.organizadores = organizadores;
        this.franjas = franjas;
        this.sedes = sedes;
        this.canchas = canchas;
        this.categorias = categorias;
        this.ramas = ramas;
        this.clubes = clubes;
        this.zonas = zonas;
        this.equipos = equipos;
    }

    /** El torneo queda a nombre del organizador autenticado (D45). */
    public TorneoDetalle crearTorneo(Long organizadorId, NuevoTorneo pedido) {
        Organizador organizador = organizadores.findById(organizadorId)
                .orElseThrow(() -> new NoEncontradoException("No existe el organizador " + organizadorId));
        Torneo t = new Torneo();
        t.setOrganizador(organizador);
        t.setNombre(pedido.nombre());
        t.setTurnosCadaMin(pedido.turnosCadaMin());
        t.setRecambioMin(pedido.recambioMin());
        t.setDescansoMinimoMin(pedido.descansoMinimoMin());
        torneos.guardar(t);
        return Torneos.detalle(t);
    }

    /** Solo los torneos del organizador autenticado. */
    @Transactional(readOnly = true)
    public List<TorneoDetalle> listarTorneos(Long organizadorId) {
        return torneos.delOrganizador(organizadorId).stream().map(Torneos::detalle).toList();
    }

    @Transactional(readOnly = true)
    public TorneoDetalle obtenerTorneo(Long organizadorId, Long torneoId) {
        return Torneos.detalle(torneos.delOrganizador(torneoId, organizadorId));
    }

    public Creado agregarFranja(Long organizadorId, Long torneoId, NuevaFranja pedido) {
        Torneo t = torneos.editable(torneoId, organizadorId);
        if (!pedido.hasta().isAfter(pedido.desde())) {
            throw new DatosInvalidosException("La franja tiene que terminar después de empezar");
        }
        FranjaHoraria f = new FranjaHoraria();
        f.setTorneo(t);
        f.setFecha(pedido.fecha());
        f.setDesde(pedido.desde());
        f.setHasta(pedido.hasta());
        franjas.save(f);
        return new Creado(f.getId(), f.getFecha() + " " + f.getDesde() + "–" + f.getHasta());
    }

    public Creado agregarSede(Long organizadorId, Long torneoId, NuevaSede pedido) {
        Sede s = new Sede();
        s.setTorneo(torneos.editable(torneoId, organizadorId));
        s.setNombre(pedido.nombre());
        s.setDireccion(pedido.direccion());
        sedes.save(s);
        return new Creado(s.getId(), s.getNombre());
    }

    public Creado agregarCancha(Long organizadorId, Long sedeId, NuevaCancha pedido) {
        // Una sede de un torneo ajeno se trata igual que una que no existe (D45).
        Sede sede = sedes.findById(sedeId)
                .filter(s -> s.getTorneo().getOrganizador().getId().equals(organizadorId))
                .orElseThrow(() -> new NoEncontradoException("No existe la sede " + sedeId));
        torneos.editable(sede.getTorneo().getId(), organizadorId);
        Cancha c = new Cancha();
        c.setSede(sede);
        c.setNombre(pedido.nombre());
        c.setHabilitada(pedido.habilitada() == null || pedido.habilitada());
        canchas.save(c);
        return new Creado(c.getId(), sede.getNombre() + " - " + c.getNombre());
    }

    public Creado agregarCategoria(Long organizadorId, Long torneoId, NuevaCategoria pedido) {
        Categoria c = new Categoria();
        c.setTorneo(torneos.editable(torneoId, organizadorId));
        c.setNombre(pedido.nombre());
        c.setMinutosNetos(pedido.minutosNetos());
        c.setCantidadCuartos(pedido.cantidadCuartos());
        c.setPausasMin(pedido.pausasMin());
        c.setMargenDetencionesMin(pedido.margenDetencionesMin());
        c.setMaxPartidosDia(pedido.maxPartidosDia());
        categorias.save(c);
        return new Creado(c.getId(), c.getNombre());
    }

    public Creado agregarRama(Long organizadorId, Long torneoId, NuevaRama pedido) {
        Rama r = new Rama();
        r.setTorneo(torneos.editable(torneoId, organizadorId));
        r.setNombre(pedido.nombre());
        ramas.save(r);
        return new Creado(r.getId(), r.getNombre());
    }

    public Creado agregarClub(Long organizadorId, Long torneoId, NuevoClub pedido) {
        Club c = new Club();
        c.setTorneo(torneos.editable(torneoId, organizadorId));
        c.setNombre(pedido.nombre());
        clubes.save(c);
        return new Creado(c.getId(), c.getNombre());
    }

    /** Las zonas se definen antes del sorteo: la cantidad de zonas de una categoría y rama es la cantidad de filas (D18). */
    public Creado agregarZona(Long organizadorId, Long torneoId, NuevaZona pedido) {
        Torneo t = torneos.editable(torneoId, organizadorId);
        Zona z = new Zona();
        z.setTorneo(t);
        z.setCategoria(categoriaDelTorneo(pedido.categoriaId(), torneoId));
        z.setRama(ramaDelTorneo(pedido.ramaId(), torneoId));
        z.setNombre(pedido.nombre());
        zonas.save(z);
        return new Creado(z.getId(), Etiquetas.zona(z));
    }

    public EquipoResumen inscribirEquipo(Long organizadorId, Long torneoId, NuevoEquipo pedido) {
        Torneo t = torneos.editable(torneoId, organizadorId);
        Equipo e = new Equipo();
        e.setTorneo(t);
        e.setClub(clubes.findByIdAndTorneoId(pedido.clubId(), torneoId)
                .orElseThrow(() -> new NoEncontradoException("No existe el club " + pedido.clubId() + " en el torneo")));
        e.setCategoria(categoriaDelTorneo(pedido.categoriaId(), torneoId));
        e.setRama(ramaDelTorneo(pedido.ramaId(), torneoId));
        e.setNombre(pedido.nombre());
        e.setResponsableNombre(pedido.responsableNombre());
        e.setResponsableTelefono(pedido.responsableTelefono());
        equipos.save(e);
        return Etiquetas.resumen(e);
    }

    /** Toda la configuración del torneo para mostrarla en el frontend (D56). Solo lectura; 404 si el torneo es ajeno. */
    @Transactional(readOnly = true)
    public ConfiguracionDetalle configuracion(Long organizadorId, Long torneoId) {
        torneos.delOrganizador(torneoId, organizadorId);
        List<Cancha> todasLasCanchas = canchas.findBySedeTorneoIdOrderByIdAsc(torneoId);
        List<SedeDetalle> sedesDetalle = sedes.findByTorneoIdOrderByIdAsc(torneoId).stream()
                .map(s -> new SedeDetalle(s.getId(), s.getNombre(), s.getDireccion(), todasLasCanchas.stream()
                        .filter(c -> c.getSede().getId().equals(s.getId()))
                        .map(c -> new CanchaDetalle(c.getId(), c.getNombre(), c.getHabilitada()))
                        .toList()))
                .toList();
        return new ConfiguracionDetalle(
                franjas.findByTorneoIdOrderByFechaAscDesdeAsc(torneoId).stream()
                        .map(f -> new FranjaDetalle(f.getId(), f.getFecha(), f.getDesde(), f.getHasta())).toList(),
                sedesDetalle,
                categorias.findByTorneoIdOrderByIdAsc(torneoId).stream()
                        .map(c -> new CategoriaDetalle(c.getId(), c.getNombre(), c.getMinutosNetos(), c.getCantidadCuartos(),
                                c.getPausasMin(), c.getMargenDetencionesMin(), c.getMaxPartidosDia()))
                        .toList(),
                ramas.findByTorneoIdOrderByIdAsc(torneoId).stream().map(r -> new Creado(r.getId(), r.getNombre())).toList(),
                clubes.findByTorneoIdOrderByIdAsc(torneoId).stream().map(c -> new Creado(c.getId(), c.getNombre())).toList(),
                zonas.findByTorneoIdOrderByIdAsc(torneoId).stream()
                        .map(z -> new ZonaDetalle(z.getId(), z.getNombre(), z.getCategoria().getId(), z.getRama().getId(),
                                Etiquetas.zona(z)))
                        .toList());
    }

    @Transactional(readOnly = true)
    public List<EquipoResumen> listarEquipos(Long organizadorId, Long torneoId) {
        torneos.delOrganizador(torneoId, organizadorId);
        return equipos.findByTorneoIdOrderByIdAsc(torneoId).stream().map(Etiquetas::resumen).toList();
    }

    /** Un equipo con sus datos de contacto, para el formulario de edición (D67). 404 si el torneo es ajeno. */
    @Transactional(readOnly = true)
    public EquipoDetalle equipo(Long organizadorId, Long torneoId, Long equipoId) {
        torneos.delOrganizador(torneoId, organizadorId);
        Equipo e = equipos.findByIdAndTorneoId(equipoId, torneoId)
                .orElseThrow(() -> new NoEncontradoException("No existe el equipo " + equipoId + " en el torneo"));
        return new EquipoDetalle(e.getId(), e.getNombre(), e.getClub().getId(), e.getCategoria().getId(), e.getRama().getId(),
                e.getResponsableNombre(), e.getResponsableTelefono(), e.getZona() == null ? null : Etiquetas.zona(e.getZona()));
    }

    private Categoria categoriaDelTorneo(Long categoriaId, Long torneoId) {
        return categorias.findByIdAndTorneoId(categoriaId, torneoId)
                .orElseThrow(() -> new NoEncontradoException("No existe la categoría " + categoriaId + " en el torneo"));
    }

    private Rama ramaDelTorneo(Long ramaId, Long torneoId) {
        return ramas.findByIdAndTorneoId(ramaId, torneoId)
                .orElseThrow(() -> new NoEncontradoException("No existe la rama " + ramaId + " en el torneo"));
    }
}

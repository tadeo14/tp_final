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
import ar.edu.utn.fixture.dto.Respuestas.Creado;
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

    public TorneoDetalle crearTorneo(NuevoTorneo pedido) {
        Organizador organizador = organizadores.findById(pedido.organizadorId())
                .orElseThrow(() -> new NoEncontradoException("No existe el organizador " + pedido.organizadorId()));
        Torneo t = new Torneo();
        t.setOrganizador(organizador);
        t.setNombre(pedido.nombre());
        t.setTurnosCadaMin(pedido.turnosCadaMin());
        t.setRecambioMin(pedido.recambioMin());
        t.setDescansoMinimoMin(pedido.descansoMinimoMin());
        torneos.guardar(t);
        return Torneos.detalle(t);
    }

    @Transactional(readOnly = true)
    public TorneoDetalle obtenerTorneo(Long torneoId) {
        return Torneos.detalle(torneos.buscar(torneoId));
    }

    public Creado agregarFranja(Long torneoId, NuevaFranja pedido) {
        Torneo t = torneos.editable(torneoId);
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

    public Creado agregarSede(Long torneoId, NuevaSede pedido) {
        Sede s = new Sede();
        s.setTorneo(torneos.editable(torneoId));
        s.setNombre(pedido.nombre());
        s.setDireccion(pedido.direccion());
        sedes.save(s);
        return new Creado(s.getId(), s.getNombre());
    }

    public Creado agregarCancha(Long sedeId, NuevaCancha pedido) {
        Sede sede = sedes.findById(sedeId).orElseThrow(() -> new NoEncontradoException("No existe la sede " + sedeId));
        torneos.editable(sede.getTorneo().getId());
        Cancha c = new Cancha();
        c.setSede(sede);
        c.setNombre(pedido.nombre());
        c.setHabilitada(pedido.habilitada() == null || pedido.habilitada());
        canchas.save(c);
        return new Creado(c.getId(), sede.getNombre() + " - " + c.getNombre());
    }

    public Creado agregarCategoria(Long torneoId, NuevaCategoria pedido) {
        Categoria c = new Categoria();
        c.setTorneo(torneos.editable(torneoId));
        c.setNombre(pedido.nombre());
        c.setMinutosNetos(pedido.minutosNetos());
        c.setCantidadCuartos(pedido.cantidadCuartos());
        c.setPausasMin(pedido.pausasMin());
        c.setMargenDetencionesMin(pedido.margenDetencionesMin());
        c.setMaxPartidosDia(pedido.maxPartidosDia());
        categorias.save(c);
        return new Creado(c.getId(), c.getNombre());
    }

    public Creado agregarRama(Long torneoId, NuevaRama pedido) {
        Rama r = new Rama();
        r.setTorneo(torneos.editable(torneoId));
        r.setNombre(pedido.nombre());
        ramas.save(r);
        return new Creado(r.getId(), r.getNombre());
    }

    public Creado agregarClub(Long torneoId, NuevoClub pedido) {
        Club c = new Club();
        c.setTorneo(torneos.editable(torneoId));
        c.setNombre(pedido.nombre());
        clubes.save(c);
        return new Creado(c.getId(), c.getNombre());
    }

    /** Las zonas se definen antes del sorteo: la cantidad de zonas de una categoría y rama es la cantidad de filas (D18). */
    public Creado agregarZona(Long torneoId, NuevaZona pedido) {
        Torneo t = torneos.editable(torneoId);
        Zona z = new Zona();
        z.setTorneo(t);
        z.setCategoria(categoriaDelTorneo(pedido.categoriaId(), torneoId));
        z.setRama(ramaDelTorneo(pedido.ramaId(), torneoId));
        z.setNombre(pedido.nombre());
        zonas.save(z);
        return new Creado(z.getId(), Etiquetas.zona(z));
    }

    public EquipoResumen inscribirEquipo(Long torneoId, NuevoEquipo pedido) {
        Torneo t = torneos.editable(torneoId);
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

    @Transactional(readOnly = true)
    public List<EquipoResumen> listarEquipos(Long torneoId) {
        torneos.buscar(torneoId);
        return equipos.findByTorneoIdOrderByIdAsc(torneoId).stream().map(Etiquetas::resumen).toList();
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

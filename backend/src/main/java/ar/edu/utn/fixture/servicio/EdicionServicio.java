package ar.edu.utn.fixture.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.fixture.dto.Pedidos.NuevaCancha;
import ar.edu.utn.fixture.dto.Pedidos.NuevaCategoria;
import ar.edu.utn.fixture.dto.Pedidos.NuevaFranja;
import ar.edu.utn.fixture.dto.Pedidos.NuevaRama;
import ar.edu.utn.fixture.dto.Pedidos.NuevaSede;
import ar.edu.utn.fixture.dto.Pedidos.NuevoClub;
import ar.edu.utn.fixture.dto.Pedidos.NuevoEquipo;
import ar.edu.utn.fixture.dto.Respuestas.CanchaDetalle;
import ar.edu.utn.fixture.dto.Respuestas.CategoriaDetalle;
import ar.edu.utn.fixture.dto.Respuestas.Creado;
import ar.edu.utn.fixture.dto.Respuestas.EquipoResumen;
import ar.edu.utn.fixture.dto.Respuestas.FranjaDetalle;
import ar.edu.utn.fixture.dto.Respuestas.SedeDetalle;
import ar.edu.utn.fixture.entidad.Cancha;
import ar.edu.utn.fixture.entidad.Categoria;
import ar.edu.utn.fixture.entidad.Club;
import ar.edu.utn.fixture.entidad.Equipo;
import ar.edu.utn.fixture.entidad.FranjaHoraria;
import ar.edu.utn.fixture.entidad.Rama;
import ar.edu.utn.fixture.entidad.Sede;
import ar.edu.utn.fixture.repositorio.CanchaRepositorio;
import ar.edu.utn.fixture.repositorio.CategoriaRepositorio;
import ar.edu.utn.fixture.repositorio.ClubRepositorio;
import ar.edu.utn.fixture.repositorio.EquipoRepositorio;
import ar.edu.utn.fixture.repositorio.FranjaHorariaRepositorio;
import ar.edu.utn.fixture.repositorio.PartidoRepositorio;
import ar.edu.utn.fixture.repositorio.RamaRepositorio;
import ar.edu.utn.fixture.repositorio.SedeRepositorio;
import ar.edu.utn.fixture.repositorio.ZonaRepositorio;

/**
 * Edición y baja de la configuración del torneo (D66). Solo en BORRADOR y solo el dueño (D45): con el torneo
 * PUBLICADO responde 409 y hay que reabrirlo (D47). Si el cambio invalida el fixture, pide confirmación
 * ({@code confirmar}) y al confirmar borra los partidos ({@link Invalidaciones}). Lo publicado no se toca (D46).
 * Los pedidos son los mismos del alta: PUT reemplaza el elemento entero.
 */
@Service
@Transactional
public class EdicionServicio {

    private final Torneos torneos;
    private final Invalidaciones invalidaciones;
    private final PartidoRepositorio partidos;
    private final FranjaHorariaRepositorio franjas;
    private final SedeRepositorio sedes;
    private final CanchaRepositorio canchas;
    private final CategoriaRepositorio categorias;
    private final RamaRepositorio ramas;
    private final ClubRepositorio clubes;
    private final ZonaRepositorio zonas;
    private final EquipoRepositorio equipos;

    public EdicionServicio(Torneos torneos, Invalidaciones invalidaciones, PartidoRepositorio partidos,
            FranjaHorariaRepositorio franjas, SedeRepositorio sedes, CanchaRepositorio canchas, CategoriaRepositorio categorias, RamaRepositorio ramas,
            ClubRepositorio clubes, ZonaRepositorio zonas, EquipoRepositorio equipos) {
        this.torneos = torneos;
        this.invalidaciones = invalidaciones;
        this.partidos = partidos;
        this.franjas = franjas;
        this.sedes = sedes;
        this.canchas = canchas;
        this.categorias = categorias;
        this.ramas = ramas;
        this.clubes = clubes;
        this.zonas = zonas;
        this.equipos = equipos;
    }

    // ------------------------------------------------------------------ franjas

    /** Cambiar el día o el horario de una franja afecta la programación. */
    public FranjaDetalle editarFranja(Long organizadorId, Long torneoId, Long franjaId, NuevaFranja pedido, boolean confirmar) {
        torneos.editable(torneoId, organizadorId);
        FranjaHoraria f = franja(franjaId, torneoId);
        if (!pedido.hasta().isAfter(pedido.desde())) {
            throw new DatosInvalidosException("La franja tiene que terminar después de empezar");
        }
        boolean cambia = !f.getFecha().equals(pedido.fecha()) || !f.getDesde().equals(pedido.desde())
                || !f.getHasta().equals(pedido.hasta());
        if (cambia) {
            invalidaciones.siHayProgramados(torneoId, confirmar, "Cambiar la franja");
        }
        f.setFecha(pedido.fecha());
        f.setDesde(pedido.desde());
        f.setHasta(pedido.hasta());
        f = franjas.save(f);
        return new FranjaDetalle(f.getId(), f.getFecha(), f.getDesde(), f.getHasta());
    }

    public void borrarFranja(Long organizadorId, Long torneoId, Long franjaId, boolean confirmar) {
        torneos.editable(torneoId, organizadorId);
        FranjaHoraria f = franja(franjaId, torneoId);
        invalidaciones.siHayProgramados(torneoId, confirmar, "Borrar la franja");
        franjas.delete(f);
    }

    // ------------------------------------------------------------------ equipos

    /**
     * Nombre, club, responsable y teléfono no afectan el fixture. Cambiar la categoría o la rama de un equipo ya
     * sorteado sí: tiene que salir de su zona, así que se deshace el sorteo (con confirmación).
     */
    public EquipoResumen editarEquipo(Long organizadorId, Long torneoId, Long equipoId, NuevoEquipo pedido, boolean confirmar) {
        torneos.editable(torneoId, organizadorId);
        Equipo e = equipo(equipoId, torneoId);
        Club club = club(pedido.clubId(), torneoId);
        Categoria categoria = categoria(pedido.categoriaId(), torneoId);
        Rama rama = rama(pedido.ramaId(), torneoId);
        boolean cambiaGrupo = !e.getCategoria().getId().equals(categoria.getId()) || !e.getRama().getId().equals(rama.getId());
        if (cambiaGrupo && e.getZona() != null) {
            invalidaciones.siHaySorteo(torneoId, confirmar, "Cambiar la categoría o la rama del equipo " + e.getNombre());
            e = equipo(equipoId, torneoId); // se vuelve a leer: el sorteo se deshizo en la base
        }
        e.setClub(club);
        e.setCategoria(categoria);
        e.setRama(rama);
        e.setNombre(pedido.nombre());
        e.setResponsableNombre(pedido.responsableNombre());
        e.setResponsableTelefono(pedido.responsableTelefono());
        return Etiquetas.resumen(equipos.save(e));
    }

    /**
     * Dar de baja un equipo (por ejemplo, porque no viene). Si ya tiene partidos, al confirmar se borran todos los
     * partidos del torneo, pero el sorteo se conserva: los demás equipos siguen en su zona y alcanza con volver a
     * generar y programar. Su zona queda con un equipo menos.
     */
    public void borrarEquipo(Long organizadorId, Long torneoId, Long equipoId, boolean confirmar) {
        torneos.editable(torneoId, organizadorId);
        Equipo e = equipo(equipoId, torneoId);
        if (partidos.contarDelEquipo(equipoId) > 0) {
            invalidaciones.siHayPartidos(torneoId, confirmar, "Dar de baja el equipo " + e.getNombre());
            e = equipo(equipoId, torneoId);
        }
        equipos.delete(e);
    }

    // ------------------------------------------------------------------ sedes y canchas

    /** El nombre y la dirección de una sede no afectan el fixture. */
    public SedeDetalle editarSede(Long organizadorId, Long torneoId, Long sedeId, NuevaSede pedido) {
        torneos.editable(torneoId, organizadorId);
        Sede s = sede(sedeId, torneoId);
        s.setNombre(pedido.nombre());
        s.setDireccion(pedido.direccion());
        s = sedes.save(s);
        return new SedeDetalle(s.getId(), s.getNombre(), s.getDireccion(), canchas.findBySedeIdOrderByIdAsc(sedeId).stream()
                .map(EdicionServicio::detalle).toList());
    }

    /** Borra la sede con sus canchas. Si alguna cancha tiene partidos programados, pide confirmación. */
    public void borrarSede(Long organizadorId, Long torneoId, Long sedeId, boolean confirmar) {
        torneos.editable(torneoId, organizadorId);
        Sede s = sede(sedeId, torneoId);
        if (partidos.contarEnLaSede(sedeId) > 0) {
            invalidaciones.siHayProgramados(torneoId, confirmar, "Borrar la sede " + s.getNombre());
            s = sede(sedeId, torneoId);
        }
        canchas.deleteAll(canchas.findBySedeIdOrderByIdAsc(sedeId)); // primero sus canchas, después la sede
        sedes.delete(s);
    }

    /** Renombrar no afecta el fixture; deshabilitar una cancha con partidos programados sí (R5). */
    public CanchaDetalle editarCancha(Long organizadorId, Long sedeId, Long canchaId, NuevaCancha pedido, boolean confirmar) {
        Sede sede = sedeDelOrganizador(sedeId, organizadorId);
        Long torneoId = sede.getTorneo().getId();
        torneos.editable(torneoId, organizadorId);
        Cancha c = cancha(canchaId, sedeId);
        boolean habilitada = pedido.habilitada() == null || pedido.habilitada();
        if (c.getHabilitada() && !habilitada && partidos.countByCanchaId(canchaId) > 0) {
            invalidaciones.siHayProgramados(torneoId, confirmar, "Deshabilitar la cancha " + Etiquetas.cancha(c));
            c = cancha(canchaId, sedeId);
        }
        c.setNombre(pedido.nombre());
        c.setHabilitada(habilitada);
        return detalle(canchas.save(c));
    }

    public void borrarCancha(Long organizadorId, Long sedeId, Long canchaId, boolean confirmar) {
        Sede sede = sedeDelOrganizador(sedeId, organizadorId);
        Long torneoId = sede.getTorneo().getId();
        torneos.editable(torneoId, organizadorId);
        Cancha c = cancha(canchaId, sedeId);
        if (partidos.countByCanchaId(canchaId) > 0) {
            invalidaciones.siHayProgramados(torneoId, confirmar, "Borrar la cancha " + Etiquetas.cancha(c));
            c = cancha(canchaId, sedeId);
        }
        canchas.delete(c);
    }

    private static CanchaDetalle detalle(Cancha c) {
        return new CanchaDetalle(c.getId(), c.getNombre(), c.getHabilitada());
    }

    // ------------------------------------------------------------------ clubes

    /** El nombre del club no afecta el fixture (la vista pública lo muestra desde la foto, D48). */
    public Creado editarClub(Long organizadorId, Long torneoId, Long clubId, NuevoClub pedido) {
        torneos.editable(torneoId, organizadorId);
        Club c = club(clubId, torneoId);
        c.setNombre(pedido.nombre());
        c = clubes.save(c);
        return new Creado(c.getId(), c.getNombre());
    }

    /** Un club con equipos no se borra: primero hay que dar de baja sus equipos o pasarlos a otro club. */
    public void borrarClub(Long organizadorId, Long torneoId, Long clubId) {
        torneos.editable(torneoId, organizadorId);
        Club c = club(clubId, torneoId);
        long enUso = equipos.countByClubId(clubId);
        if (enUso > 0) {
            throw new ConflictoException("El club " + c.getNombre() + " tiene " + cantidad(enUso, "equipo", "equipos")
                    + ": para borrarlo, primero hay que dar de baja sus equipos o pasarlos a otro club");
        }
        clubes.delete(c);
    }

    // ------------------------------------------------------------------ categorías y ramas

    /**
     * El nombre y la cantidad de cuartos (informativa, no entra en el cálculo) no afectan el fixture. La duración y
     * el tope diario sí, si la categoría ya tiene partidos programados.
     */
    public CategoriaDetalle editarCategoria(Long organizadorId, Long torneoId, Long categoriaId, NuevaCategoria pedido,
            boolean confirmar) {
        torneos.editable(torneoId, organizadorId);
        Categoria c = categoria(categoriaId, torneoId);
        boolean cambiaElCalculo = !c.getMinutosNetos().equals(pedido.minutosNetos()) || !c.getPausasMin().equals(pedido.pausasMin())
                || !c.getMargenDetencionesMin().equals(pedido.margenDetencionesMin())
                || !c.getMaxPartidosDia().equals(pedido.maxPartidosDia());
        if (cambiaElCalculo && partidos.contarProgramadosDeLaCategoria(categoriaId) > 0) {
            invalidaciones.siHayProgramados(torneoId, confirmar,
                    "Cambiar la duración o el tope diario de la categoría " + c.getNombre());
            c = categoria(categoriaId, torneoId);
        }
        c.setNombre(pedido.nombre());
        c.setMinutosNetos(pedido.minutosNetos());
        c.setCantidadCuartos(pedido.cantidadCuartos());
        c.setPausasMin(pedido.pausasMin());
        c.setMargenDetencionesMin(pedido.margenDetencionesMin());
        c.setMaxPartidosDia(pedido.maxPartidosDia());
        c = categorias.save(c);
        return new CategoriaDetalle(c.getId(), c.getNombre(), c.getMinutosNetos(), c.getCantidadCuartos(), c.getPausasMin(),
                c.getMargenDetencionesMin(), c.getMaxPartidosDia());
    }

    /** Una categoría con zonas o equipos no se borra. */
    public void borrarCategoria(Long organizadorId, Long torneoId, Long categoriaId) {
        torneos.editable(torneoId, organizadorId);
        Categoria c = categoria(categoriaId, torneoId);
        noEstaEnUso("La categoría " + c.getNombre(), "borrarla", zonas.countByCategoriaId(categoriaId),
                equipos.countByCategoriaId(categoriaId));
        categorias.delete(c);
    }

    /** El nombre de la rama no afecta el fixture. */
    public Creado editarRama(Long organizadorId, Long torneoId, Long ramaId, NuevaRama pedido) {
        torneos.editable(torneoId, organizadorId);
        Rama r = rama(ramaId, torneoId);
        r.setNombre(pedido.nombre());
        r = ramas.save(r);
        return new Creado(r.getId(), r.getNombre());
    }

    /** Una rama con zonas o equipos no se borra. */
    public void borrarRama(Long organizadorId, Long torneoId, Long ramaId) {
        torneos.editable(torneoId, organizadorId);
        Rama r = rama(ramaId, torneoId);
        noEstaEnUso("La rama " + r.getNombre(), "borrarla", zonas.countByRamaId(ramaId), equipos.countByRamaId(ramaId));
        ramas.delete(r);
    }

    /** 409 que dice qué lo usa: "La categoría U9 tiene 1 zona y 4 equipos: para borrarla, primero…". */
    private static void noEstaEnUso(String que, String accion, long cantidadZonas, long cantidadEquipos) {
        if (cantidadZonas == 0 && cantidadEquipos == 0) {
            return;
        }
        String usos = cantidadZonas > 0 && cantidadEquipos > 0
                ? cantidad(cantidadZonas, "zona", "zonas") + " y " + cantidad(cantidadEquipos, "equipo", "equipos")
                : cantidadZonas > 0 ? cantidad(cantidadZonas, "zona", "zonas") : cantidad(cantidadEquipos, "equipo", "equipos");
        throw new ConflictoException(que + " tiene " + usos + ": para " + accion
                + ", primero hay que borrar sus zonas y dar de baja sus equipos (o pasarlos a otra)");
    }

    /** "1 equipo", "4 equipos". */
    private static String cantidad(long n, String singular, String plural) {
        return n + " " + (n == 1 ? singular : plural);
    }

    // ------------------------------------------------------------------ búsquedas dentro del torneo (404 si no está)

    private Sede sede(Long sedeId, Long torneoId) {
        return sedes.findByIdAndTorneoId(sedeId, torneoId)
                .orElseThrow(() -> new NoEncontradoException("No existe la sede " + sedeId + " en el torneo"));
    }

    /** Una sede de un torneo ajeno se trata igual que una que no existe (D45). */
    private Sede sedeDelOrganizador(Long sedeId, Long organizadorId) {
        return sedes.findById(sedeId)
                .filter(s -> s.getTorneo().getOrganizador().getId().equals(organizadorId))
                .orElseThrow(() -> new NoEncontradoException("No existe la sede " + sedeId));
    }

    private Cancha cancha(Long canchaId, Long sedeId) {
        return canchas.findByIdAndSedeId(canchaId, sedeId)
                .orElseThrow(() -> new NoEncontradoException("No existe la cancha " + canchaId + " en la sede"));
    }

    private Equipo equipo(Long equipoId, Long torneoId) {
        return equipos.findByIdAndTorneoId(equipoId, torneoId)
                .orElseThrow(() -> new NoEncontradoException("No existe el equipo " + equipoId + " en el torneo"));
    }

    private Club club(Long clubId, Long torneoId) {
        return clubes.findByIdAndTorneoId(clubId, torneoId)
                .orElseThrow(() -> new NoEncontradoException("No existe el club " + clubId + " en el torneo"));
    }

    private Categoria categoria(Long categoriaId, Long torneoId) {
        return categorias.findByIdAndTorneoId(categoriaId, torneoId)
                .orElseThrow(() -> new NoEncontradoException("No existe la categoría " + categoriaId + " en el torneo"));
    }

    private Rama rama(Long ramaId, Long torneoId) {
        return ramas.findByIdAndTorneoId(ramaId, torneoId)
                .orElseThrow(() -> new NoEncontradoException("No existe la rama " + ramaId + " en el torneo"));
    }

    private FranjaHoraria franja(Long franjaId, Long torneoId) {
        return franjas.findByIdAndTorneoId(franjaId, torneoId)
                .orElseThrow(() -> new NoEncontradoException("No existe la franja " + franjaId + " en el torneo"));
    }
}

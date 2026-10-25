package ar.edu.utn.fixture.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.fixture.dto.Pedidos.NuevaFranja;
import ar.edu.utn.fixture.dto.Pedidos.NuevoEquipo;
import ar.edu.utn.fixture.dto.Respuestas.EquipoResumen;
import ar.edu.utn.fixture.dto.Respuestas.FranjaDetalle;
import ar.edu.utn.fixture.entidad.Categoria;
import ar.edu.utn.fixture.entidad.Club;
import ar.edu.utn.fixture.entidad.Equipo;
import ar.edu.utn.fixture.entidad.FranjaHoraria;
import ar.edu.utn.fixture.entidad.Rama;
import ar.edu.utn.fixture.repositorio.CategoriaRepositorio;
import ar.edu.utn.fixture.repositorio.ClubRepositorio;
import ar.edu.utn.fixture.repositorio.EquipoRepositorio;
import ar.edu.utn.fixture.repositorio.FranjaHorariaRepositorio;
import ar.edu.utn.fixture.repositorio.PartidoRepositorio;
import ar.edu.utn.fixture.repositorio.RamaRepositorio;

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
    private final CategoriaRepositorio categorias;
    private final RamaRepositorio ramas;
    private final ClubRepositorio clubes;
    private final EquipoRepositorio equipos;

    public EdicionServicio(Torneos torneos, Invalidaciones invalidaciones, PartidoRepositorio partidos,
            FranjaHorariaRepositorio franjas, CategoriaRepositorio categorias, RamaRepositorio ramas,
            ClubRepositorio clubes, EquipoRepositorio equipos) {
        this.torneos = torneos;
        this.invalidaciones = invalidaciones;
        this.partidos = partidos;
        this.franjas = franjas;
        this.categorias = categorias;
        this.ramas = ramas;
        this.clubes = clubes;
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

    // ------------------------------------------------------------------ búsquedas dentro del torneo (404 si no está)

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

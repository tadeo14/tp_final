package ar.edu.utn.fixture.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ar.edu.utn.fixture.entidad.Partido;

public interface PartidoRepositorio extends JpaRepository<Partido, Long> {

    /** Todos los partidos del torneo con sus relaciones, en una sola consulta. */
    @Query("""
            select p from Partido p
            join fetch p.zona z
            join fetch z.categoria
            join fetch z.rama
            join fetch p.equipoLocal
            join fetch p.equipoVisitante
            left join fetch p.cancha c
            left join fetch c.sede
            where z.torneo.id = :torneoId
            order by z.id, p.ronda, p.id
            """)
    List<Partido> buscarFixture(@Param("torneoId") Long torneoId);

    @Query("select count(p) from Partido p where p.zona.torneo.id = :torneoId")
    long contarDelTorneo(@Param("torneoId") Long torneoId);

    @Query("select count(p) from Partido p where p.zona.torneo.id = :torneoId and p.inicio is not null")
    long contarProgramadosDelTorneo(@Param("torneoId") Long torneoId);

    long countByCanchaId(Long canchaId);

    @Query("select count(p) from Partido p where p.zona.categoria.id = :categoriaId and p.inicio is not null")
    long contarProgramadosDeLaCategoria(@Param("categoriaId") Long categoriaId);

    @Query("select count(p) from Partido p where p.cancha.sede.id = :sedeId")
    long contarEnLaSede(@Param("sedeId") Long sedeId);

    @Query("select count(p) from Partido p where p.equipoLocal.id = :equipoId or p.equipoVisitante.id = :equipoId")
    long contarDelEquipo(@Param("equipoId") Long equipoId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Partido p where p.zona.id in (select z.id from Zona z where z.torneo.id = :torneoId)")
    void borrarDelTorneo(@Param("torneoId") Long torneoId);
}

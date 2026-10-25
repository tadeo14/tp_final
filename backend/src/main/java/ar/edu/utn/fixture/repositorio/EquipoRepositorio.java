package ar.edu.utn.fixture.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ar.edu.utn.fixture.entidad.Equipo;

public interface EquipoRepositorio extends JpaRepository<Equipo, Long> {

    List<Equipo> findByTorneoIdOrderByIdAsc(Long torneoId);

    Optional<Equipo> findByIdAndTorneoId(Long id, Long torneoId);

    /** Equipos de una zona en el orden del sorteo (orden_en_zona). */
    List<Equipo> findByZonaIdOrderByOrdenEnZonaAsc(Long zonaId);

    long countByTorneoIdAndZonaIsNotNull(Long torneoId);

    long countByClubId(Long clubId);

    long countByCategoriaId(Long categoriaId);

    long countByRamaId(Long ramaId);

    long countByZonaId(Long zonaId);

    /** Deja todos los equipos del torneo sin zona: el sorteo se deshace (D66). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Equipo e set e.zona = null, e.ordenEnZona = null where e.torneo.id = :torneoId")
    void deshacerSorteo(@Param("torneoId") Long torneoId);
}

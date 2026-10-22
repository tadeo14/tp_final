package ar.edu.utn.fixture.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.fixture.entidad.Equipo;

public interface EquipoRepositorio extends JpaRepository<Equipo, Long> {

    List<Equipo> findByTorneoIdOrderByIdAsc(Long torneoId);

    Optional<Equipo> findByIdAndTorneoId(Long id, Long torneoId);

    /** Equipos de una zona en el orden del sorteo (orden_en_zona). */
    List<Equipo> findByZonaIdOrderByOrdenEnZonaAsc(Long zonaId);
}

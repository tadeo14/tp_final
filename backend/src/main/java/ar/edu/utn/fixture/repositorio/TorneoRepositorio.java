package ar.edu.utn.fixture.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.fixture.entidad.Torneo;

public interface TorneoRepositorio extends JpaRepository<Torneo, Long> {

    /** El torneo solo si es de ese organizador (D45). */
    Optional<Torneo> findByIdAndOrganizadorId(Long id, Long organizadorId);

    List<Torneo> findByOrganizadorIdOrderByIdAsc(Long organizadorId);
}

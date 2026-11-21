package ar.edu.utn.fixture.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.fixture.entidad.Club;

public interface ClubRepositorio extends JpaRepository<Club, Long> {

    Optional<Club> findByIdAndTorneoId(Long id, Long torneoId);
    List<Club> findByTorneoIdOrderByIdAsc(Long torneoId);
}

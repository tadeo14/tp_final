package ar.edu.utn.fixture.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.fixture.entidad.Rama;

public interface RamaRepositorio extends JpaRepository<Rama, Long> {

    Optional<Rama> findByIdAndTorneoId(Long id, Long torneoId);
    List<Rama> findByTorneoIdOrderByIdAsc(Long torneoId);
}

package ar.edu.utn.fixture.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.fixture.entidad.Sede;

public interface SedeRepositorio extends JpaRepository<Sede, Long> {

    List<Sede> findByTorneoIdOrderByIdAsc(Long torneoId);

    Optional<Sede> findByIdAndTorneoId(Long id, Long torneoId);
}

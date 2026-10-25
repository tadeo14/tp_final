package ar.edu.utn.fixture.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.fixture.entidad.Cancha;

public interface CanchaRepositorio extends JpaRepository<Cancha, Long> {

    /** Canchas habilitadas del torneo, en orden de carga: es el orden en que las recorre el Programador (D37). */
    List<Cancha> findBySedeTorneoIdAndHabilitadaTrueOrderByIdAsc(Long torneoId);
    /** Todas las canchas del torneo, habilitadas o no, en orden de carga. */
    List<Cancha> findBySedeTorneoIdOrderByIdAsc(Long torneoId);

    List<Cancha> findBySedeIdOrderByIdAsc(Long sedeId);

    Optional<Cancha> findByIdAndSedeId(Long id, Long sedeId);
}

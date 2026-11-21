package ar.edu.utn.fixture.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.fixture.entidad.Zona;

public interface ZonaRepositorio extends JpaRepository<Zona, Long> {

    List<Zona> findByTorneoIdOrderByIdAsc(Long torneoId);

    Optional<Zona> findByIdAndTorneoId(Long id, Long torneoId);

    long countByCategoriaId(Long categoriaId);

    long countByRamaId(Long ramaId);
}

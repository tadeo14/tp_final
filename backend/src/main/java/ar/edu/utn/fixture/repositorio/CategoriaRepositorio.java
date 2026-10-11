package ar.edu.utn.fixture.repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.fixture.entidad.Categoria;

public interface CategoriaRepositorio extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByIdAndTorneoId(Long id, Long torneoId);
}

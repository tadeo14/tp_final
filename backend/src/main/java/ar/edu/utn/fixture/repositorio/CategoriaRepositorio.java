package ar.edu.utn.fixture.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.fixture.entidad.Categoria;

public interface CategoriaRepositorio extends JpaRepository<Categoria, Long> {
}

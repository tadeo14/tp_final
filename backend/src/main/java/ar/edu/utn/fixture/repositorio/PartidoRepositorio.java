package ar.edu.utn.fixture.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.fixture.entidad.Partido;

public interface PartidoRepositorio extends JpaRepository<Partido, Long> {
}

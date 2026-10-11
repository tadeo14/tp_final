package ar.edu.utn.fixture.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.fixture.entidad.Rama;

public interface RamaRepositorio extends JpaRepository<Rama, Long> {
}

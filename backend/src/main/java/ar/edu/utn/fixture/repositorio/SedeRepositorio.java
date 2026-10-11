package ar.edu.utn.fixture.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.fixture.entidad.Sede;

public interface SedeRepositorio extends JpaRepository<Sede, Long> {
}

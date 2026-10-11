package ar.edu.utn.fixture.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.fixture.entidad.Equipo;

public interface EquipoRepositorio extends JpaRepository<Equipo, Long> {
}

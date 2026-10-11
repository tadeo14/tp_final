package ar.edu.utn.fixture.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.fixture.entidad.Torneo;

public interface TorneoRepositorio extends JpaRepository<Torneo, Long> {
}

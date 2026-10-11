package ar.edu.utn.fixture.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.fixture.entidad.Cancha;

public interface CanchaRepositorio extends JpaRepository<Cancha, Long> {
}

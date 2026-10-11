package ar.edu.utn.fixture.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.fixture.entidad.Club;

public interface ClubRepositorio extends JpaRepository<Club, Long> {
}

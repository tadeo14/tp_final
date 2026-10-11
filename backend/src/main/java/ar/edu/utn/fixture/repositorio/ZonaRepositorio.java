package ar.edu.utn.fixture.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.fixture.entidad.Zona;

public interface ZonaRepositorio extends JpaRepository<Zona, Long> {
}

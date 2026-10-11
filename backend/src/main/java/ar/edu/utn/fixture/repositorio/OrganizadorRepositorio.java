package ar.edu.utn.fixture.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.fixture.entidad.Organizador;

public interface OrganizadorRepositorio extends JpaRepository<Organizador, Long> {
}

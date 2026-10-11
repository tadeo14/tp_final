package ar.edu.utn.fixture.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.fixture.entidad.FranjaHoraria;

public interface FranjaHorariaRepositorio extends JpaRepository<FranjaHoraria, Long> {
}

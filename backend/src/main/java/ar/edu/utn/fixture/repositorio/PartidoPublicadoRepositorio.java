package ar.edu.utn.fixture.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.fixture.entidad.PartidoPublicado;

public interface PartidoPublicadoRepositorio extends JpaRepository<PartidoPublicado, Long> {

    List<PartidoPublicado> findByPublicacionIdOrderByInicioAscIdAsc(Long publicacionId);
}

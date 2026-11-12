package ar.edu.utn.fixture.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.fixture.entidad.Publicacion;

public interface PublicacionRepositorio extends JpaRepository<Publicacion, Long> {

    /** La versión vigente: la de número más alto. */
    Optional<Publicacion> findFirstByTorneoIdOrderByNumeroDesc(Long torneoId);

    /** Historial, de la más nueva a la más vieja. */
    List<Publicacion> findByTorneoIdOrderByNumeroDesc(Long torneoId);
}

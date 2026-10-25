package ar.edu.utn.fixture.servicio;

import org.springframework.stereotype.Component;

import ar.edu.utn.fixture.dto.Respuestas.VersionPublicada;
import ar.edu.utn.fixture.entidad.Publicacion;
import ar.edu.utn.fixture.repositorio.PublicacionRepositorio;

/**
 * Búsqueda de la versión publicada vigente, compartida por la vista pública y el organizador (D46).
 */
@Component
public class Publicaciones {

    private final PublicacionRepositorio repositorio;

    public Publicaciones(PublicacionRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * La última versión publicada del torneo, aunque después se haya reabierto (D47).
     * Si nunca se publicó, para el público no existe: 404.
     */
    public Publicacion vigente(Long torneoId) {
        return repositorio.findFirstByTorneoIdOrderByNumeroDesc(torneoId)
                .orElseThrow(() -> new NoEncontradoException("No hay un torneo publicado con id " + torneoId));
    }

    static VersionPublicada version(Publicacion p) {
        return new VersionPublicada(p.getNumero(), p.getPublicadaEn(), p.getMotivo());
    }
}

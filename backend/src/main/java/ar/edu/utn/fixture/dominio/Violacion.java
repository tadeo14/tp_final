package ar.edu.utn.fixture.dominio;

import java.util.List;

/**
 * Una regla que no se cumple: cuál es, por qué y en qué partidos.
 */
public record Violacion<E, C>(Restriccion restriccion, String detalle, List<PartidoProgramado<E, C>> partidos) {

    public Violacion {
        partidos = List.copyOf(partidos);
    }
}

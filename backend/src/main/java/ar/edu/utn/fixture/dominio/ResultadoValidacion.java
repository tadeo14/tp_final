package ar.edu.utn.fixture.dominio;

import java.util.List;

/**
 * Resultado del ValidadorRestricciones. El fixture es válido solo si no hay ninguna violación.
 */
public record ResultadoValidacion<E, C>(List<Violacion<E, C>> violaciones) {

    public ResultadoValidacion {
        violaciones = List.copyOf(violaciones);
    }

    public boolean esValido() {
        return violaciones.isEmpty();
    }
}

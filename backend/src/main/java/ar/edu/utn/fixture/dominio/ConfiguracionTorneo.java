package ar.edu.utn.fixture.dominio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Parámetros del torneo que usan el validador y el programador, y el cálculo del bloque de cada partido.
 *
 * <p>La duración programada y el bloque se calculan cada vez, no se guardan (D7, D8):
 * <pre>
 * duración programada = minutos netos + pausas + margen por detenciones + recambio
 * bloque              = duración programada redondeada hacia arriba al múltiplo de "turnos cada"
 * fin                 = inicio + bloque
 * </pre>
 *
 * @param <C> cómo se identifica una cancha (en la aplicación, su id; en los tests, un nombre)
 */
public record ConfiguracionTorneo<C>(
        int turnosCadaMin,
        int recambioMin,
        int descansoMinimoMin,
        List<Franja> franjas,
        Set<C> canchasHabilitadas) {

    public ConfiguracionTorneo {
        if (turnosCadaMin <= 0) {
            throw new IllegalArgumentException("\"Turnos cada\" tiene que ser mayor que 0");
        }
        franjas = List.copyOf(franjas);
        canchasHabilitadas = Set.copyOf(canchasHabilitadas);
    }

    public int duracionProgramadaMin(ParametrosCategoria categoria) {
        return categoria.minutosNetos() + categoria.pausasMin() + categoria.margenDetencionesMin() + recambioMin;
    }

    public int bloqueMin(ParametrosCategoria categoria) {
        int duracion = duracionProgramadaMin(categoria);
        int turnos = (duracion + turnosCadaMin - 1) / turnosCadaMin; // división redondeando hacia arriba
        return turnos * turnosCadaMin;
    }

    public LocalDateTime fin(LocalDateTime inicio, ParametrosCategoria categoria) {
        return inicio.plusMinutes(bloqueMin(categoria));
    }
}

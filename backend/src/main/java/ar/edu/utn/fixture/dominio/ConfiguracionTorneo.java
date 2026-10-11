package ar.edu.utn.fixture.dominio;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

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
 * @param canchasHabilitadas canchas donde se puede programar, en el orden en que las recorre el Programador (D37)
 * @param <C> cómo se identifica una cancha (en la aplicación, su id; en los tests, un nombre)
 */
public record ConfiguracionTorneo<C>(
        int turnosCadaMin,
        int recambioMin,
        int descansoMinimoMin,
        List<Franja> franjas,
        List<C> canchasHabilitadas) {

    public ConfiguracionTorneo {
        if (turnosCadaMin <= 0) {
            throw new IllegalArgumentException("\"Turnos cada\" tiene que ser mayor que 0");
        }
        franjas = List.copyOf(franjas);
        canchasHabilitadas = List.copyOf(canchasHabilitadas);
        if (new HashSet<>(canchasHabilitadas).size() != canchasHabilitadas.size()) {
            throw new IllegalArgumentException("Hay canchas repetidas");
        }
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

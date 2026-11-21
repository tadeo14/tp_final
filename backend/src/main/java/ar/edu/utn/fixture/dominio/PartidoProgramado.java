package ar.edu.utn.fixture.dominio;

import java.time.LocalDateTime;

/**
 * Un partido con (o sin) cancha y horario, tal como lo recibe el validador.
 * {@code cancha} e {@code inicio} son null si el partido todavía no se programó (eso viola R8).
 *
 * @param <E> cómo se identifica un equipo
 * @param <C> cómo se identifica una cancha
 */
public record PartidoProgramado<E, C>(
        E local,
        E visitante,
        ParametrosCategoria categoria,
        C cancha,
        LocalDateTime inicio) {

    public boolean estaProgramado() {
        return cancha != null && inicio != null;
    }

    public boolean juega(E equipo) {
        return local.equals(equipo) || visitante.equals(equipo);
    }

    /** Texto para los mensajes de error, por ejemplo "A vs B (Cancha 1, 2030-01-05 09:00)". */
    public String describir() {
        String donde = estaProgramado() ? cancha + ", " + inicio.toLocalDate() + " " + inicio.toLocalTime() : "sin programar";
        return local + " vs " + visitante + " (" + donde + ")";
    }
}

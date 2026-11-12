package ar.edu.utn.fixture.dominio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Ventana de un día en la que se puede jugar (R4).
 */
public record Franja(LocalDate fecha, LocalTime desde, LocalTime hasta) {

    public LocalDateTime apertura() {
        return fecha.atTime(desde);
    }

    public LocalDateTime cierre() {
        return fecha.atTime(hasta);
    }
}

package ar.edu.utn.fixture.dominio;

/**
 * Cómo terminó el Programador (D34).
 */
public enum EstadoProgramacion {
    /** Todos los partidos quedaron programados y el fixture es válido. */
    PROGRAMADO,
    /** El chequeo por conteo demuestra que con esta configuración no hay solución. */
    NO_EXISTE,
    /** El conteo no demuestra que sea imposible, pero el greedy no encontró lugar para algún partido. */
    NO_ENCONTRADA
}

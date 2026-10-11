package ar.edu.utn.fixture.dto;

import java.time.LocalDateTime;
import java.util.List;

import ar.edu.utn.fixture.entidad.EstadoTorneo;

/**
 * Cuerpos de las respuestas de la API.
 */
public final class Respuestas {

    private Respuestas() {
    }

    /** Respuesta simple al crear algo: el id y un nombre para reconocerlo. */
    public record Creado(Long id, String nombre) {
    }

    public record OrganizadorCreado(Long id, String nombre, String email) {
    }

    public record TorneoDetalle(Long id, String nombre, EstadoTorneo estado,
            int turnosCadaMin, int recambioMin, int descansoMinimoMin) {
    }

    /** Sin datos de contacto: se puede mostrar en la vista pública. */
    public record EquipoResumen(Long id, String nombre, String club, String categoria, String rama, String zona) {
    }

    public record ZonaSorteada(String zona, List<String> equipos) {
    }

    public record Generacion(int cantidadPartidos) {
    }

    /** {@code fin} se calcula (inicio + bloque): no se guarda (D8). */
    public record PartidoDetalle(Long id, String zona, int ronda, String local, String visitante,
            String sede, String cancha, LocalDateTime inicio, LocalDateTime fin) {
    }

    public record ViolacionDetalle(String regla, String descripcion, String detalle) {
    }

    public record Validacion(boolean valido, List<ViolacionDetalle> violaciones) {
    }

    public record PartidoDeEquipo(int ronda, String rival, String condicion, String sede, String cancha,
            LocalDateTime inicio, LocalDateTime fin) {
    }

    /** Fixture de un equipo. Las rondas libres se deducen: no se guardan (D18). */
    public record FixtureDeEquipo(Long equipoId, String equipo, String club, String zona,
            List<PartidoDeEquipo> partidos, List<Integer> rondasLibres) {
    }

    /** Formato de todos los errores de la API. */
    public record Error(String error, List<String> detalles) {
    }
}

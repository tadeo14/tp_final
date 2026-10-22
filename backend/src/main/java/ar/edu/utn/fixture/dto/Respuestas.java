package ar.edu.utn.fixture.dto;

import java.time.Instant;
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

    /** Respuesta del login: el token se manda después en el encabezado "Authorization: Bearer {token}". */
    public record Sesion(String token, String tipo, Instant expiraEn, OrganizadorCreado organizador) {
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

    public record PartidoDeEquipo(int ronda, String rival, String clubRival, String condicion, String sede, String cancha,
            LocalDateTime inicio, LocalDateTime fin) {
    }

    /** Qué versión publicada se está mostrando (D46). {@code motivo} es null en la versión 1. */
    public record VersionPublicada(int numero, Instant publicadaEn, String motivo) {
    }

    /** Respuesta de publicar: la versión que se acaba de crear. */
    public record Publicado(Long torneoId, EstadoTorneo estado, VersionPublicada version) {
    }

    /** Equipo según la última versión publicada (sin datos de contacto). */
    public record EquipoPublicado(Long id, String nombre, String club, String categoria, String rama, String zona) {
    }

    public record EquiposPublicados(VersionPublicada version, List<EquipoPublicado> equipos) {
    }

    /** Fixture de un equipo según la última versión publicada. Las rondas libres se deducen: no se guardan (D18). */
    public record FixtureDeEquipo(VersionPublicada version, Long equipoId, String equipo, String club, String zona,
            List<PartidoDeEquipo> partidos, List<Integer> rondasLibres) {
    }

    /** Formato de todos los errores de la API. */
    public record Error(String error, List<String> detalles) {
    }
}

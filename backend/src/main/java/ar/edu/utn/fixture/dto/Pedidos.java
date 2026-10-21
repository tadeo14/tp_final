package ar.edu.utn.fixture.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Cuerpos de los pedidos (POST) de la API. Las anotaciones validan los datos antes de llegar al servicio.
 */
public final class Pedidos {

    private Pedidos() {
    }

    public record NuevoOrganizador(
            @NotBlank String nombre,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, message = "debe tener al menos 8 caracteres") String contrasenia) {
    }

    public record Credenciales(@NotBlank @Email String email, @NotBlank String contrasenia) {
    }

    /** El organizador no viene en el pedido: es el del token (D45). */
    public record NuevoTorneo(
            @NotBlank String nombre,
            @NotNull @Positive Integer turnosCadaMin,
            @NotNull @PositiveOrZero Integer recambioMin,
            @NotNull @PositiveOrZero Integer descansoMinimoMin) {
    }

    public record NuevaFranja(@NotNull LocalDate fecha, @NotNull LocalTime desde, @NotNull LocalTime hasta) {
    }

    public record NuevaSede(@NotBlank String nombre, String direccion) {
    }

    /** {@code habilitada} es opcional: si no viene, la cancha queda habilitada. */
    public record NuevaCancha(@NotBlank String nombre, Boolean habilitada) {
    }

    public record NuevaCategoria(
            @NotBlank String nombre,
            @NotNull @Positive Integer minutosNetos,
            @NotNull @Positive Integer cantidadCuartos,
            @NotNull @PositiveOrZero Integer pausasMin,
            @NotNull @PositiveOrZero Integer margenDetencionesMin,
            @NotNull @Positive Integer maxPartidosDia) {
    }

    public record NuevaRama(@NotBlank String nombre) {
    }

    public record NuevoClub(@NotBlank String nombre) {
    }

    public record NuevaZona(@NotNull Long categoriaId, @NotNull Long ramaId, @NotBlank String nombre) {
    }

    /** Datos personales mínimos: solo responsable y teléfono (regla 7). */
    public record NuevoEquipo(
            @NotNull Long clubId,
            @NotNull Long categoriaId,
            @NotNull Long ramaId,
            @NotBlank String nombre,
            @NotBlank String responsableNombre,
            @NotBlank String responsableTelefono) {
    }
}

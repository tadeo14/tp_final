package ar.edu.utn.fixture.controlador;

import java.util.Map;

import org.springframework.validation.FieldError;

/**
 * Traduce un error de validación de un pedido (@NotBlank, @Positive...) a un mensaje en español, con el campo
 * nombrado como lo ve el usuario en la pantalla: "turnosCadaMin: must be greater than 0" pasa a ser
 * "Turnos cada (min): tiene que ser mayor que 0" (D61).
 */
final class MensajesValidacion {

    /** Nombre de cada campo de {@code Pedidos} tal como aparece en el frontend. */
    private static final Map<String, String> CAMPOS = Map.ofEntries(
            Map.entry("nombre", "Nombre"),
            Map.entry("email", "Email"),
            Map.entry("contrasenia", "Contraseña"),
            Map.entry("turnosCadaMin", "Turnos cada (min)"),
            Map.entry("recambioMin", "Recambio (min)"),
            Map.entry("descansoMinimoMin", "Descanso mínimo (min)"),
            Map.entry("motivo", "Motivo de la corrección"),
            Map.entry("fecha", "Día"),
            Map.entry("desde", "Desde"),
            Map.entry("hasta", "Hasta"),
            Map.entry("direccion", "Dirección"),
            Map.entry("habilitada", "Habilitada"),
            Map.entry("minutosNetos", "Minutos de juego"),
            Map.entry("cantidadCuartos", "Cuartos"),
            Map.entry("pausasMin", "Pausas (min, en total)"),
            Map.entry("margenDetencionesMin", "Margen por detenciones (min)"),
            Map.entry("maxPartidosDia", "Máx. partidos por día"),
            Map.entry("clubId", "Club"),
            Map.entry("categoriaId", "Categoría"),
            Map.entry("ramaId", "Rama"),
            Map.entry("responsableNombre", "Responsable"),
            Map.entry("responsableTelefono", "Teléfono del responsable"),
            Map.entry("maximoEquiposPorZona", "Máximo de equipos por zona"));

    /** Mensaje por tipo de anotación. Las que no están (por ejemplo @Size) ya traen su mensaje en español. */
    private static final Map<String, String> MENSAJES = Map.of(
            "NotBlank", "es obligatorio",
            "NotNull", "es obligatorio",
            "Positive", "tiene que ser mayor que 0",
            "PositiveOrZero", "no puede ser negativo",
            "Email", "no es un email válido");

    private MensajesValidacion() {
    }

    static String traducir(FieldError error) {
        String campo = CAMPOS.getOrDefault(error.getField(), error.getField());
        String mensaje = MENSAJES.getOrDefault(error.getCode(), error.getDefaultMessage());
        return campo + ": " + mensaje;
    }
}

package ar.edu.utn.fixture.controlador;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ar.edu.utn.fixture.dto.Respuestas.Error;
import ar.edu.utn.fixture.servicio.ConflictoException;
import ar.edu.utn.fixture.servicio.CredencialesInvalidasException;
import ar.edu.utn.fixture.servicio.DatosInvalidosException;
import ar.edu.utn.fixture.servicio.FixtureInvalidoException;
import ar.edu.utn.fixture.servicio.NoEncontradoException;

/**
 * Traduce las excepciones a respuestas HTTP con un formato único: {"error": "...", "detalles": [...]}.
 */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(NoEncontradoException.class)
    public ResponseEntity<Error> noEncontrado(NoEncontradoException e) {
        return respuesta(HttpStatus.NOT_FOUND, e.getMessage(), List.of());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Error> credencialesInvalidas(CredencialesInvalidasException e) {
        return respuesta(HttpStatus.UNAUTHORIZED, e.getMessage(), List.of());
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<Error> conflicto(ConflictoException e) {
        return respuesta(HttpStatus.CONFLICT, e.getMessage(), List.of());
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<Error> datosInvalidos(DatosInvalidosException e) {
        return respuesta(HttpStatus.BAD_REQUEST, e.getMessage(), List.of());
    }

    @ExceptionHandler(FixtureInvalidoException.class)
    public ResponseEntity<Error> fixtureInvalido(FixtureInvalidoException e) {
        return respuesta(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), e.getDetalles());
    }

    /** Fallan las anotaciones de los pedidos (@NotBlank, @Positive...). Los detalles van en español y ordenados (D61). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Error> pedidoInvalido(MethodArgumentNotValidException e) {
        List<String> detalles = e.getBindingResult().getFieldErrors().stream()
                .map(MensajesValidacion::traducir)
                .sorted()
                .toList();
        return respuesta(HttpStatus.BAD_REQUEST, "Hay datos inválidos en el pedido", detalles);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Error> pedidoIlegible(HttpMessageNotReadableException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo del pedido no tiene el formato esperado", List.of());
    }

    /** La base rechazó el dato: nombre repetido o una restricción CHECK / FK (D14). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Error> integridad(DataIntegrityViolationException e) {
        return respuesta(HttpStatus.CONFLICT,
                "La base rechazó el dato: puede estar repetido o no ser coherente con los datos ya cargados", List.of());
    }

    private static ResponseEntity<Error> respuesta(HttpStatus estado, String mensaje, List<String> detalles) {
        return ResponseEntity.status(estado).body(new Error(mensaje, detalles));
    }
}

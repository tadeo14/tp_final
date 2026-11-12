package ar.edu.utn.fixture.servicio;

/**
 * Email o contraseña incorrectos. La API responde 401 con el mismo mensaje en los dos casos,
 * para no revelar qué emails están registrados.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Email o contraseña incorrectos");
    }
}

package ar.edu.utn.fixture.servicio;

/** La acción no se puede hacer en el estado actual del torneo, por ejemplo modificar uno publicado (D20). La API responde 409. */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}

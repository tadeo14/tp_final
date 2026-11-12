package ar.edu.utn.fixture.servicio;

/** Lo pedido no existe (o no pertenece al torneo indicado). La API responde 404. */
public class NoEncontradoException extends RuntimeException {

    public NoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

package ar.edu.utn.fixture.servicio;

/**
 * El cambio invalida el fixture (los partidos generados o el sorteo) y el organizador no lo confirmó (D66).
 * La API responde 409 con {@code requiereConfirmacion: true}: repitiendo el pedido con {@code confirmar=true} se hace.
 */
public class ConfirmacionRequeridaException extends RuntimeException {

    public ConfirmacionRequeridaException(String mensaje) {
        super(mensaje);
    }
}

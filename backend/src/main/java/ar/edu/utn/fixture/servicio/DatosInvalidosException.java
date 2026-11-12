package ar.edu.utn.fixture.servicio;

/** Los datos no alcanzan para hacer lo pedido (por ejemplo, faltan zonas o equipos). La API responde 400. */
public class DatosInvalidosException extends RuntimeException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}

package ar.edu.utn.fixture.servicio;

import java.util.List;

/**
 * No hay programación válida, o el fixture no cumple R1–R8 al publicar. La API responde 422
 * con el mensaje y el detalle de qué zona, equipo o partido falla (restricciones.md, sección 5).
 */
public class FixtureInvalidoException extends RuntimeException {

    private final List<String> detalles;

    public FixtureInvalidoException(String mensaje, List<String> detalles) {
        super(mensaje);
        this.detalles = List.copyOf(detalles);
    }

    public List<String> getDetalles() {
        return detalles;
    }
}

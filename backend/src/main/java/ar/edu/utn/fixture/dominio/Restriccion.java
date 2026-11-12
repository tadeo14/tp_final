package ar.edu.utn.fixture.dominio;

/**
 * Restricciones obligatorias del fixture (docs/restricciones.md, sección 4).
 */
public enum Restriccion {
    R1("Cancha sin superposición"),
    R2("Equipo sin superposición"),
    R3("Descanso mínimo"),
    R4("Dentro de franja"),
    R5("Cancha habilitada"),
    R6("Tope diario"),
    R7("Inicio en turno"),
    R8("Completitud");

    private final String descripcion;

    Restriccion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String descripcion() {
        return descripcion;
    }
}

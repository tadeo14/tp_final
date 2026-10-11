package ar.edu.utn.fixture.dominio;

/**
 * Un partido que sale del GeneradorEnfrentamientos y todavía no tiene cancha ni horario.
 *
 * @param zona      nombre de la zona para los mensajes, por ejemplo "U13 Femenino - Zona A"
 * @param categoria parámetros de la categoría de la zona (definen el bloque y el tope diario)
 * @param <E>       cómo se identifica un equipo
 */
public record PartidoAProgramar<E>(E local, E visitante, int ronda, String zona, ParametrosCategoria categoria) {

    public String describir() {
        return local + " vs " + visitante + " (zona " + zona + ", ronda " + ronda + ")";
    }
}

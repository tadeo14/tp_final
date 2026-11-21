package ar.edu.utn.fixture.dominio;

/**
 * Un partido generado: quién juega contra quién y en qué ronda (fecha) de la zona.
 * Todavía no tiene cancha ni horario: eso lo asigna el Programador.
 *
 * @param <E> cómo se identifica un equipo (en la aplicación, su id; en los tests, un nombre)
 */
public record Enfrentamiento<E>(E local, E visitante, int ronda) {
}

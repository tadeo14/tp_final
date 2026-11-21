package ar.edu.utn.fixture.dominio;

/**
 * Parámetros de una categoría que intervienen en la programación. Vienen de la configuración del
 * torneo (tabla categoria): nada queda fijo en el código (regla 1).
 *
 * @param minutosNetos          tiempo de juego total (ej. U9: 4 × 7 = 28)
 * @param pausasMin             suma de pausas entre cuartos y entretiempo
 * @param margenDetencionesMin  margen por tiros libres e imprevistos
 * @param maxPartidosDia        tope de partidos por día de un equipo (R6)
 */
public record ParametrosCategoria(int minutosNetos, int pausasMin, int margenDetencionesMin, int maxPartidosDia) {
}

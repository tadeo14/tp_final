package ar.edu.utn.fixture.dominio;

import java.util.List;

/**
 * Resultado del Programador.
 *
 * @param mensaje  mensaje para el organizador (vacío si se programó todo)
 * @param detalles qué zona, equipo o partido no se pudo ubicar
 * @param partidos los partidos programados, en el mismo orden en que se recibieron; vacío si no hubo solución
 */
public record ResultadoProgramacion<E, C>(
        EstadoProgramacion estado,
        String mensaje,
        List<String> detalles,
        List<PartidoProgramado<E, C>> partidos) {

    public static final String NO_EXISTE = "No existe una programación válida con la configuración actual";
    public static final String NO_ENCONTRADA = "No se encontró una programación válida con la configuración actual";

    public ResultadoProgramacion {
        detalles = List.copyOf(detalles);
        partidos = List.copyOf(partidos);
    }

    static <E, C> ResultadoProgramacion<E, C> programado(List<PartidoProgramado<E, C>> partidos) {
        return new ResultadoProgramacion<>(EstadoProgramacion.PROGRAMADO, "", List.of(), partidos);
    }

    static <E, C> ResultadoProgramacion<E, C> noExiste(List<String> detalles) {
        return new ResultadoProgramacion<>(EstadoProgramacion.NO_EXISTE, NO_EXISTE, detalles, List.of());
    }

    static <E, C> ResultadoProgramacion<E, C> noEncontrada(List<String> detalles) {
        return new ResultadoProgramacion<>(EstadoProgramacion.NO_ENCONTRADA, NO_ENCONTRADA, detalles, List.of());
    }
}

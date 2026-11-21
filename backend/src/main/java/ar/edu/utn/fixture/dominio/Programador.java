package ar.edu.utn.fixture.dominio;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Asigna cancha y horario a cada partido con un algoritmo greedy: toma el primer lugar válido y no vuelve atrás.
 * Ver la explicación en docs/algoritmo-programador.md.
 *
 * <ol>
 *   <li>Chequeo por conteo (D35): si demuestra que no hay solución, responde "No existe…" sin programar.</li>
 *   <li>Ordena los partidos: por ronda y, dentro de la ronda, primero los de bloque más largo y zona más grande (D33).</li>
 *   <li>A cada partido le da el primer casillero (horario de turno + cancha habilitada) que el
 *       ValidadorRestricciones acepta (D36). Si no hay ninguno, responde "No se encontró…" con ese partido.</li>
 *   <li>Antes de devolver el fixture, lo valida completo con el ValidadorRestricciones (regla 3).</li>
 * </ol>
 *
 * Nunca relaja restricciones ni deja partidos sin programar (regla 5).
 */
public class Programador {

    private final ValidadorRestricciones validador;

    public Programador(ValidadorRestricciones validador) {
        this.validador = validador;
    }

    public <E, C> ResultadoProgramacion<E, C> programar(ConfiguracionTorneo<C> config, List<PartidoAProgramar<E>> partidos) {
        List<String> imposibles = chequeoPorConteo(config, partidos);
        if (!imposibles.isEmpty()) {
            return ResultadoProgramacion.noExiste(imposibles);
        }

        List<Franja> franjas = config.franjas().stream().sorted(Comparator.comparing(Franja::apertura)).toList();
        @SuppressWarnings("unchecked")
        PartidoProgramado<E, C>[] ubicados = new PartidoProgramado[partidos.size()];
        List<PartidoProgramado<E, C>> yaUbicados = new ArrayList<>();

        for (int indice : ordenDeProceso(config, partidos)) {
            PartidoAProgramar<E> partido = partidos.get(indice);
            PartidoProgramado<E, C> lugar = primerLugarValido(config, franjas, partido, yaUbicados);
            if (lugar == null) {
                return ResultadoProgramacion.noEncontrada(List.of("No se pudo ubicar " + partido.describir()));
            }
            ubicados[indice] = lugar;
            yaUbicados.add(lugar);
        }

        List<PartidoProgramado<E, C>> fixture = Arrays.asList(ubicados);
        ResultadoValidacion<E, C> validacion = validador.validar(config, fixture);
        if (!validacion.esValido()) {
            // No debería pasar: cada partido se ubicó solo donde el validador lo aceptó. Si pasa, es un error de programación.
            throw new IllegalStateException("El fixture armado no es válido: " + validacion.violaciones());
        }
        return ResultadoProgramacion.programado(fixture);
    }

    // ------------------------------------------------------------------ orden (D33)

    /** Índices de los partidos en el orden en que se procesan: ronda, bloque más largo, zona más grande. */
    private static <E, C> List<Integer> ordenDeProceso(ConfiguracionTorneo<C> config, List<PartidoAProgramar<E>> partidos) {
        Map<String, Set<E>> equiposPorZona = new LinkedHashMap<>();
        for (PartidoAProgramar<E> p : partidos) {
            Set<E> equipos = equiposPorZona.computeIfAbsent(p.zona(), z -> new HashSet<>());
            equipos.add(p.local());
            equipos.add(p.visitante());
        }
        Comparator<Integer> orden = Comparator
                .<Integer>comparingInt(i -> partidos.get(i).ronda())
                .thenComparing(i -> config.bloqueMin(partidos.get(i).categoria()), Comparator.reverseOrder())
                .thenComparing(i -> equiposPorZona.get(partidos.get(i).zona()).size(), Comparator.reverseOrder())
                .thenComparing(i -> partidos.get(i).zona())
                .thenComparingInt(i -> i);
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < partidos.size(); i++) {
            indices.add(i);
        }
        indices.sort(orden);
        return indices;
    }

    // ------------------------------------------------------------------ greedy (D36)

    /**
     * Recorre los casilleros de más temprano a más tarde y, en cada horario, las canchas en orden.
     * Solo se ofrecen horarios de turno (R7) en canchas habilitadas (R5) donde el bloque entra en la franja (R4);
     * el resto de las reglas las decide el ValidadorRestricciones.
     */
    private <E, C> PartidoProgramado<E, C> primerLugarValido(ConfiguracionTorneo<C> config, List<Franja> franjas,
            PartidoAProgramar<E> partido, List<PartidoProgramado<E, C>> yaUbicados) {
        int bloque = config.bloqueMin(partido.categoria());
        for (Franja franja : franjas) {
            for (LocalDateTime inicio = franja.apertura();
                    !inicio.plusMinutes(bloque).isAfter(franja.cierre());
                    inicio = inicio.plusMinutes(config.turnosCadaMin())) {
                for (C cancha : config.canchasHabilitadas()) {
                    PartidoProgramado<E, C> candidato = new PartidoProgramado<>(
                            partido.local(), partido.visitante(), partido.categoria(), cancha, inicio);
                    if (esValidoConLosYaUbicados(config, candidato, yaUbicados)) {
                        return candidato;
                    }
                }
            }
        }
        return null;
    }

    /** Le pregunta al validador, pasándole solo los partidos que pueden chocar: misma cancha o algún equipo en común. */
    private <E, C> boolean esValidoConLosYaUbicados(ConfiguracionTorneo<C> config, PartidoProgramado<E, C> candidato,
            List<PartidoProgramado<E, C>> yaUbicados) {
        List<PartidoProgramado<E, C>> relevantes = new ArrayList<>();
        for (PartidoProgramado<E, C> p : yaUbicados) {
            boolean mismaCancha = p.cancha().equals(candidato.cancha());
            boolean equipoEnComun = p.juega(candidato.local()) || p.juega(candidato.visitante());
            if (mismaCancha || equipoEnComun) {
                relevantes.add(p);
            }
        }
        relevantes.add(candidato);
        return validador.validar(config, relevantes).esValido();
    }

    // ------------------------------------------------------------------ chequeo por conteo (D35)

    /**
     * Condiciones necesarias: si alguna falla, seguro que no hay solución. Devuelve un detalle por cada problema.
     * 1. Cada equipo tiene que poder jugar todos sus partidos en los días del torneo.
     * 2. Las horas de cancha necesarias no pueden superar las disponibles.
     */
    private static <E, C> List<String> chequeoPorConteo(ConfiguracionTorneo<C> config, List<PartidoAProgramar<E>> partidos) {
        List<String> detalles = new ArrayList<>();

        // 1. Partidos por equipo. Se informa el primer equipo que no llega de cada zona.
        Map<E, List<PartidoAProgramar<E>>> partidosPorEquipo = new LinkedHashMap<>();
        for (PartidoAProgramar<E> p : partidos) {
            partidosPorEquipo.computeIfAbsent(p.local(), e -> new ArrayList<>()).add(p);
            partidosPorEquipo.computeIfAbsent(p.visitante(), e -> new ArrayList<>()).add(p);
        }
        Set<String> zonasInformadas = new LinkedHashSet<>();
        for (Map.Entry<E, List<PartidoAProgramar<E>>> entrada : partidosPorEquipo.entrySet()) {
            PartidoAProgramar<E> alguno = entrada.getValue().get(0);
            int necesita = entrada.getValue().size();
            int puede = maximoDePartidosDeUnEquipo(config, alguno.categoria());
            if (necesita > puede && zonasInformadas.add(alguno.zona())) {
                detalles.add("Zona " + alguno.zona() + ": " + entrada.getKey() + " necesita " + necesita
                        + " partidos y puede jugar como máximo " + puede + " en los días del torneo");
            }
        }

        // 2. Horas de cancha.
        long necesarios = partidos.stream().mapToLong(p -> config.bloqueMin(p.categoria())).sum();
        long disponibles = config.franjas().stream()
                .mapToLong(f -> Duration.between(f.apertura(), f.cierre()).toMinutes())
                .sum() * config.canchasHabilitadas().size();
        if (necesarios > disponibles) {
            detalles.add("Se necesitan " + necesarios + " minutos de cancha (" + necesarios / 60 + " h) y hay "
                    + disponibles + " disponibles (" + disponibles / 60 + " h)");
        }
        return detalles;
    }

    /**
     * Cota de partidos que puede jugar un equipo de esta categoría en todo el torneo.
     * En una franja de L minutos, con bloque B y descanso D, entran como máximo (L + D) / (B + D) partidos
     * (cada partido ocupa B y, salvo el último, necesita D de descanso después). Por día, nunca más que el tope.
     */
    private static <C> int maximoDePartidosDeUnEquipo(ConfiguracionTorneo<C> config, ParametrosCategoria categoria) {
        int bloque = config.bloqueMin(categoria);
        int descanso = config.descansoMinimoMin();
        Map<LocalDate, Integer> porDia = new LinkedHashMap<>();
        for (Franja f : config.franjas()) {
            long largo = Duration.between(f.apertura(), f.cierre()).toMinutes();
            int entran = (int) ((largo + descanso) / (bloque + descanso));
            porDia.merge(f.fecha(), entran, Integer::sum);
        }
        return porDia.values().stream().mapToInt(n -> Math.min(n, categoria.maxPartidosDia())).sum();
    }
}

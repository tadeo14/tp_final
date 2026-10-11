package ar.edu.utn.fixture.dominio;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Único lugar donde se verifican las restricciones obligatorias R1–R8 (regla 3 de CLAUDE.md).
 * Lo usan el Programador antes de devolver un fixture y el servicio antes de publicar.
 *
 * <p>No corta en el primer error: revisa todas las reglas y devuelve cada violación con los partidos
 * involucrados, para que el organizador vea todo lo que hay que corregir.
 */
public class ValidadorRestricciones {

    public <E, C> ResultadoValidacion<E, C> validar(ConfiguracionTorneo<C> config, List<PartidoProgramado<E, C>> partidos) {
        List<Violacion<E, C>> violaciones = new ArrayList<>();

        // R8: un partido sin cancha u horario no se puede revisar con las demás reglas.
        List<PartidoProgramado<E, C>> programados = new ArrayList<>();
        for (PartidoProgramado<E, C> p : partidos) {
            if (p.estaProgramado()) {
                programados.add(p);
            } else {
                violaciones.add(new Violacion<>(Restriccion.R8, "Partido sin programar: " + p.describir(), List.of(p)));
            }
        }

        for (PartidoProgramado<E, C> p : programados) {
            revisarCanchaHabilitada(config, p, violaciones);   // R5
            revisarFranjaYTurno(config, p, violaciones);       // R4 y R7
        }
        revisarCanchas(config, programados, violaciones);      // R1
        revisarEquipos(config, programados, violaciones);      // R2, R3 y R6

        return new ResultadoValidacion<>(violaciones);
    }

    /** R5: la cancha tiene que ser una de las habilitadas del torneo. */
    private <E, C> void revisarCanchaHabilitada(ConfiguracionTorneo<C> config, PartidoProgramado<E, C> p,
            List<Violacion<E, C>> violaciones) {
        if (!config.canchasHabilitadas().contains(p.cancha())) {
            violaciones.add(new Violacion<>(Restriccion.R5,
                    "La cancha " + p.cancha() + " no está habilitada en el torneo: " + p.describir(), List.of(p)));
        }
    }

    /**
     * R4: el partido empieza y termina dentro de una misma franja del día.
     * R7: el inicio cae en un turno, contando desde la apertura de la franja (9:00, 9:15, 9:30... si es cada 15).
     * Si el inicio no está en ninguna franja ya falla R4, y R7 no se revisa porque no hay desde dónde contar.
     */
    private <E, C> void revisarFranjaYTurno(ConfiguracionTorneo<C> config, PartidoProgramado<E, C> p,
            List<Violacion<E, C>> violaciones) {
        LocalDateTime inicio = p.inicio();
        LocalDateTime fin = config.fin(inicio, p.categoria());

        Franja franjaDelInicio = null;
        boolean entraEnUnaFranja = false;
        for (Franja f : config.franjas()) {
            boolean empiezaAdentro = !inicio.isBefore(f.apertura()) && inicio.isBefore(f.cierre());
            if (empiezaAdentro) {
                franjaDelInicio = f;
                if (!fin.isAfter(f.cierre())) {
                    entraEnUnaFranja = true;
                }
            }
        }

        if (!entraEnUnaFranja) {
            violaciones.add(new Violacion<>(Restriccion.R4,
                    "El partido termina a las " + fin.toLocalTime() + " o empieza fuera de las franjas habilitadas: "
                            + p.describir(), List.of(p)));
        }
        if (franjaDelInicio != null) {
            long minutosDesdeApertura = Duration.between(franjaDelInicio.apertura(), inicio).toMinutes();
            boolean enTurno = inicio.getSecond() == 0 && inicio.getNano() == 0
                    && minutosDesdeApertura % config.turnosCadaMin() == 0;
            if (!enTurno) {
                violaciones.add(new Violacion<>(Restriccion.R7,
                        "El inicio no coincide con un turno de " + config.turnosCadaMin() + " minutos: " + p.describir(),
                        List.of(p)));
            }
        }
    }

    /**
     * R1: en cada cancha, ningún par de partidos se pisa. Se comparan todos los pares (no solo los
     * consecutivos) porque los bloques de distintas categorías tienen distinta duración.
     */
    private <E, C> void revisarCanchas(ConfiguracionTorneo<C> config, List<PartidoProgramado<E, C>> programados,
            List<Violacion<E, C>> violaciones) {
        Map<C, List<PartidoProgramado<E, C>>> porCancha = new LinkedHashMap<>();
        for (PartidoProgramado<E, C> p : programados) {
            porCancha.computeIfAbsent(p.cancha(), c -> new ArrayList<>()).add(p);
        }
        for (Map.Entry<C, List<PartidoProgramado<E, C>>> cancha : porCancha.entrySet()) {
            List<PartidoProgramado<E, C>> partidos = ordenadosPorInicio(cancha.getValue());
            for (int i = 0; i < partidos.size(); i++) {
                for (int j = i + 1; j < partidos.size(); j++) {
                    PartidoProgramado<E, C> a = partidos.get(i);
                    PartidoProgramado<E, C> b = partidos.get(j);
                    if (sePisan(config, a, b)) {
                        violaciones.add(new Violacion<>(Restriccion.R1,
                                "Dos partidos se superponen en la cancha " + cancha.getKey() + ": "
                                        + a.describir() + " y " + b.describir(), List.of(a, b)));
                    }
                }
            }
        }
    }

    /**
     * Para cada equipo:
     * R2: ningún par de sus partidos se pisa.
     * R3: entre un partido y el siguiente hay al menos el descanso mínimo (si ya se pisan, se informa solo R2).
     * R6: por día no juega más partidos que el tope de su categoría.
     */
    private <E, C> void revisarEquipos(ConfiguracionTorneo<C> config, List<PartidoProgramado<E, C>> programados,
            List<Violacion<E, C>> violaciones) {
        Set<E> equipos = new LinkedHashSet<>();
        for (PartidoProgramado<E, C> p : programados) {
            equipos.add(p.local());
            equipos.add(p.visitante());
        }

        for (E equipo : equipos) {
            List<PartidoProgramado<E, C>> partidos = ordenadosPorInicio(
                    programados.stream().filter(p -> p.juega(equipo)).toList());

            // R2
            for (int i = 0; i < partidos.size(); i++) {
                for (int j = i + 1; j < partidos.size(); j++) {
                    PartidoProgramado<E, C> a = partidos.get(i);
                    PartidoProgramado<E, C> b = partidos.get(j);
                    if (sePisan(config, a, b)) {
                        violaciones.add(new Violacion<>(Restriccion.R2,
                                equipo + " tiene dos partidos al mismo tiempo: " + a.describir() + " y " + b.describir(),
                                List.of(a, b)));
                    }
                }
            }

            // R3: inicio_siguiente >= fin_anterior + descanso
            for (int i = 0; i + 1 < partidos.size(); i++) {
                PartidoProgramado<E, C> anterior = partidos.get(i);
                PartidoProgramado<E, C> siguiente = partidos.get(i + 1);
                LocalDateTime finAnterior = config.fin(anterior.inicio(), anterior.categoria());
                LocalDateTime puedeVolverAJugar = finAnterior.plusMinutes(config.descansoMinimoMin());
                if (!sePisan(config, anterior, siguiente) && siguiente.inicio().isBefore(puedeVolverAJugar)) {
                    violaciones.add(new Violacion<>(Restriccion.R3,
                            equipo + " no puede volver a jugar antes de las " + puedeVolverAJugar.toLocalTime()
                                    + " (descanso de " + config.descansoMinimoMin() + " minutos): "
                                    + anterior.describir() + " y " + siguiente.describir(),
                            List.of(anterior, siguiente)));
                }
            }

            // R6
            Map<LocalDate, List<PartidoProgramado<E, C>>> porDia = new LinkedHashMap<>();
            for (PartidoProgramado<E, C> p : partidos) {
                porDia.computeIfAbsent(p.inicio().toLocalDate(), d -> new ArrayList<>()).add(p);
            }
            for (Map.Entry<LocalDate, List<PartidoProgramado<E, C>>> dia : porDia.entrySet()) {
                int tope = dia.getValue().get(0).categoria().maxPartidosDia();
                if (dia.getValue().size() > tope) {
                    violaciones.add(new Violacion<>(Restriccion.R6,
                            equipo + " juega " + dia.getValue().size() + " partidos el " + dia.getKey()
                                    + " y el tope es " + tope, dia.getValue()));
                }
            }
        }
    }

    /** Dos partidos se pisan si uno empieza antes de que termine el otro: [inicio, fin) se intersecan. */
    private static <E, C> boolean sePisan(ConfiguracionTorneo<C> config, PartidoProgramado<E, C> a, PartidoProgramado<E, C> b) {
        LocalDateTime finA = config.fin(a.inicio(), a.categoria());
        LocalDateTime finB = config.fin(b.inicio(), b.categoria());
        return a.inicio().isBefore(finB) && b.inicio().isBefore(finA);
    }

    private static <E, C> List<PartidoProgramado<E, C>> ordenadosPorInicio(List<PartidoProgramado<E, C>> partidos) {
        return partidos.stream().sorted(Comparator.comparing(PartidoProgramado::inicio)).toList();
    }
}

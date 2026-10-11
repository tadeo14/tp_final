package ar.edu.utn.fixture.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * Tests del generador de enfrentamientos (todos contra todos, método del círculo).
 * T1 y T2 son los casos de docs/restricciones.md. Los equipos son DATOS DE PRUEBA.
 */
class GeneradorEnfrentamientosTest {

    private final GeneradorEnfrentamientos generador = new GeneradorEnfrentamientos();

    @Test
    void t1_cuatroEquipos_seisPartidos_cadaEquipoJuegaTres() {
        List<String> equipos = List.of("A", "B", "C", "D");

        List<Enfrentamiento<String>> partidos = generador.todosContraTodos(equipos);

        assertThat(partidos).hasSize(6);
        assertThat(partidosPorEquipo(partidos)).containsOnly(
                Map.entry("A", 3L), Map.entry("B", 3L), Map.entry("C", 3L), Map.entry("D", 3L));
        assertThat(rondas(partidos)).containsExactly(1, 2, 3);
        assertCadaParJuegaUnaSolaVez(equipos, partidos);
        assertNingunEquipoJuegaDosVecesEnLaMismaRonda(partidos);
    }

    @Test
    void t2_cincoEquipos_diezPartidos_cadaEquipoJuegaCuatroYTieneUnaFechaLibre() {
        List<String> equipos = List.of("A", "B", "C", "D", "E");

        List<Enfrentamiento<String>> partidos = generador.todosContraTodos(equipos);

        assertThat(partidos).hasSize(10);
        assertThat(partidosPorEquipo(partidos)).containsOnly(
                Map.entry("A", 4L), Map.entry("B", 4L), Map.entry("C", 4L), Map.entry("D", 4L), Map.entry("E", 4L));
        assertThat(rondas(partidos)).containsExactly(1, 2, 3, 4, 5);
        assertCadaParJuegaUnaSolaVez(equipos, partidos);
        assertNingunEquipoJuegaDosVecesEnLaMismaRonda(partidos);

        // El "libre" no se guarda (D18): un equipo está libre en la ronda en la que no juega.
        for (int ronda = 1; ronda <= 5; ronda++) {
            assertThat(libresEnRonda(equipos, partidos, ronda)).as("libres en la ronda %d", ronda).hasSize(1);
        }
        for (String equipo : equipos) {
            long rondasLibres = rondas(partidos).stream()
                    .filter(r -> libresEnRonda(equipos, partidos, r).contains(equipo))
                    .count();
            assertThat(rondasLibres).as("fechas libres de %s", equipo).isEqualTo(1);
        }
    }

    @Test
    void zonaDeTres_tresPartidosEnTresRondas() {
        // Caso real: U13 femenino tiene una zona de 3 equipos.
        List<Enfrentamiento<String>> partidos = generador.todosContraTodos(List.of("A", "B", "C"));

        assertThat(partidos).hasSize(3);
        assertThat(rondas(partidos)).containsExactly(1, 2, 3);
    }

    @Test
    void elMismoOrdenDeEquiposGeneraSiempreLosMismosPartidos() {
        List<String> equipos = List.of("A", "B", "C", "D", "E", "F");

        assertThat(generador.todosContraTodos(equipos)).isEqualTo(generador.todosContraTodos(equipos));
    }

    @Test
    void casoReal_conLasZonasElegidas_generaLos91Partidos() {
        // Tamaños de zona de docs/caso-real.md, sección 3 ("Con zonas: configuración elegida").
        List<List<Integer>> zonasPorGrupo = List.of(
                List.of(5, 5),       // U9 masc
                List.of(5),          // U9 fem
                List.of(4, 4, 4),    // U11 masc
                List.of(5, 4),       // U11 fem
                List.of(4, 4),       // U13 masc
                List.of(4, 4, 3));   // U13 fem

        int total = 0;
        for (List<Integer> zonas : zonasPorGrupo) {
            for (int tamanio : zonas) {
                total += generador.todosContraTodos(equiposDePrueba(tamanio)).size();
            }
        }

        assertThat(total).isEqualTo(91);
    }

    @Test
    void rechazaZonasConMenosDeDosEquipos() {
        assertThatThrownBy(() -> generador.todosContraTodos(List.of("A")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaEquiposRepetidos() {
        assertThatThrownBy(() -> generador.todosContraTodos(List.of("A", "B", "A")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // --- Auxiliares ---

    private static Map<String, Long> partidosPorEquipo(List<Enfrentamiento<String>> partidos) {
        return partidos.stream()
                .flatMap(p -> java.util.stream.Stream.of(p.local(), p.visitante()))
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
    }

    private static Set<Integer> rondas(List<Enfrentamiento<String>> partidos) {
        return partidos.stream().map(Enfrentamiento::ronda).collect(Collectors.toCollection(java.util.TreeSet::new));
    }

    private static Set<String> libresEnRonda(List<String> equipos, List<Enfrentamiento<String>> partidos, int ronda) {
        Set<String> libres = new HashSet<>(equipos);
        partidos.stream().filter(p -> p.ronda() == ronda).forEach(p -> {
            libres.remove(p.local());
            libres.remove(p.visitante());
        });
        return libres;
    }

    private static void assertCadaParJuegaUnaSolaVez(List<String> equipos, List<Enfrentamiento<String>> partidos) {
        Set<Set<String>> pares = new HashSet<>();
        for (Enfrentamiento<String> p : partidos) {
            assertThat(pares.add(Set.of(p.local(), p.visitante())))
                    .as("%s contra %s aparece más de una vez", p.local(), p.visitante()).isTrue();
        }
        int n = equipos.size();
        assertThat(pares).hasSize(n * (n - 1) / 2);
    }

    private static void assertNingunEquipoJuegaDosVecesEnLaMismaRonda(List<Enfrentamiento<String>> partidos) {
        Map<Integer, List<Enfrentamiento<String>>> porRonda =
                partidos.stream().collect(Collectors.groupingBy(Enfrentamiento::ronda));
        porRonda.forEach((ronda, delaRonda) -> {
            Set<String> vistos = new HashSet<>();
            for (Enfrentamiento<String> p : delaRonda) {
                assertThat(vistos.add(p.local())).as("%s juega dos veces en la ronda %d", p.local(), ronda).isTrue();
                assertThat(vistos.add(p.visitante())).as("%s juega dos veces en la ronda %d", p.visitante(), ronda).isTrue();
            }
        });
    }

    private static List<String> equiposDePrueba(int cantidad) {
        return java.util.stream.IntStream.rangeClosed(1, cantidad).mapToObj(i -> "Equipo " + i).toList();
    }
}

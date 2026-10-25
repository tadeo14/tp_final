package ar.edu.utn.fixture.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

/**
 * Tests del sorteo de zonas. Los equipos son DATOS DE PRUEBA; las cantidades de equipos y zonas
 * son las del caso real (docs/caso-real.md).
 */
class SorteoZonasTest {

    @Test
    void nueveEquiposEnDosZonas_quedanCincoYCuatro() {
        // Caso real: U11 femenino.
        List<List<String>> zonas = new SorteoZonas(new Random(1)).sortear(equipos(9), 2);

        assertThat(zonas).extracting(List::size).containsExactly(5, 4);
    }

    @Test
    void onceEquiposEnTresZonas_quedanCuatroCuatroYTres() {
        // Caso real: U13 femenino.
        List<List<String>> zonas = new SorteoZonas(new Random(1)).sortear(equipos(11), 3);

        assertThat(zonas).extracting(List::size).containsExactly(4, 4, 3);
    }

    @Test
    void doceEquiposEnTresZonas_quedanZonasIguales() {
        // Caso real: U11 masculino.
        List<List<String>> zonas = new SorteoZonas(new Random(1)).sortear(equipos(12), 3);

        assertThat(zonas).extracting(List::size).containsExactly(4, 4, 4);
    }

    @Test
    void cadaEquipoQuedaEnExactamenteUnaZona() {
        List<String> equipos = equipos(11);

        List<List<String>> zonas = new SorteoZonas(new Random(7)).sortear(equipos, 3);

        assertThat(zonas.stream().flatMap(List::stream).toList()).containsExactlyInAnyOrderElementsOf(equipos);
    }

    @Test
    void unaSolaZona_entranTodosLosEquipos() {
        // Caso real: U9 femenino, zona única.
        List<List<String>> zonas = new SorteoZonas(new Random(1)).sortear(equipos(5), 1);

        assertThat(zonas).hasSize(1);
        assertThat(zonas.get(0)).containsExactlyInAnyOrderElementsOf(equipos(5));
    }

    @Test
    void conLaMismaSemillaElSorteoDaLoMismo() {
        List<List<String>> primero = new SorteoZonas(new Random(42)).sortear(equipos(10), 2);
        List<List<String>> segundo = new SorteoZonas(new Random(42)).sortear(equipos(10), 2);

        assertThat(primero).isEqualTo(segundo);
    }

    @Test
    void elSorteoMezclaLosEquipos() {
        List<String> equipos = equipos(12);

        List<List<String>> zonas = new SorteoZonas(new Random(3)).sortear(equipos, 1);

        assertThat(zonas.get(0)).isNotEqualTo(equipos);
    }

    @Test
    void noModificaLaListaRecibida() {
        List<String> equipos = new java.util.ArrayList<>(equipos(8));
        List<String> copia = List.copyOf(equipos);

        new SorteoZonas(new Random(1)).sortear(equipos, 2);

        assertThat(equipos).isEqualTo(copia);
    }

    @Test
    void rechazaCeroZonas() {
        assertThatThrownBy(() -> new SorteoZonas(new Random(1)).sortear(equipos(4), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaZonasConMenosDeDosEquipos() {
        // 5 equipos en 3 zonas dejaría una zona con un solo equipo, que no tendría contra quién jugar.
        assertThatThrownBy(() -> new SorteoZonas(new Random(1)).sortear(equipos(5), 3))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static List<String> equipos(int cantidad) {
        return IntStream.rangeClosed(1, cantidad).mapToObj(i -> "Equipo " + i).toList();
    }
}

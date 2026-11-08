package ar.edu.utn.fixture.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ar.edu.utn.fixture.dominio.PropuestaZonas.Propuesta;
import ar.edu.utn.fixture.dominio.PropuestaZonas.Zona;

/**
 * Tests de PropuestaZonas (D69): cuántas zonas y de qué tamaño, con el máximo por zona como parámetro.
 */
class PropuestaZonasTest {

    private static List<Integer> tamanios(Propuesta p) {
        return p.zonas().stream().map(Zona::equipos).toList();
    }

    @Test
    void casoReal_conMaximo5_proponeLas13ZonasDelCasoReal() {
        // docs/caso-real.md, sección 3 ("Con zonas: configuración elegida").
        Map<String, Integer> equipos = new LinkedHashMap<>();
        equipos.put("U9 Masculino", 10);
        equipos.put("U9 Femenino", 5);
        equipos.put("U11 Masculino", 12);
        equipos.put("U11 Femenino", 9);
        equipos.put("U13 Masculino", 8);
        equipos.put("U13 Femenino", 11);
        PropuestaZonas propuesta = new PropuestaZonas(5);

        Map<String, List<Integer>> resultado = new LinkedHashMap<>();
        equipos.forEach((grupo, n) -> resultado.put(grupo, tamanios(propuesta.proponer(n))));

        assertThat(resultado).containsExactly(
                Map.entry("U9 Masculino", List.of(5, 5)),
                Map.entry("U9 Femenino", List.of(5)),
                Map.entry("U11 Masculino", List.of(4, 4, 4)),
                Map.entry("U11 Femenino", List.of(5, 4)),
                Map.entry("U13 Masculino", List.of(4, 4)),
                Map.entry("U13 Femenino", List.of(4, 4, 3)));
        assertThat(resultado.values().stream().mapToInt(List::size).sum()).isEqualTo(13);
        assertThat(resultado.values().stream().flatMap(List::stream).mapToInt(Integer::intValue).sum()).isEqualTo(55);
    }

    @Test
    void repartePareja_lasPrimerasZonasQuedanConUnoMas() {
        Propuesta p = new PropuestaZonas(5).proponer(11);

        assertThat(p.zonas()).containsExactly(new Zona("Zona A", 4), new Zona("Zona B", 4), new Zona("Zona C", 3));
        assertThat(p.aviso()).isNull();
    }

    @Test
    void siEntranEnUnaZona_proponeUnaSola() {
        assertThat(tamanios(new PropuestaZonas(5).proponer(2))).containsExactly(2);
        assertThat(tamanios(new PropuestaZonas(5).proponer(5))).containsExactly(5);
        assertThat(tamanios(new PropuestaZonas(5).proponer(6))).containsExactly(3, 3);
    }

    @Test
    void conUnSoloEquipo_noProponeZonaYAvisaPorQue() {
        Propuesta p = new PropuestaZonas(5).proponer(1);

        assertThat(p.zonas()).isEmpty();
        assertThat(p.aviso()).isEqualTo("Hay un solo equipo: una zona necesita al menos 2 equipos, así que no se propone ninguna");
    }

    @Test
    void sinEquipos_noProponeNadaNiAvisa() {
        Propuesta p = new PropuestaZonas(5).proponer(0);

        assertThat(p.zonas()).isEmpty();
        assertThat(p.aviso()).isNull();
    }

    @Test
    void conMaximo3_ningunaZonaQuedaConMenosDe2() {
        PropuestaZonas propuesta = new PropuestaZonas(3);
        for (int n = 2; n <= 60; n++) {
            List<Integer> t = tamanios(propuesta.proponer(n));
            assertThat(t).as("n = " + n).allMatch(x -> x >= 2 && x <= 3);
            assertThat(t.stream().mapToInt(Integer::intValue).sum()).as("n = " + n).isEqualTo(n);
        }
    }

    @Test
    void elMaximoTieneQueSerAlMenos3() {
        assertThatThrownBy(() -> new PropuestaZonas(2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El máximo de equipos por zona tiene que ser al menos 3: con 2 podría quedar una zona de un solo equipo");
    }

    @Test
    void nombresDeLasZonas() {
        assertThat(PropuestaZonas.nombre(0)).isEqualTo("Zona A");
        assertThat(PropuestaZonas.nombre(25)).isEqualTo("Zona Z");
        assertThat(PropuestaZonas.nombre(26)).isEqualTo("Zona AA");
    }
}

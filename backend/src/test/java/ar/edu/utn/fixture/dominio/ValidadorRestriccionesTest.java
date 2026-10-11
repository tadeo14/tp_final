package ar.edu.utn.fixture.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests del ValidadorRestricciones: T4 a T9 de docs/restricciones.md y, para cada regla R1–R8,
 * al menos un caso que la cumple y otro que la viola.
 *
 * Parámetros del caso real (turnos cada 15, recambio 10, descanso 90, sábado 9 a 20).
 * Los equipos y canchas son DATOS DE PRUEBA.
 */
class ValidadorRestriccionesTest {

    private static final LocalDate SABADO = LocalDate.of(2030, 1, 5);
    private static final LocalDate DOMINGO = SABADO.plusDays(1);

    /** U9: bloque de 60 minutos. */
    private static final ParametrosCategoria U9 = new ParametrosCategoria(28, 4, 10, 3);
    /** U13: bloque de 75 minutos. */
    private static final ParametrosCategoria U13 = new ParametrosCategoria(40, 4, 10, 3);

    private final ConfiguracionTorneo<String> config = new ConfiguracionTorneo<>(15, 10, 90,
            List.of(new Franja(SABADO, LocalTime.of(9, 0), LocalTime.of(20, 0)),
                    new Franja(DOMINGO, LocalTime.of(9, 0), LocalTime.of(16, 0))),
            List.of("Cancha 1", "Cancha 2"));

    private final ValidadorRestricciones validador = new ValidadorRestricciones();

    // ---------------------------------------------------------------- fixture válido

    @Test
    void fixtureSinConflictos_esValido() {
        List<PartidoProgramado<String, String>> partidos = List.of(
                partido("A", "B", U9, "Cancha 1", SABADO, 9, 0),
                partido("C", "D", U9, "Cancha 2", SABADO, 9, 0),
                partido("A", "C", U9, "Cancha 1", SABADO, 11, 30),
                partido("B", "D", U9, "Cancha 2", SABADO, 11, 30));

        ResultadoValidacion<String, String> resultado = validador.validar(config, partidos);

        assertThat(resultado.esValido()).isTrue();
        assertThat(resultado.violaciones()).isEmpty();
    }

    // ---------------------------------------------------------------- R1 cancha sin superposición

    @Test
    void t4_dosPartidosQueSePisanEnLaMismaCancha_violanR1() {
        List<PartidoProgramado<String, String>> partidos = List.of(
                partido("A", "B", U9, "Cancha 1", SABADO, 9, 0),     // 9:00–10:00
                partido("C", "D", U9, "Cancha 1", SABADO, 9, 45));   // 9:45–10:45

        assertThat(reglasVioladas(partidos)).containsExactly(Restriccion.R1);
    }

    @Test
    void r1_partidoQueEmpiezaJustoCuandoTerminaElAnterior_esValido() {
        List<PartidoProgramado<String, String>> partidos = List.of(
                partido("A", "B", U9, "Cancha 1", SABADO, 9, 0),     // 9:00–10:00
                partido("C", "D", U9, "Cancha 1", SABADO, 10, 0));   // 10:00–11:00

        assertThat(reglasVioladas(partidos)).isEmpty();
    }

    @Test
    void r1_detectaUnPartidoLargoQuePisaAUnoQueNoEsElSiguiente() {
        // U13 9:00–10:15 pisa a 10:00, aunque entre medio haya otro partido que empieza antes.
        List<PartidoProgramado<String, String>> partidos = List.of(
                partido("A", "B", U13, "Cancha 1", SABADO, 9, 0),    // 9:00–10:15
                partido("C", "D", U9, "Cancha 1", SABADO, 9, 0),     // 9:00–10:00
                partido("E", "F", U9, "Cancha 1", SABADO, 10, 0));   // 10:00–11:00

        ResultadoValidacion<String, String> resultado = validador.validar(config, partidos);

        assertThat(resultado.violaciones()).extracting(Violacion::restriccion)
                .containsExactly(Restriccion.R1, Restriccion.R1);
    }

    // ---------------------------------------------------------------- R2 equipo sin superposición

    @Test
    void t5_unEquipoEnDosPartidosAlMismoTiempo_violaR2() {
        List<PartidoProgramado<String, String>> partidos = List.of(
                partido("A", "B", U9, "Cancha 1", SABADO, 9, 0),
                partido("A", "C", U9, "Cancha 2", SABADO, 9, 30));

        assertThat(reglasVioladas(partidos)).containsExactly(Restriccion.R2);
    }

    @Test
    void r2_mismoEquipoEnCanchasDistintasSinPisarse_noViolaR2() {
        List<PartidoProgramado<String, String>> partidos = List.of(
                partido("A", "B", U9, "Cancha 1", SABADO, 9, 0),
                partido("A", "C", U9, "Cancha 2", SABADO, 11, 30));

        assertThat(reglasVioladas(partidos)).isEmpty();
    }

    // ---------------------------------------------------------------- R3 descanso mínimo

    @Test
    void t6_descansoExactamenteEnElMinimo_esValido() {
        // Ejemplo de restricciones.md: U9 9:00–10:00 + 90 de descanso → puede volver a jugar a las 11:30.
        List<PartidoProgramado<String, String>> partidos = List.of(
                partido("A", "B", U9, "Cancha 1", SABADO, 9, 0),
                partido("A", "C", U9, "Cancha 1", SABADO, 11, 30));

        assertThat(validador.validar(config, partidos).esValido()).isTrue();
    }

    @Test
    void t7_descansoUnTurnoPorDebajoDelMinimo_violaR3() {
        List<PartidoProgramado<String, String>> partidos = List.of(
                partido("A", "B", U9, "Cancha 1", SABADO, 9, 0),
                partido("A", "C", U9, "Cancha 1", SABADO, 11, 15));

        assertThat(reglasVioladas(partidos)).containsExactly(Restriccion.R3);
    }

    @Test
    void r3_partidosDeDiasDistintosNoNecesitanDescansoEntreSi() {
        // El último del sábado termina 20:00 y el primero del domingo empieza 9:00: más de 90 minutos igual.
        List<PartidoProgramado<String, String>> partidos = List.of(
                partido("A", "B", U9, "Cancha 1", SABADO, 19, 0),
                partido("A", "C", U9, "Cancha 1", DOMINGO, 9, 0));

        assertThat(reglasVioladas(partidos)).isEmpty();
    }

    // ---------------------------------------------------------------- R4 dentro de franja

    @Test
    void t8_partidoQueTerminariaDespuesDelCierreDeFranja_violaR4() {
        // U13 19:00 + 75 = 20:15, la franja del sábado cierra a las 20:00.
        List<PartidoProgramado<String, String>> partidos = List.of(
                partido("A", "B", U13, "Cancha 1", SABADO, 19, 0));

        assertThat(reglasVioladas(partidos)).containsExactly(Restriccion.R4);
    }

    @Test
    void r4_partidoQueTerminaJustoAlCierre_esValido() {
        // U13 18:45 + 75 = 20:00.
        List<PartidoProgramado<String, String>> partidos = List.of(
                partido("A", "B", U13, "Cancha 1", SABADO, 18, 45));

        assertThat(reglasVioladas(partidos)).isEmpty();
    }

    @Test
    void r4_partidoAntesDeLaApertura_violaR4() {
        List<PartidoProgramado<String, String>> partidos = List.of(
                partido("A", "B", U9, "Cancha 1", SABADO, 8, 45));

        assertThat(reglasVioladas(partidos)).containsExactly(Restriccion.R4);
    }

    @Test
    void r4_partidoEnUnDiaSinFranjas_violaR4() {
        List<PartidoProgramado<String, String>> partidos = List.of(
                partido("A", "B", U9, "Cancha 1", SABADO.minusDays(1), 10, 0));

        assertThat(reglasVioladas(partidos)).containsExactly(Restriccion.R4);
    }

    // ---------------------------------------------------------------- R5 cancha habilitada

    @Test
    void r5_partidoEnUnaCanchaNoHabilitada_violaR5() {
        List<PartidoProgramado<String, String>> partidos = List.of(
                partido("A", "B", U9, "Cancha de otro torneo", SABADO, 9, 0));

        assertThat(reglasVioladas(partidos)).containsExactly(Restriccion.R5);
    }

    // ---------------------------------------------------------------- R6 tope diario

    @Test
    void t9_equipoConCuatroPartidosEnUnDiaYTopeTres_violaR6() {
        // Separados 150 minutos (bloque 60 + descanso 90) para que solo falle el tope.
        List<PartidoProgramado<String, String>> partidos = List.of(
                partido("A", "B", U9, "Cancha 1", SABADO, 9, 0),
                partido("A", "C", U9, "Cancha 1", SABADO, 11, 30),
                partido("A", "D", U9, "Cancha 1", SABADO, 14, 0),
                partido("A", "E", U9, "Cancha 1", SABADO, 16, 30));

        ResultadoValidacion<String, String> resultado = validador.validar(config, partidos);

        assertThat(resultado.violaciones()).extracting(Violacion::restriccion).containsExactly(Restriccion.R6);
        assertThat(resultado.violaciones().get(0).detalle()).contains("A").contains("4");
    }

    @Test
    void r6_equipoConTresPartidosEnUnDiaYTopeTres_esValido() {
        List<PartidoProgramado<String, String>> partidos = List.of(
                partido("A", "B", U9, "Cancha 1", SABADO, 9, 0),
                partido("A", "C", U9, "Cancha 1", SABADO, 11, 30),
                partido("A", "D", U9, "Cancha 1", SABADO, 14, 0));

        assertThat(reglasVioladas(partidos)).isEmpty();
    }

    // ---------------------------------------------------------------- R7 inicio en turno

    @Test
    void r7_inicioFueraDeTurno_violaR7() {
        List<PartidoProgramado<String, String>> partidos = List.of(
                partido("A", "B", U9, "Cancha 1", SABADO, 9, 5));

        assertThat(reglasVioladas(partidos)).containsExactly(Restriccion.R7);
    }

    @Test
    void r7_inicioEnTurno_esValido() {
        List<PartidoProgramado<String, String>> partidos = List.of(
                partido("A", "B", U9, "Cancha 1", SABADO, 9, 45));

        assertThat(reglasVioladas(partidos)).isEmpty();
    }

    // ---------------------------------------------------------------- R8 completitud

    @Test
    void r8_partidoSinProgramar_violaR8() {
        List<PartidoProgramado<String, String>> partidos = List.of(
                partido("A", "B", U9, "Cancha 1", SABADO, 9, 0),
                new PartidoProgramado<>("C", "D", U9, null, null));

        assertThat(reglasVioladas(partidos)).containsExactly(Restriccion.R8);
    }

    @Test
    void r8_todosLosPartidosProgramados_esValido() {
        List<PartidoProgramado<String, String>> partidos = List.of(
                partido("A", "B", U9, "Cancha 1", SABADO, 9, 0));

        assertThat(validador.validar(config, partidos).esValido()).isTrue();
    }

    // ---------------------------------------------------------------- informe

    @Test
    void informaTodasLasReglasQueFallanYEnQuePartido() {
        List<PartidoProgramado<String, String>> partidos = new ArrayList<>(List.of(
                partido("A", "B", U9, "Cancha 1", SABADO, 9, 0),
                partido("C", "D", U9, "Cancha 1", SABADO, 9, 30),    // R1 con el anterior
                partido("E", "F", U9, "Cancha 9", SABADO, 9, 5)));   // R5 y R7

        ResultadoValidacion<String, String> resultado = validador.validar(config, partidos);

        assertThat(resultado.esValido()).isFalse();
        assertThat(resultado.violaciones()).extracting(Violacion::restriccion)
                .containsExactlyInAnyOrder(Restriccion.R1, Restriccion.R5, Restriccion.R7);
        Violacion<String, String> r1 = resultado.violaciones().stream()
                .filter(v -> v.restriccion() == Restriccion.R1).findFirst().orElseThrow();
        assertThat(r1.partidos()).containsExactly(partidos.get(0), partidos.get(1));
        assertThat(r1.detalle()).contains("Cancha 1");
    }

    // ---------------------------------------------------------------- auxiliares

    private List<Restriccion> reglasVioladas(List<PartidoProgramado<String, String>> partidos) {
        return validador.validar(config, partidos).violaciones().stream().map(Violacion::restriccion).toList();
    }

    private static PartidoProgramado<String, String> partido(String local, String visitante,
            ParametrosCategoria categoria, String cancha, LocalDate dia, int hora, int minuto) {
        return new PartidoProgramado<>(local, visitante, categoria, cancha, LocalDateTime.of(dia, LocalTime.of(hora, minuto)));
    }
}

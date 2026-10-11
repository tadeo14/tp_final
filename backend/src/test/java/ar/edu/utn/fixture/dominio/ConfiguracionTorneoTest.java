package ar.edu.utn.fixture.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Cálculo de duración programada y bloque (docs/restricciones.md, sección 3).
 * Los valores son los del caso real (docs/caso-real.md, "Duración de los partidos").
 */
class ConfiguracionTorneoTest {

    // Caso real: turnos cada 15, recambio 10, descanso 90.
    private final ConfiguracionTorneo<String> config =
            new ConfiguracionTorneo<>(15, 10, 90, List.of(), Set.of());

    private static final ParametrosCategoria U9 = new ParametrosCategoria(28, 4, 10, 3);
    private static final ParametrosCategoria U11 = new ParametrosCategoria(32, 4, 10, 3);
    private static final ParametrosCategoria U13 = new ParametrosCategoria(40, 4, 10, 3);

    @Test
    void duracionProgramada_esNetosMasPausasMasMargenMasRecambio() {
        assertThat(config.duracionProgramadaMin(U9)).isEqualTo(52);
        assertThat(config.duracionProgramadaMin(U11)).isEqualTo(56);
        assertThat(config.duracionProgramadaMin(U13)).isEqualTo(64);
    }

    @Test
    void bloque_redondeaHaciaArribaAlMultiploDeTurnosCada() {
        assertThat(config.bloqueMin(U9)).isEqualTo(60);
        assertThat(config.bloqueMin(U11)).isEqualTo(60);
        assertThat(config.bloqueMin(U13)).isEqualTo(75);
    }

    @Test
    void bloque_siYaEsMultiploNoSeRedondea() {
        // 30 + 10 + 10 + 10 = 60, ya es múltiplo de 15.
        assertThat(config.bloqueMin(new ParametrosCategoria(30, 10, 10, 3))).isEqualTo(60);
    }

    @Test
    void fin_esInicioMasBloque() {
        // Ejemplo de restricciones.md: un U13 que empieza a las 10:00 termina a las 11:15.
        assertThat(config.fin(LocalDateTime.of(2030, 1, 5, 10, 0), U13))
                .isEqualTo(LocalDateTime.of(2030, 1, 5, 11, 15));
    }
}

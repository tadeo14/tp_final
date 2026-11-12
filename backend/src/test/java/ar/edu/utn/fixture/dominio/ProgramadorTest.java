package ar.edu.utn.fixture.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

/**
 * Tests del Programador: T3, T10 y T11 de docs/restricciones.md, y los dos mensajes de "sin solución" (D34).
 *
 * Los parámetros, categorías, cantidades de equipos y tamaños de zona son los del caso real
 * (docs/caso-real.md). Los nombres de equipos y canchas son DATOS DE PRUEBA.
 */
class ProgramadorTest {

    private static final LocalDate SABADO = LocalDate.of(2030, 1, 5);
    private static final LocalDate DOMINGO = SABADO.plusDays(1);

    private static final ParametrosCategoria U9 = new ParametrosCategoria(28, 4, 10, 3);
    private static final ParametrosCategoria U11 = new ParametrosCategoria(32, 4, 10, 3);
    private static final ParametrosCategoria U13 = new ParametrosCategoria(40, 4, 10, 3);

    private static final List<Franja> FRANJAS_CASO_REAL = List.of(
            new Franja(SABADO, LocalTime.of(9, 0), LocalTime.of(20, 0)),
            new Franja(DOMINGO, LocalTime.of(9, 0), LocalTime.of(16, 0)));

    /** 2 sedes × 4 canchas. */
    private static final List<String> CANCHAS_CASO_REAL = List.of(
            "Sede A - Cancha 1", "Sede A - Cancha 2", "Sede A - Cancha 3", "Sede A - Cancha 4",
            "Sede B - Cancha 1", "Sede B - Cancha 2", "Sede B - Cancha 3", "Sede B - Cancha 4");

    private final ValidadorRestricciones validador = new ValidadorRestricciones();
    private final Programador programador = new Programador(validador);
    private final GeneradorEnfrentamientos generador = new GeneradorEnfrentamientos();

    // ---------------------------------------------------------------- T3

    @Test
    void t3_dosCategoriasCompartiendoUnaCancha_sinSuperposiciones() {
        ConfiguracionTorneo<String> config = new ConfiguracionTorneo<>(15, 10, 90, FRANJAS_CASO_REAL, List.of("Cancha 1"));
        List<PartidoAProgramar<String>> partidos = new ArrayList<>();
        partidos.addAll(zona("U9 Masculino - Zona A", U9, 4));
        partidos.addAll(zona("U13 Masculino - Zona A", U13, 4));

        ResultadoProgramacion<String, String> resultado = programador.programar(config, partidos);

        assertThat(resultado.estado()).isEqualTo(EstadoProgramacion.PROGRAMADO);
        assertThat(resultado.partidos()).hasSize(12).allMatch(p -> p.cancha().equals("Cancha 1"));
        assertThat(validador.validar(config, resultado.partidos()).violaciones())
                .as("ninguna regla violada, en particular R1").isEmpty();
    }

    // ---------------------------------------------------------------- T10

    @Test
    void t10_casoRealConTodosContraTodos_noExisteSolucion() {
        ConfiguracionTorneo<String> config = configCasoReal();
        List<PartidoAProgramar<String>> partidos = new ArrayList<>();
        // Sin zonas: cada categoría y rama es una sola zona con todos sus equipos (caso-real.md, sección 3).
        partidos.addAll(zona("U9 Masculino", U9, 10));
        partidos.addAll(zona("U9 Femenino", U9, 5));
        partidos.addAll(zona("U11 Masculino", U11, 12));
        partidos.addAll(zona("U11 Femenino", U11, 9));
        partidos.addAll(zona("U13 Masculino", U13, 8));
        partidos.addAll(zona("U13 Femenino", U13, 11));
        assertThat(partidos).hasSize(240);

        ResultadoProgramacion<String, String> resultado = programador.programar(config, partidos);

        assertThat(resultado.estado()).isEqualTo(EstadoProgramacion.NO_EXISTE);
        assertThat(resultado.mensaje()).isEqualTo("No existe una programación válida con la configuración actual");
        assertThat(resultado.partidos()).isEmpty();
        // Las 5 zonas que no entran, con el equipo que no llega (caso-real.md: todas menos U9 femenino).
        assertThat(String.join("\n", resultado.detalles()))
                .contains("U9 Masculino", "U11 Masculino", "U11 Femenino", "U13 Masculino", "U13 Femenino")
                .doesNotContain("U9 Femenino")
                .contains("U9 Masculino - Equipo 1 necesita 9 partidos y puede jugar como máximo 6");
        // Y faltan horas de cancha.
        assertThat(resultado.detalles()).anyMatch(d -> d.contains("minutos de cancha"));
    }

    // ---------------------------------------------------------------- T11

    @Test
    void t11_casoRealConZonas_fixtureCompletoYValido() {
        ConfiguracionTorneo<String> config = configCasoReal();
        List<PartidoAProgramar<String>> partidos = partidosCasoRealConZonas();
        assertThat(partidos).hasSize(91);

        ResultadoProgramacion<String, String> resultado = programador.programar(config, partidos);

        assertThat(resultado.estado()).as(resultado.mensaje() + " " + resultado.detalles())
                .isEqualTo(EstadoProgramacion.PROGRAMADO);
        assertThat(resultado.partidos()).hasSize(91).allMatch(PartidoProgramado::estaProgramado);
        assertThat(validador.validar(config, resultado.partidos()).violaciones()).isEmpty();
    }

    @Test
    void elResultadoRespetaElOrdenDeLosPartidosRecibidos() {
        ConfiguracionTorneo<String> config = configCasoReal();
        List<PartidoAProgramar<String>> partidos = partidosCasoRealConZonas();

        ResultadoProgramacion<String, String> resultado = programador.programar(config, partidos);

        for (int i = 0; i < partidos.size(); i++) {
            assertThat(resultado.partidos().get(i).local()).isEqualTo(partidos.get(i).local());
            assertThat(resultado.partidos().get(i).visitante()).isEqualTo(partidos.get(i).visitante());
        }
    }

    @Test
    void elMismoPedidoDaSiempreElMismoFixture() {
        ConfiguracionTorneo<String> config = configCasoReal();
        List<PartidoAProgramar<String>> partidos = partidosCasoRealConZonas();

        assertThat(programador.programar(config, partidos)).isEqualTo(programador.programar(config, partidos));
    }

    // ---------------------------------------------------------------- "No se encontró"

    @Test
    void siElConteoNoLoDemuestraPeroNoEntra_informaQueNoSeEncontro() {
        // Zona de 3: cada partido comparte un equipo con los otros dos, así que van de a uno con 150 min
        // entre inicios (bloque 60 + descanso 90): 9:00, 11:30 y 14:00. El tercero terminaría 15:00 y la
        // franja cierra 14:00. El conteo no lo detecta: cada equipo juega 2 y en 5 horas entran 2 por equipo.
        ConfiguracionTorneo<String> config = new ConfiguracionTorneo<>(15, 10, 90,
                List.of(new Franja(SABADO, LocalTime.of(9, 0), LocalTime.of(14, 0))),
                List.of("Cancha 1", "Cancha 2", "Cancha 3"));

        ResultadoProgramacion<String, String> resultado =
                programador.programar(config, zona("U9 Femenino - Zona C", U9, 3));

        assertThat(resultado.estado()).isEqualTo(EstadoProgramacion.NO_ENCONTRADA);
        assertThat(resultado.mensaje()).isEqualTo("No se encontró una programación válida con la configuración actual");
        assertThat(resultado.partidos()).isEmpty();
        assertThat(resultado.detalles()).singleElement().asString()
                .contains("U9 Femenino - Zona C").contains("ronda 3");
    }

    @Test
    void sinPartidos_noHayNadaQueProgramar() {
        ResultadoProgramacion<String, String> resultado = programador.programar(configCasoReal(), List.of());

        assertThat(resultado.estado()).isEqualTo(EstadoProgramacion.PROGRAMADO);
        assertThat(resultado.partidos()).isEmpty();
    }

    // ---------------------------------------------------------------- auxiliares

    private static ConfiguracionTorneo<String> configCasoReal() {
        return new ConfiguracionTorneo<>(15, 10, 90, FRANJAS_CASO_REAL, CANCHAS_CASO_REAL);
    }

    /** Zonas elegidas en caso-real.md, sección 3 ("Con zonas: configuración elegida"). */
    private List<PartidoAProgramar<String>> partidosCasoRealConZonas() {
        List<PartidoAProgramar<String>> partidos = new ArrayList<>();
        partidos.addAll(zona("U9 Masculino - Zona A", U9, 5));
        partidos.addAll(zona("U9 Masculino - Zona B", U9, 5));
        partidos.addAll(zona("U9 Femenino - Zona A", U9, 5));
        partidos.addAll(zona("U11 Masculino - Zona A", U11, 4));
        partidos.addAll(zona("U11 Masculino - Zona B", U11, 4));
        partidos.addAll(zona("U11 Masculino - Zona C", U11, 4));
        partidos.addAll(zona("U11 Femenino - Zona A", U11, 5));
        partidos.addAll(zona("U11 Femenino - Zona B", U11, 4));
        partidos.addAll(zona("U13 Masculino - Zona A", U13, 4));
        partidos.addAll(zona("U13 Masculino - Zona B", U13, 4));
        partidos.addAll(zona("U13 Femenino - Zona A", U13, 4));
        partidos.addAll(zona("U13 Femenino - Zona B", U13, 4));
        partidos.addAll(zona("U13 Femenino - Zona C", U13, 3));
        return partidos;
    }

    /** Arma una zona de prueba con sus equipos y le genera el todos contra todos. */
    private List<PartidoAProgramar<String>> zona(String nombre, ParametrosCategoria categoria, int cantidadEquipos) {
        List<String> equipos = IntStream.rangeClosed(1, cantidadEquipos).mapToObj(i -> nombre + " - Equipo " + i).toList();
        return generador.todosContraTodos(equipos).stream()
                .map(e -> new PartidoAProgramar<>(e.local(), e.visitante(), e.ronda(), nombre, categoria))
                .toList();
    }
}

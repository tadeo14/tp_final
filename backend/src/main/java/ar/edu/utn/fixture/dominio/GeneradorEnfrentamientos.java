package ar.edu.utn.fixture.dominio;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Genera los partidos de una zona: todos contra todos, por el método del círculo.
 * Ver el dibujo y la explicación en docs/algoritmo-enfrentamientos.md.
 *
 * <p>Con n equipos (n par) hay n - 1 rondas de n / 2 partidos. Si n es impar se agrega un lugar
 * vacío ("libre"): el equipo que en una ronda queda emparejado con el lugar vacío no juega esa ronda.
 * El libre no se devuelve ni se guarda: se deduce de los partidos (D18).
 *
 * <p>No usa azar: el mismo orden de equipos genera siempre los mismos partidos. El azar está
 * en el sorteo de zonas, y su resultado se guarda (D5).
 */
public class GeneradorEnfrentamientos {

    /**
     * @param equipos equipos de la zona, en el orden del sorteo (orden_en_zona)
     * @return los partidos, ordenados por ronda
     */
    public <E> List<Enfrentamiento<E>> todosContraTodos(List<E> equipos) {
        validar(equipos);

        // La "ronda" del círculo: un lugar por equipo, más un lugar vacío (null) si son impares.
        List<E> circulo = new ArrayList<>(equipos);
        if (circulo.size() % 2 != 0) {
            circulo.add(null);
        }
        int lugares = circulo.size();
        int cantidadRondas = lugares - 1;

        List<Enfrentamiento<E>> partidos = new ArrayList<>();
        for (int ronda = 1; ronda <= cantidadRondas; ronda++) {
            // Se enfrentan los lugares opuestos: el primero con el último, el segundo con el anteúltimo...
            for (int i = 0; i < lugares / 2; i++) {
                E uno = circulo.get(i);
                E otro = circulo.get(lugares - 1 - i);
                if (uno == null || otro == null) {
                    continue; // el equipo enfrentado al lugar vacío tiene fecha libre
                }
                // El equipo fijo (lugar 0) alterna de local y visitante para no ser siempre local.
                boolean invertir = i == 0 && ronda % 2 == 0;
                partidos.add(invertir
                        ? new Enfrentamiento<>(otro, uno, ronda)
                        : new Enfrentamiento<>(uno, otro, ronda));
            }
            rotar(circulo);
        }
        return partidos;
    }

    /**
     * El primer lugar queda fijo y los demás giran una posición en el sentido de las agujas del reloj:
     * el último pasa a ser el segundo. Así cada equipo se cruza con todos los demás exactamente una vez.
     */
    private static <E> void rotar(List<E> circulo) {
        E ultimo = circulo.remove(circulo.size() - 1);
        circulo.add(1, ultimo);
    }

    private static <E> void validar(List<E> equipos) {
        if (equipos == null || equipos.size() < 2) {
            throw new IllegalArgumentException("Una zona necesita al menos 2 equipos");
        }
        if (equipos.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("La lista de equipos no puede tener elementos vacíos");
        }
        if (new HashSet<>(equipos).size() != equipos.size()) {
            throw new IllegalArgumentException("Hay equipos repetidos en la zona");
        }
    }
}

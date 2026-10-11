package ar.edu.utn.fixture.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Reparte por sorteo los equipos de una categoría y rama en N zonas.
 *
 * <p>Se mezclan los equipos al azar y se reparten como cartas: el primero a la zona 1, el segundo a la
 * zona 2, ..., y se vuelve a empezar. Así las zonas difieren a lo sumo en un equipo, y si no se pueden
 * hacer iguales las últimas quedan con uno menos (D4). Ejemplo: 11 equipos en 3 zonas = 4 + 4 + 3.
 *
 * <p>El orden dentro de cada zona es el orden_en_zona que se guarda (D5, D16): con él, el
 * GeneradorEnfrentamientos arma siempre los mismos partidos.
 */
public class SorteoZonas {

    private final Random azar;

    /**
     * @param azar fuente de azar. En los tests se pasa una semilla fija para que el resultado sea predecible.
     */
    public SorteoZonas(Random azar) {
        this.azar = azar;
    }

    /**
     * @return una lista por zona, con sus equipos en el orden sorteado
     */
    public <E> List<List<E>> sortear(List<E> equipos, int cantidadZonas) {
        validar(equipos, cantidadZonas);

        List<E> mezclados = new ArrayList<>(equipos); // copia: no se modifica la lista recibida
        Collections.shuffle(mezclados, azar);

        List<List<E>> zonas = new ArrayList<>();
        for (int z = 0; z < cantidadZonas; z++) {
            zonas.add(new ArrayList<>());
        }
        for (int i = 0; i < mezclados.size(); i++) {
            zonas.get(i % cantidadZonas).add(mezclados.get(i));
        }
        return zonas;
    }

    private static <E> void validar(List<E> equipos, int cantidadZonas) {
        if (cantidadZonas < 1) {
            throw new IllegalArgumentException("Tiene que haber al menos 1 zona");
        }
        if (equipos == null || equipos.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("La lista de equipos no puede tener elementos vacíos");
        }
        if (new HashSet<>(equipos).size() != equipos.size()) {
            throw new IllegalArgumentException("Hay equipos repetidos");
        }
        // La zona más chica tiene equipos / zonas (división entera) y necesita al menos 2 para jugar.
        if (equipos.size() / cantidadZonas < 2) {
            throw new IllegalArgumentException("Con " + equipos.size() + " equipos y " + cantidadZonas
                    + " zonas quedaría una zona con menos de 2 equipos");
        }
    }
}

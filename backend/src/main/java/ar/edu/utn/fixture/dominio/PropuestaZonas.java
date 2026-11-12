package ar.edu.utn.fixture.dominio;

import java.util.ArrayList;
import java.util.List;

/**
 * Propone cuántas zonas armar en una categoría y rama, y de qué tamaño, a partir de la cantidad de equipos
 * (D69). Es solo la estructura: qué equipo va a cada zona lo decide después el sorteo ({@link SorteoZonas}).
 *
 * <p>Regla: con n equipos y un máximo m por zona, se arman ceil(n / m) zonas y los equipos se reparten lo más
 * parejo posible; las primeras zonas quedan con uno más, como en el sorteo (D4, D26). Ejemplo: 11 equipos con
 * máximo 5 → 3 zonas de 4, 4 y 3. Una zona necesita al menos 2 equipos: con 1 equipo no se propone zona.
 *
 * <p>El máximo tiene que ser al menos 3: con 2, una cantidad impar de equipos dejaría una zona de 1. Con 3 o más
 * eso nunca pasa, porque ceil(n / m) ≤ n / 2 para todo n ≥ 2.
 */
public class PropuestaZonas {

    /** Mínimo de equipos que necesita una zona para jugar. */
    public static final int MINIMO_POR_ZONA = 2;
    /** Mínimo valor aceptado para el máximo por zona (ver la explicación de la clase). */
    public static final int MINIMO_DEL_MAXIMO = 3;

    /** Una zona propuesta: su nombre y cuántos equipos van a quedar en ella después del sorteo. */
    public record Zona(String nombre, int equipos) {
    }

    /** Propuesta para un grupo (categoría y rama): las zonas, o ninguna y el aviso de por qué. */
    public record Propuesta(List<Zona> zonas, String aviso) {

        public Propuesta {
            zonas = List.copyOf(zonas);
        }
    }

    private final int maximoPorZona;

    /** @param maximoPorZona máximo de equipos por zona: viene de la configuración, no está fijo (regla 1). */
    public PropuestaZonas(int maximoPorZona) {
        if (maximoPorZona < MINIMO_DEL_MAXIMO) {
            throw new IllegalArgumentException("El máximo de equipos por zona tiene que ser al menos " + MINIMO_DEL_MAXIMO
                    + ": con " + maximoPorZona + " podría quedar una zona de un solo equipo");
        }
        this.maximoPorZona = maximoPorZona;
    }

    /** Propuesta para un grupo con {@code cantidadEquipos} equipos. Con 0 equipos no hay zonas ni aviso. */
    public Propuesta proponer(int cantidadEquipos) {
        if (cantidadEquipos < 0) {
            throw new IllegalArgumentException("La cantidad de equipos no puede ser negativa");
        }
        if (cantidadEquipos == 0) {
            return new Propuesta(List.of(), null);
        }
        if (cantidadEquipos < MINIMO_POR_ZONA) {
            return new Propuesta(List.of(), "Hay un solo equipo: una zona necesita al menos " + MINIMO_POR_ZONA
                    + " equipos, así que no se propone ninguna");
        }
        int cantidadZonas = (cantidadEquipos + maximoPorZona - 1) / maximoPorZona; // ceil(n / m)
        int base = cantidadEquipos / cantidadZonas;
        int conUnoMas = cantidadEquipos % cantidadZonas;
        List<Zona> zonas = new ArrayList<>();
        for (int i = 0; i < cantidadZonas; i++) {
            zonas.add(new Zona(nombre(i), i < conUnoMas ? base + 1 : base));
        }
        return new Propuesta(zonas, null);
    }

    /** "Zona A", "Zona B"… y, si hicieran falta más de 26, "Zona AA", "Zona AB"… */
    static String nombre(int indice) {
        StringBuilder letras = new StringBuilder();
        int i = indice;
        do {
            letras.insert(0, (char) ('A' + i % 26));
            i = i / 26 - 1;
        } while (i >= 0);
        return "Zona " + letras;
    }
}

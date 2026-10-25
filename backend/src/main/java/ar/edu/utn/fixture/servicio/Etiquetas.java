package ar.edu.utn.fixture.servicio;

import ar.edu.utn.fixture.dto.Respuestas.EquipoResumen;
import ar.edu.utn.fixture.entidad.Cancha;
import ar.edu.utn.fixture.entidad.Equipo;
import ar.edu.utn.fixture.entidad.Zona;

/**
 * Textos que se muestran al usuario, armados siempre igual.
 */
final class Etiquetas {

    private Etiquetas() {
    }

    /** Ej.: "U13 Femenino - Zona A". */
    static String zona(Zona z) {
        return z.getCategoria().getNombre() + " " + z.getRama().getNombre() + " - " + z.getNombre();
    }

    /** Ej.: "Atlético María Juana - Cancha 1". */
    static String cancha(Cancha c) {
        return c.getSede().getNombre() + " - " + c.getNombre();
    }

    static EquipoResumen resumen(Equipo e) {
        return new EquipoResumen(e.getId(), e.getNombre(), e.getClub().getNombre(), e.getCategoria().getNombre(),
                e.getRama().getNombre(), e.getZona() == null ? null : zona(e.getZona()));
    }
}

package ar.edu.utn.fixture.servicio;

import java.util.List;

import ar.edu.utn.fixture.dominio.ConfiguracionTorneo;
import ar.edu.utn.fixture.dominio.Franja;
import ar.edu.utn.fixture.dominio.ParametrosCategoria;
import ar.edu.utn.fixture.dominio.PartidoProgramado;
import ar.edu.utn.fixture.entidad.Cancha;
import ar.edu.utn.fixture.entidad.Categoria;
import ar.edu.utn.fixture.entidad.Equipo;
import ar.edu.utn.fixture.entidad.FranjaHoraria;
import ar.edu.utn.fixture.entidad.Partido;
import ar.edu.utn.fixture.entidad.Torneo;

/**
 * Traduce las entidades JPA a los tipos del dominio. Así las clases del dominio no conocen JPA (regla 4, D28).
 */
final class AdaptadorDominio {

    /** Cómo ve el dominio a un equipo: el id lo identifica y el nombre aparece en los mensajes. */
    record EquipoRef(Long id, String nombre) {
        @Override
        public String toString() {
            return nombre;
        }
    }

    /** Cómo ve el dominio a una cancha. */
    record CanchaRef(Long id, String nombre) {
        @Override
        public String toString() {
            return nombre;
        }
    }

    private AdaptadorDominio() {
    }

    static EquipoRef equipo(Equipo e) {
        return new EquipoRef(e.getId(), e.getNombre());
    }

    static CanchaRef cancha(Cancha c) {
        return new CanchaRef(c.getId(), Etiquetas.cancha(c));
    }

    static ParametrosCategoria parametros(Categoria c) {
        return new ParametrosCategoria(c.getMinutosNetos(), c.getPausasMin(), c.getMargenDetencionesMin(), c.getMaxPartidosDia());
    }

    /** Configuración con las franjas y las canchas habilitadas del torneo, en el orden de la base (D37). */
    static ConfiguracionTorneo<CanchaRef> configuracion(Torneo t, List<FranjaHoraria> franjas, List<Cancha> canchasHabilitadas) {
        return new ConfiguracionTorneo<>(t.getTurnosCadaMin(), t.getRecambioMin(), t.getDescansoMinimoMin(),
                franjas.stream().map(f -> new Franja(f.getFecha(), f.getDesde(), f.getHasta())).toList(),
                canchasHabilitadas.stream().map(AdaptadorDominio::cancha).toList());
    }

    /** Solo para calcular bloques y fines: no hace falta saber franjas ni canchas. */
    static ConfiguracionTorneo<CanchaRef> parametrosDeTiempo(Torneo t) {
        return configuracion(t, List.of(), List.of());
    }

    static PartidoProgramado<EquipoRef, CanchaRef> programado(Partido p) {
        return new PartidoProgramado<>(equipo(p.getEquipoLocal()), equipo(p.getEquipoVisitante()),
                parametros(p.getZona().getCategoria()),
                p.getCancha() == null ? null : cancha(p.getCancha()), p.getInicio());
    }
}

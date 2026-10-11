package ar.edu.utn.fixture.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Categoría (U9, U11, U13...) con sus parámetros de duración y tope diario.
 * La duración programada y el bloque se calculan a partir de estos datos (D8): no se guardan.
 */
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "minutos_netos", nullable = false)
    private Integer minutosNetos;

    @Column(name = "cantidad_cuartos", nullable = false)
    private Integer cantidadCuartos;

    @Column(name = "pausas_min", nullable = false)
    private Integer pausasMin;

    @Column(name = "margen_detenciones_min", nullable = false)
    private Integer margenDetencionesMin;

    @Column(name = "max_partidos_dia", nullable = false)
    private Integer maxPartidosDia;

    public Long getId() {
        return id;
    }

    public Torneo getTorneo() {
        return torneo;
    }

    public void setTorneo(Torneo torneo) {
        this.torneo = torneo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getMinutosNetos() {
        return minutosNetos;
    }

    public void setMinutosNetos(Integer minutosNetos) {
        this.minutosNetos = minutosNetos;
    }

    public Integer getCantidadCuartos() {
        return cantidadCuartos;
    }

    public void setCantidadCuartos(Integer cantidadCuartos) {
        this.cantidadCuartos = cantidadCuartos;
    }

    public Integer getPausasMin() {
        return pausasMin;
    }

    public void setPausasMin(Integer pausasMin) {
        this.pausasMin = pausasMin;
    }

    public Integer getMargenDetencionesMin() {
        return margenDetencionesMin;
    }

    public void setMargenDetencionesMin(Integer margenDetencionesMin) {
        this.margenDetencionesMin = margenDetencionesMin;
    }

    public Integer getMaxPartidosDia() {
        return maxPartidosDia;
    }

    public void setMaxPartidosDia(Integer maxPartidosDia) {
        this.maxPartidosDia = maxPartidosDia;
    }
}

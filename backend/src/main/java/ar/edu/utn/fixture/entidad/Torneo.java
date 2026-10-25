package ar.edu.utn.fixture.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Torneo con sus parámetros generales. Los días salen de las franjas horarias.
 */
@Entity
@Table(name = "torneo")
public class Torneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizador_id", nullable = false)
    private Organizador organizador;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "turnos_cada_min", nullable = false)
    private Integer turnosCadaMin;

    @Column(name = "recambio_min", nullable = false)
    private Integer recambioMin;

    @Column(name = "descanso_minimo_min", nullable = false)
    private Integer descansoMinimoMin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoTorneo estado = EstadoTorneo.BORRADOR;

    public Long getId() {
        return id;
    }

    public Organizador getOrganizador() {
        return organizador;
    }

    public void setOrganizador(Organizador organizador) {
        this.organizador = organizador;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getTurnosCadaMin() {
        return turnosCadaMin;
    }

    public void setTurnosCadaMin(Integer turnosCadaMin) {
        this.turnosCadaMin = turnosCadaMin;
    }

    public Integer getRecambioMin() {
        return recambioMin;
    }

    public void setRecambioMin(Integer recambioMin) {
        this.recambioMin = recambioMin;
    }

    public Integer getDescansoMinimoMin() {
        return descansoMinimoMin;
    }

    public void setDescansoMinimoMin(Integer descansoMinimoMin) {
        this.descansoMinimoMin = descansoMinimoMin;
    }

    public EstadoTorneo getEstado() {
        return estado;
    }

    public void setEstado(EstadoTorneo estado) {
        this.estado = estado;
    }
}

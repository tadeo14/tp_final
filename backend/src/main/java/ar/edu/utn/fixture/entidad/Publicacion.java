package ar.edu.utn.fixture.entidad;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

/**
 * Una versión publicada del fixture de un torneo: 1, 2, 3… (D46). La última es la que ve el público.
 * Es inmutable: Hibernate nunca le hace UPDATE. El motivo es obligatorio desde la versión 2.
 */
@Entity
@Immutable
@Table(name = "publicacion")
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;

    @Column(name = "numero", nullable = false)
    private Integer numero;

    @Column(name = "publicada_en", nullable = false)
    private Instant publicadaEn;

    @Column(name = "motivo", length = 500)
    private String motivo;

    protected Publicacion() {
        // para JPA
    }

    public Publicacion(Torneo torneo, int numero, Instant publicadaEn, String motivo) {
        this.torneo = torneo;
        this.numero = numero;
        this.publicadaEn = publicadaEn;
        this.motivo = motivo;
    }

    public Long getId() {
        return id;
    }

    public Torneo getTorneo() {
        return torneo;
    }

    public Integer getNumero() {
        return numero;
    }

    public Instant getPublicadaEn() {
        return publicadaEn;
    }

    public String getMotivo() {
        return motivo;
    }
}

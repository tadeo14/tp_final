package ar.edu.utn.fixture.entidad;

import java.time.LocalDateTime;

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
 * Foto de un partido tal como se publicó (D46). Guarda textos, no referencias: si después cambian los equipos,
 * las canchas o la configuración, la foto no cambia. Inmutable: no tiene setters y Hibernate nunca le hace UPDATE.
 *
 * <p>Los ids de los equipos no son claves foráneas: solo sirven para filtrar el fixture de un equipo.
 * El fin se guarda porque es el horario que se comunicó (D48).
 */
@Entity
@Immutable
@Table(name = "partido_publicado")
public class PartidoPublicado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publicacion_id", nullable = false)
    private Publicacion publicacion;

    @Column(name = "equipo_local_id", nullable = false)
    private Long equipoLocalId;

    @Column(name = "equipo_visitante_id", nullable = false)
    private Long equipoVisitanteId;

    @Column(name = "equipo_local", nullable = false, length = 150)
    private String equipoLocal;

    @Column(name = "equipo_visitante", nullable = false, length = 150)
    private String equipoVisitante;

    @Column(name = "categoria", nullable = false, length = 50)
    private String categoria;

    @Column(name = "rama", nullable = false, length = 50)
    private String rama;

    @Column(name = "zona", nullable = false, length = 50)
    private String zona;

    @Column(name = "ronda", nullable = false)
    private Integer ronda;

    @Column(name = "sede", nullable = false, length = 100)
    private String sede;

    @Column(name = "cancha", nullable = false, length = 100)
    private String cancha;

    @Column(name = "inicio", nullable = false)
    private LocalDateTime inicio;

    @Column(name = "fin", nullable = false)
    private LocalDateTime fin;

    protected PartidoPublicado() {
        // para JPA
    }

    /** Saca la foto de un partido programado. {@code fin} viene calculado (inicio + bloque) al momento de publicar. */
    public PartidoPublicado(Publicacion publicacion, Partido partido, LocalDateTime fin) {
        Zona z = partido.getZona();
        this.publicacion = publicacion;
        this.equipoLocalId = partido.getEquipoLocal().getId();
        this.equipoVisitanteId = partido.getEquipoVisitante().getId();
        this.equipoLocal = partido.getEquipoLocal().getNombre();
        this.equipoVisitante = partido.getEquipoVisitante().getNombre();
        this.categoria = z.getCategoria().getNombre();
        this.rama = z.getRama().getNombre();
        this.zona = z.getNombre();
        this.ronda = partido.getRonda();
        this.sede = partido.getCancha().getSede().getNombre();
        this.cancha = partido.getCancha().getNombre();
        this.inicio = partido.getInicio();
        this.fin = fin;
    }

    public Long getId() {
        return id;
    }

    public Publicacion getPublicacion() {
        return publicacion;
    }

    public Long getEquipoLocalId() {
        return equipoLocalId;
    }

    public Long getEquipoVisitanteId() {
        return equipoVisitanteId;
    }

    public String getEquipoLocal() {
        return equipoLocal;
    }

    public String getEquipoVisitante() {
        return equipoVisitante;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getRama() {
        return rama;
    }

    public String getZona() {
        return zona;
    }

    public Integer getRonda() {
        return ronda;
    }

    public String getSede() {
        return sede;
    }

    public String getCancha() {
        return cancha;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    /** "U13 Femenino - Zona A", igual que en el resto de la API. */
    public String etiquetaZona() {
        return categoria + " " + rama + " - " + zona;
    }

    public boolean juega(Long equipoId) {
        return equipoLocalId.equals(equipoId) || equipoVisitanteId.equals(equipoId);
    }
}

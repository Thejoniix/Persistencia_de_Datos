package com.compumaib.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_orden_servicio")
@Getter @Setter @NoArgsConstructor
public class OrdenServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "os_id")
    private Integer id;

    @Column(name = "os_fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "os_falla", length = 300)
    private String fallaReportada;

    // RECIBIDA, EN_REPARACION, FINALIZADA, ENTREGADA
    @Column(name = "os_estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "os_costo_mano_obra")
    private Double costoManoObra;

    @Column(name = "os_diagnostico", length = 400)
    private String diagnostico;

    @Column(name = "os_fecha_entrega")
    private LocalDate fechaEntrega;

    // ===== Relaciones =====

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cli_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eq_id", nullable = false)
    private Equipo equipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tec_id")
    private Tecnico tecnico;

    // Detalles de repuestos usados (relación N:N)
    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleOrden> detalles = new ArrayList<>();

    public OrdenServicio(Cliente cliente, Equipo equipo, Tecnico tecnico,
                         String fallaReportada, String estado, Double costoManoObra) {
        this.cliente = cliente;
        this.equipo = equipo;
        this.tecnico = tecnico;
        this.fallaReportada = fallaReportada;
        this.estado = estado;
        this.costoManoObra = costoManoObra;
        this.fechaIngreso = LocalDate.now();
    }

    /** Helper para agregar un repuesto a la orden */
    public void agregarRepuesto(Repuesto repuesto, int cantidad, double precioAplicado) {
        DetalleOrden detalle = new DetalleOrden(this, repuesto, cantidad, precioAplicado);
        this.detalles.add(detalle);
        repuesto.getDetalles().add(detalle);
    }

    @Override
    public String toString() {
        return "OrdenServicio{id=" + id
                + ", cliente=" + (cliente != null ? cliente.getNombres() : "?")
                + ", equipo=" + (equipo != null ? equipo.getMarca() + " " + equipo.getModelo() : "?")
                + ", estado=" + estado
                + ", costo=$" + costoManoObra + "}";
    }
}

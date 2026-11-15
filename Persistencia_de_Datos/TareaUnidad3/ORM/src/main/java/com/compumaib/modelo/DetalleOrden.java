package com.compumaib.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad intermedia que modela la relación N:N entre OrdenServicio y Repuesto.
 * Guarda la cantidad usada y el precio aplicado en ese momento.
 */
@Entity
@Table(name = "tb_detalle_orden")
@Getter @Setter @NoArgsConstructor
public class DetalleOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "det_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "os_id", nullable = false)
    private OrdenServicio orden;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rep_id", nullable = false)
    private Repuesto repuesto;

    @Column(name = "det_cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "det_precio_aplicado", nullable = false)
    private Double precioAplicado;

    public DetalleOrden(OrdenServicio orden, Repuesto repuesto, Integer cantidad, Double precioAplicado) {
        this.orden = orden;
        this.repuesto = repuesto;
        this.cantidad = cantidad;
        this.precioAplicado = precioAplicado;
    }

    @Override
    public String toString() {
        return "DetalleOrden{id=" + id
                + ", repuesto=" + (repuesto != null ? repuesto.getNombre() : "?")
                + ", cantidad=" + cantidad
                + ", precio=$" + precioAplicado + "}";
    }
}

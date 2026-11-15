package com.compumaib.modelo;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_repuesto")
@Getter @Setter @NoArgsConstructor
public class Repuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rep_id")
    private Integer id;

    @Column(name = "rep_codigo", nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "rep_nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "rep_marca", length = 50)
    private String marca;

    @Column(name = "rep_precio_unitario", nullable = false)
    private Double precioUnitario;

    @Column(name = "rep_stock", nullable = false)
    private Integer stock = 0;

    // Relación inversa N:N a través de DetalleOrden
    @OneToMany(mappedBy = "repuesto")
    private List<DetalleOrden> detalles = new ArrayList<>();

    public Repuesto(String codigo, String nombre, String marca, Double precioUnitario, Integer stock) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.marca = marca;
        this.precioUnitario = precioUnitario;
        this.stock = stock;
    }

    @Override
    public String toString() {
        return "Repuesto{id=" + id + ", codigo=" + codigo + ", nombre=" + nombre
                + ", precio=$" + precioUnitario + ", stock=" + stock + "}";
    }
}

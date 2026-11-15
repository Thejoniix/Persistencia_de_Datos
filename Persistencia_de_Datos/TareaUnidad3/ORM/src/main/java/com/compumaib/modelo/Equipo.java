package com.compumaib.modelo;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_equipo")
@Getter @Setter @NoArgsConstructor
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eq_id")
    private Integer id;

    @Column(name = "eq_marca", nullable = false, length = 50)
    private String marca;

    @Column(name = "eq_modelo", nullable = false, length = 80)
    private String modelo;

    @Column(name = "eq_serie", length = 60)
    private String numeroSerie;

    @Column(name = "eq_procesador", length = 80)
    private String procesador;

    @Column(name = "eq_ram", length = 30)
    private String ram;

    @Column(name = "eq_almacenamiento", length = 50)
    private String almacenamiento;

    // Laptop, Desktop, All-in-One, Tablet, etc.
    @Column(name = "eq_tipo", length = 30)
    private String tipo;

    // Dueño del equipo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cli_id", nullable = false)
    private Cliente propietario;

    // Un equipo puede tener varias órdenes a lo largo del tiempo
    @OneToMany(mappedBy = "equipo")
    private List<OrdenServicio> ordenes = new ArrayList<>();

    public Equipo(String marca, String modelo, String numeroSerie, String procesador,
                  String ram, String almacenamiento, String tipo, Cliente propietario) {
        this.marca = marca;
        this.modelo = modelo;
        this.numeroSerie = numeroSerie;
        this.procesador = procesador;
        this.ram = ram;
        this.almacenamiento = almacenamiento;
        this.tipo = tipo;
        this.propietario = propietario;
    }

    @Override
    public String toString() {
        return "Equipo{id=" + id + ", " + marca + " " + modelo + ", serie=" + numeroSerie + "}";
    }
}

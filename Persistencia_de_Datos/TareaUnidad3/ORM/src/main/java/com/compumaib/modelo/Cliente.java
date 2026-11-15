package com.compumaib.modelo;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_cliente")
@Getter @Setter @NoArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cli_id")
    private Integer id;

    @Column(name = "cli_cedula", nullable = false, unique = true, length = 13)
    private String cedula;

    @Column(name = "cli_nombres", nullable = false, length = 120)
    private String nombres;

    @Column(name = "cli_telefono", length = 20)
    private String telefono;

    @Column(name = "cli_correo", length = 100)
    private String correo;

    @Column(name = "cli_direccion", length = 200)
    private String direccion;

    // Un cliente puede tener muchas órdenes
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<OrdenServicio> ordenes = new ArrayList<>();

    // Un cliente puede tener varios equipos registrados
    @OneToMany(mappedBy = "propietario", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Equipo> equipos = new ArrayList<>();

    public Cliente(String cedula, String nombres, String telefono, String correo, String direccion) {
        this.cedula = cedula;
        this.nombres = nombres;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
    }

    @Override
    public String toString() {
        return "Cliente{id=" + id + ", cedula=" + cedula + ", nombres=" + nombres + "}";
    }
}

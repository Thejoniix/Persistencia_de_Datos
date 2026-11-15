package com.compumaib.modelo;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_tecnico")
@Getter @Setter @NoArgsConstructor
public class Tecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tec_id")
    private Integer id;

    @Column(name = "tec_cedula", nullable = false, unique = true, length = 13)
    private String cedula;

    @Column(name = "tec_nombres", nullable = false, length = 120)
    private String nombres;

    @Column(name = "tec_especialidad", length = 80)
    private String especialidad;

    @Column(name = "tec_telefono", length = 20)
    private String telefono;

    @Column(name = "tec_activo", nullable = false)
    private Boolean activo = true;

    // Un técnico atiende muchas órdenes
    @OneToMany(mappedBy = "tecnico")
    private List<OrdenServicio> ordenes = new ArrayList<>();

    public Tecnico(String cedula, String nombres, String especialidad, String telefono) {
        this.cedula = cedula;
        this.nombres = nombres;
        this.especialidad = especialidad;
        this.telefono = telefono;
        this.activo = true;
    }

    @Override
    public String toString() {
        return "Tecnico{id=" + id + ", nombres=" + nombres + ", especialidad=" + especialidad + "}";
    }
}

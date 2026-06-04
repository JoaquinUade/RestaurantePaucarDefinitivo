package com.uade.tpo.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categoria_gasto_variable")
public class CategoriaGastoVariable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long idCategoria;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    public CategoriaGastoVariable() {
    }

    public CategoriaGastoVariable(String nombre) {
        this.nombre = nombre;
    }

    public Long getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Long idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}

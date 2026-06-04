package com.uade.tpo.demo.entity.dto;

public class EmpleadoRequest {
    private String nombre;
    private String apellido;

    public EmpleadoRequest() {
    }

    public EmpleadoRequest(String nombre, String apellido) {
        this.nombre = nombre;
        this.apellido = apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
}

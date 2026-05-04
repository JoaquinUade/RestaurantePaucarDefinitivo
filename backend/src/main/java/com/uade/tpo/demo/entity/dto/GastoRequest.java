package com.uade.tpo.demo.entity.dto;

public class GastoRequest {
    private String nombre;
    private String descripcion;
    private Double monto;
    private Boolean pagar;
    private String observacion;

    public GastoRequest() {
    }

    public GastoRequest(String nombre, String descripcion, Double monto, Boolean pagar, String observacion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.monto = monto;
        this.pagar = pagar;
        this.observacion = observacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public Boolean getPagar() {
        return pagar;
    }

    public void setPagar(Boolean pagar) {
        this.pagar = pagar;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}

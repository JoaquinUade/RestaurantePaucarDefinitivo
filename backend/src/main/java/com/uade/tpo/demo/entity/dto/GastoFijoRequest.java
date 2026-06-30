package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GastoFijoRequest {
    private LocalDate fecha;
    private String detalle;
    private Boolean estado;
    private BigDecimal monto;
    private Boolean esPersonal;
    private String observacion;

    public GastoFijoRequest() {
    }

    public GastoFijoRequest(LocalDate fecha, String detalle, Boolean estado, BigDecimal monto, Boolean esPersonal, String observacion) {
        this.fecha = fecha;
        this.detalle = detalle;
        this.estado = estado;
        this.monto = monto;
        this.esPersonal = esPersonal;
        this.observacion = observacion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public Boolean getEsPersonal() {
        return esPersonal;
    }

    public void setEsPersonal(Boolean esPersonal) {
        this.esPersonal = esPersonal;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}

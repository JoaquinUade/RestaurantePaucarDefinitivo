package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GastoIndividualRequest {
    private LocalDate fecha;
    private String detalle;
    private BigDecimal monto;
    private Long empleadoId;

    public GastoIndividualRequest() {
    }

    public GastoIndividualRequest(LocalDate fecha, String detalle, BigDecimal monto, Long empleadoId) {
        this.fecha = fecha;
        this.detalle = detalle;
        this.monto = monto;
        this.empleadoId = empleadoId;
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

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public Long getEmpleadoId() {
        return empleadoId;
    }

    public void setEmpleadoId(Long empleadoId) {
        this.empleadoId = empleadoId;
    }
}

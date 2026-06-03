package com.uade.tpo.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "gastos_fijos")
public class GastosFijos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_gasto_fijo")
    private Long idGastoFijo;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "detalle", nullable = false)
    private String detalle;

    @Column(name = "estado", nullable = false)
    private Boolean estado;

    @Column(name = "monto", nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Column(name = "es_personal", nullable = false)
    private Boolean esPersonal;

    public GastosFijos() {
    }

    public GastosFijos(LocalDate fecha, String detalle, Boolean estado, BigDecimal monto, Boolean esPersonal) {
        this.fecha = fecha;
        this.detalle = detalle;
        this.estado = estado;
        this.monto = monto;
        this.esPersonal = esPersonal;
    }

    public Long getIdGastoFijo() {
        return idGastoFijo;
    }

    public void setIdGastoFijo(Long idGastoFijo) {
        this.idGastoFijo = idGastoFijo;
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
}

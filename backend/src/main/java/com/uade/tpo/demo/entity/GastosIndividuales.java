package com.uade.tpo.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "gastos_individuales")
public class GastosIndividuales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_gasto_individual")
    private Long idGastoIndividual;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "detalle", nullable = false)
    private String detalle;

    @Column(name = "monto", nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @ManyToOne
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;


    public GastosIndividuales() {
    }

    public GastosIndividuales(LocalDate fecha, String detalle, BigDecimal monto, Empleado empleado) {
        this.fecha = fecha;
        this.detalle = detalle;
        this.monto = monto;
        this.empleado = empleado;
    }

    public Long getIdGastoIndividual() {
        return idGastoIndividual;
    }

    public void setIdGastoIndividual(Long idGastoIndividual) {
        this.idGastoIndividual = idGastoIndividual;
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

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

}

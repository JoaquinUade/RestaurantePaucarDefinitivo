package com.uade.tpo.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

@Entity
@Table(name = "gastos")
public class Gastos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_gasto")
    private Long idGasto;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "monto", nullable = false)
    private Double monto;

    @Column(name = "pagar", nullable = true)
    private Boolean pagar;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "dia", nullable = false)
    private String dia;

    @Column(name = "observacion")
    private String observacion;

    public Gastos() {
    }

    public Gastos(String nombre, String descripcion, Double monto, Boolean pagar, String observacion, LocalDateTime fecha) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.monto = monto;
        this.pagar = pagar;
        this.observacion = observacion;
        this.fecha = fecha;
        this.dia = fecha.getDayOfWeek()
                       .getDisplayName(TextStyle.FULL, 
                       new Locale("es", "ES"));
    }

    public Long getIdGasto() {
        return idGasto;
    }

    public void setIdGasto(Long idGasto) {
        this.idGasto = idGasto;
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

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}

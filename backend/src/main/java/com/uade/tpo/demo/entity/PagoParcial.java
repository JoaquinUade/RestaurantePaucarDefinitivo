package com.uade.tpo.demo.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Comprobante / registro administrativo de un pago selectivo realizado
 * sobre una o varias ventas específicas de una empresa.
 */
@Entity
@Table(name = "pago_parcial")
public class PagoParcial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Column(name = "payer_name")
    private String payerName;

    @Column(name = "cuit")
    private String cuit;

    @Column(name = "factura")
    private String factura;

    @Column(name = "observaciones")
    private String observaciones;

    @Column(name = "monto_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal montoTotal;

    @ManyToMany
    @JoinTable(
            name = "pago_parcial_venta",
            joinColumns = @JoinColumn(name = "pago_parcial_id"),
            inverseJoinColumns = @JoinColumn(name = "id_venta"))
    @JsonIgnore
    private List<Venta> ventas = new ArrayList<>();

    public PagoParcial() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String nombre) {
        this.payerName = nombre;
    }

    public String getCuit() {
        return cuit;
    }

    public void setCuit(String cuit) {
        this.cuit = cuit;
    }

    public String getFactura() {
        return factura;
    }

    public void setFactura(String factura) {
        this.factura = factura;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public List<Venta> getVentas() {
        return ventas;
    }

    public void setVentas(List<Venta> ventas) {
        this.ventas = ventas;
    }
}
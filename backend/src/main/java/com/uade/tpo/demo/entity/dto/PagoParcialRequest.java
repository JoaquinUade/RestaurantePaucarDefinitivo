package com.uade.tpo.demo.entity.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request para crear un PagoParcial (pago selectivo de deudas empresariales).
 */
public class PagoParcialRequest {

    private LocalDateTime fechaPago;
    private String payerName;
    private String cuit;
    private String factura;
    private String observaciones;
    private List<Long> idVentas;

    public PagoParcialRequest() {
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

    public List<Long> getIdVentas() {
        return idVentas;
    }

    public void setIdVentas(List<Long> idVentas) {
        this.idVentas = idVentas;
    }
}
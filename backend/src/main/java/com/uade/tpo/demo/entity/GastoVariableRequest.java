package com.uade.tpo.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GastoVariableRequest {
    private LocalDate fecha;
    private String producto;
    private BigDecimal cantidad;
    private String medida;
    private BigDecimal monto;
    private Boolean cargadoEnStock;
    private Long categoriaId;

    public GastoVariableRequest() {
    }

    public GastoVariableRequest(LocalDate fecha, String producto, BigDecimal cantidad, String medida, BigDecimal monto) {
        this.fecha = fecha;
        this.producto = producto;
        this.cantidad = cantidad;
        this.medida = medida;
        this.monto = monto;
    }

    public Boolean getCargadoEnStock() {
        return cargadoEnStock;
    }

    public void setCargadoEnStock(Boolean cargadoEnStock) {
        this.cargadoEnStock = cargadoEnStock;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public String getMedida() {
        return medida;
    }

    public void setMedida(String medida) {
        this.medida = medida;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
}

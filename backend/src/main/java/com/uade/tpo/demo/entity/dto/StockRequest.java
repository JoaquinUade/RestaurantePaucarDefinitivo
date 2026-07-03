package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;

public class StockRequest {

    private String nombreProducto;
    private BigDecimal cantidad;
    private BigDecimal stockMinimo;

    public StockRequest() {
    }

    public StockRequest(String nombreProducto, BigDecimal cantidad, BigDecimal stockMinimo) {
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.stockMinimo = stockMinimo;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = stockMinimo;
    }
}

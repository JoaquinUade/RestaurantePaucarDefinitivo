package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;

public class StockRequest {

    private Long categoriaId;
    private String nombreProducto;

    private BigDecimal cantidad;
    private String unidadCantidad;

    private BigDecimal stockMinimo;
    private String unidadStockMinimo;


    public StockRequest() {
    }

    public StockRequest(Long categoriaId, String nombreProducto, BigDecimal cantidad,
                       String unidadCantidad, BigDecimal stockMinimo, String unidadStockMinimo) {
        this.categoriaId = categoriaId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.unidadCantidad = unidadCantidad;
        this.stockMinimo = stockMinimo;
        this.unidadStockMinimo = unidadStockMinimo;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
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

    public String getUnidadCantidad() {
        return unidadCantidad;
    }

    public void setUnidadCantidad(String unidadCantidad) {
        this.unidadCantidad = unidadCantidad;
    }

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public String getUnidadStockMinimo() {
        return unidadStockMinimo;
    }

    public void setUnidadStockMinimo(String unidadStockMinimo) {
        this.unidadStockMinimo = unidadStockMinimo;
    }

}

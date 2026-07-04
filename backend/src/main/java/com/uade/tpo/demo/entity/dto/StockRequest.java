package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;

public class StockRequest {

    private Long categoriaId;
    private String nombreProducto;
    private BigDecimal cantidad;
    private BigDecimal stockMinimo;
    private String unidadMedida;

    public StockRequest() {
    }

    public StockRequest(Long categoriaId, String nombreProducto, BigDecimal cantidad, BigDecimal stockMinimo) {
        this.categoriaId = categoriaId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.stockMinimo = stockMinimo;
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

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = stockMinimo;
    }
    
   public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

}

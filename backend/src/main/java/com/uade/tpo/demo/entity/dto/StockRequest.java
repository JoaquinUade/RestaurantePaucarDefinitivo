package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StockRequest {

    private Long categoriaId;
    private String nombreProducto;

    private BigDecimal cantComprada;
    private String unidadCantComprada;

    private BigDecimal cantidad;
    private String unidadCantidad;

    private Long gastoVariableId;
    private BigDecimal stockMinimo;
    private LocalDate fecha;

    public StockRequest() {
    }

    public StockRequest(
            Long categoriaId,
            String nombreProducto,
            BigDecimal cantComprada,
            String unidadCantComprada,
            BigDecimal cantidad,
            String unidadCantidad,
            BigDecimal stockMinimo) {
        this.categoriaId = categoriaId;
        this.nombreProducto = nombreProducto;
        this.cantComprada = cantComprada;
        this.unidadCantComprada = unidadCantComprada;
        this.cantidad = cantidad;
        this.unidadCantidad = unidadCantidad;
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

    public BigDecimal getCantComprada() {
        return cantComprada;
    }

    public void setCantComprada(BigDecimal cantidad) {
        this.cantComprada = cantidad;
    }

    public String getUnidadCantComprada() {
        return unidadCantComprada;
    }

    public void setUnidadCantComprada(String unidadCantComprada) {
        this.unidadCantComprada = unidadCantComprada;
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

    public Long getGastoVariableId() {
        return gastoVariableId;
    }

    public void setGastoVariableId(Long gastoVariableId) {
        this.gastoVariableId = gastoVariableId;
    }

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}

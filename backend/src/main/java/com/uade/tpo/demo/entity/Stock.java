package com.uade.tpo.demo.entity;

import java.time.LocalDate;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "stock")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_stock")
    private Long idStock;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private CategoriaGastoVariable categoriaGastoVariable;

    @OneToOne
    @JoinColumn(name = "id_gasto_variable", nullable = false, unique = true)
    private GastosVariables gastoVariable;

    @Column(name = "nombre_producto", nullable = false)
    private String nombreProducto;

    @Column(name = "cantidad", nullable = false, precision = 19, scale = 4)
    private BigDecimal cantidad;

    @Column(name = "stock_minimo", nullable = false, precision = 19, scale = 4)
    private BigDecimal stockMinimo;

    @Column(name = "unidad_cantidad")
    private String unidadCantidad;

    @Column(name = "unidad_stock_minimo")
    private String unidadStockMinimo;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(nullable = false)
    private Boolean activo = true;

    public Stock() {
    }

    public Stock(CategoriaGastoVariable categoriaGastoVariable, String nombreProducto,
            BigDecimal cantidad, BigDecimal stockMinimo, String unidadCantidad,
            String unidadStockMinimo) {

        this.categoriaGastoVariable = categoriaGastoVariable;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.stockMinimo = stockMinimo;
        this.unidadCantidad = unidadCantidad;
        this.unidadStockMinimo = unidadStockMinimo;
    }

    public Long getIdStock() {
        return idStock;
    }

    public void setIdStock(Long idStock) {
        this.idStock = idStock;
    }

    public CategoriaGastoVariable getCategoriaGastoVariable() {
        return categoriaGastoVariable;
    }

    public void setCategoriaGastoVariable(CategoriaGastoVariable categoriaGastoVariable) {
        this.categoriaGastoVariable = categoriaGastoVariable;
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

    public String getUnidadCantidad() {
        return unidadCantidad;
    }

    public void setUnidadCantidad(String unidadCantidad) {
        this.unidadCantidad = unidadCantidad;
    }

    public String getUnidadStockMinimo() {
        return unidadStockMinimo;
    }

    public void setUnidadStockMinimo(String unidadStockMinimo) {
        this.unidadStockMinimo = unidadStockMinimo;
    }

    public GastosVariables getGastoVariable() {
        return gastoVariable;
    }

    public void setGastoVariable(GastosVariables gastoVariable) {
        this.gastoVariable = gastoVariable;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}

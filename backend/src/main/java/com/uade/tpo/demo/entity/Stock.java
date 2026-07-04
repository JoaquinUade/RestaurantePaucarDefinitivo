package com.uade.tpo.demo.entity;

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
    @JoinColumn(name = "id_categoria", nullable = false, unique = true)
    private CategoriaGastoVariable categoriaGastoVariable;

    @Column(name = "nombre_producto", nullable = false)
    private String nombreProducto;

    @Column(name = "cantidad", nullable = false, precision = 19, scale = 4)
    private BigDecimal cantidad;

    @Column(name = "stock_minimo", nullable = false, precision = 19, scale = 4)
    private BigDecimal stockMinimo;

    @Column(name = "unidad_medida")
    private String unidadMedida;

    public Stock() {
    }

    public Stock(CategoriaGastoVariable categoriaGastoVariable, String nombreProducto, BigDecimal cantidad, BigDecimal stockMinimo) {
        this.categoriaGastoVariable = categoriaGastoVariable;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.stockMinimo = stockMinimo;
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

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }
}

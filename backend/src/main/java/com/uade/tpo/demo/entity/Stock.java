package com.uade.tpo.demo.entity;

import java.time.LocalDate;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
@Table(name = "stock")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_stock")
    private Long idStock;

    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = false)
    private CategoriaGastoVariable categoriaGastoVariable;

    @OneToMany(
            mappedBy = "stock",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    private List<HistorialStock> historiales;

    @Column(name = "nombre_producto", nullable = false)
    private String nombreProducto;

    @Column(name = "cant_comprada", nullable = false, precision = 19, scale = 4)
    private BigDecimal cantComprada;

    @Column(name = "cantidad", nullable = false, precision = 19, scale = 4)
    private BigDecimal cantidad;

    @Column(name = "unidad_cant_comprada")
    private String unidadCantComprada;

    @Column(name = "unidad_cantidad")
    private String unidadCantidad;

    @Column(name = "stock_minimo", nullable = false, precision = 19, scale = 4)
    private BigDecimal stockMinimo;

    @Column(name = "fecha")
    private LocalDate fecha;

    public Stock() {
    }

    public Stock(CategoriaGastoVariable categoriaGastoVariable,
            String nombreProducto,
            BigDecimal cantComprada,
            BigDecimal cantidad,
            BigDecimal stockMinimo,
            String unidadCantComprada,
            String unidadCantidad) {

        this.categoriaGastoVariable = categoriaGastoVariable;
        this.nombreProducto = nombreProducto;
        this.cantComprada = cantComprada;
        this.cantidad = cantidad;
        this.stockMinimo = stockMinimo;
        this.unidadCantComprada = unidadCantComprada;
        this.unidadCantidad = unidadCantidad;
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

    public BigDecimal getCantComprada() {
        return cantComprada;
    }

    public void setCantComprada(BigDecimal cantidad) {
        this.cantComprada = cantidad;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public String getUnidadCantComprada() {
        return unidadCantComprada;
    }

    public void setUnidadCantComprada(String unidadCantidad) {
        this.unidadCantComprada = unidadCantidad;
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

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public List<HistorialStock> getHistoriales() {
        return historiales;
    }

    public void setHistoriales(List<HistorialStock> historiales) {
        this.historiales = historiales;
    }
}

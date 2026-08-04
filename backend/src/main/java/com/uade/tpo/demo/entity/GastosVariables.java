package com.uade.tpo.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "gastos_variables")
public class GastosVariables {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_gasto_variable")
    private Long idGastoVariable;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "producto", nullable = false)
    private String producto;

    @Column(name = "cant_comprada", nullable = false, precision = 19, scale = 4)
    private BigDecimal cantComprada;

    @Column(name = "medida")
    private String medida;

    @Column(name = "monto", nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Column(name = "cargado_en_stock")
    private Boolean cargadoEnStock;
    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = true)
    private CategoriaGastoVariable categoria;
    @ManyToOne
    @JoinColumn(name = "id_stock")
    private Stock stock;

    public GastosVariables() {
    }

    public GastosVariables(LocalDate fecha, String producto, BigDecimal cantComprada, String medida, BigDecimal monto) {
        this.fecha = fecha;
        this.producto = producto;
        this.cantComprada = cantComprada;
        this.medida = medida;
        this.monto = monto;
    }

    public Boolean getCargadoEnStock() {
        return cargadoEnStock;
    }

    public void setCargadoEnStock(Boolean cargadoEnStock) {
        this.cargadoEnStock = cargadoEnStock;
    }

    public Long getIdGastoVariable() {
        return idGastoVariable;
    }

    public void setIdGastoVariable(Long idGastoVariable) {
        this.idGastoVariable = idGastoVariable;
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

    public BigDecimal getCantComprada() {
        return cantComprada;
    }

    public void setCantComprada(BigDecimal cantComprada) {
        this.cantComprada = cantComprada;
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

    public CategoriaGastoVariable getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaGastoVariable categoria) {
        this.categoria = categoria;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }
}

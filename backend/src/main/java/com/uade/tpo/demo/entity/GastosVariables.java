package com.uade.tpo.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import com.uade.tpo.demo.entity.CategoriaGastoVariable;

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

    @Column(name = "cantidad", nullable = false, precision = 19, scale = 4)
    private BigDecimal cantidad;

    @Column(name = "medida")
    private String medida;

    @Column(name = "monto", nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Column(name = "cargado_en_stock")
    private Boolean cargadoEnStock;
    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = true)
    private CategoriaGastoVariable categoria;

    public GastosVariables() {
    }

    public GastosVariables(LocalDate fecha, String producto, BigDecimal cantidad, String medida, BigDecimal monto) {
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

    public CategoriaGastoVariable getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaGastoVariable categoria) {
        this.categoria = categoria;
    }
}

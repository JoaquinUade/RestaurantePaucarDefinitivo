package com.uade.tpo.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.math.BigDecimal;

@Entity
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Long idVenta;

    @Column(name = "factura", nullable = true, precision = 19, scale = 2)
    private BigDecimal Factura;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "dia", nullable = false)
    private String dia;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "monto", nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private TipoDePago estado;

    @Column(name = "observaciones")
    private String observaciones;

    public Venta() {
    }

    public Venta(Cliente cliente, String descripcion, BigDecimal monto, TipoDePago estado, String observaciones, LocalDateTime fecha) {
        this.cliente = cliente;
        this.descripcion = descripcion;
        this.monto = monto;
        this.estado = estado;
        this.observaciones = observaciones;
        this.fecha = fecha;
        this.dia = fecha.getDayOfWeek()
                       .getDisplayName(TextStyle.FULL, 
                       new Locale("es", "ES"));
    }

    public Long getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Long idVenta) {
        this.idVenta = idVenta;
    }

    public BigDecimal getFactura() {
        return Factura;
    }

    public void setFactura(BigDecimal Factura) {
        this.Factura = Factura;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public TipoDePago getEstado() {
        return estado;
    }

    public void setEstado(TipoDePago estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
        public String getDia() {
            return dia;
        }

        public void setDia(String dia) {
            this.dia = dia;
        }
}

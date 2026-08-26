package com.uade.tpo.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "pago_empresa")
public class PagoEmpresa {

    private static final BigDecimal PORCENTAJE_IVA = new BigDecimal("0.21"); // 21% de IVA (Argentina)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_periodicidad", nullable = false)
    private TipoPeriodicidad tipoPeriodicidad;

    @Column(name = "cuit")
    private String cuit;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "numero_pago")
    private Integer numeroPago;

    @Column(name = "monto", nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Column(name = "monto_con_iva", precision = 19, scale = 2)
    private BigDecimal montoConIva;

    @Column(name = "factura")
    private String factura;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPago estado;

    @Column(name = "observacion")
    private String observacion;

    public PagoEmpresa() {
    }

    public PagoEmpresa(Long empresaId, String nombre, TipoPeriodicidad tipoPeriodicidad,
            String cuit, LocalDateTime fecha, Integer numeroPago,
            BigDecimal monto, BigDecimal montoConIva, String factura,
            EstadoPago estado, String observacion) {
        this.empresaId = empresaId;
        this.nombre = nombre;
        this.tipoPeriodicidad = tipoPeriodicidad;
        this.cuit = cuit;
        this.fecha = fecha;
        this.numeroPago = numeroPago;
        this.monto = monto;
        this.montoConIva = montoConIva != null ? montoConIva : calcularMontoConIva(monto);
        this.factura = factura;
        this.estado = estado;
        this.observacion = observacion;
    }

    @PrePersist
    @PreUpdate
    private void calcularMontoConIvaAutomatico() {
        if (this.montoConIva == null && this.monto != null) {
            this.montoConIva = calcularMontoConIva(this.monto);
        }
    }

    private static BigDecimal calcularMontoConIva(BigDecimal monto) {
        if (monto == null) {
            return null;
        }
        return monto.multiply(BigDecimal.ONE.add(PORCENTAJE_IVA))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoPeriodicidad getTipoPeriodicidad() {
        return tipoPeriodicidad;
    }

    public void setTipoPeriodicidad(TipoPeriodicidad tipoPeriodicidad) {
        this.tipoPeriodicidad = tipoPeriodicidad;
    }

    public String getCuit() {
        return cuit;
    }

    public void setCuit(String cuit) {
        this.cuit = cuit;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    /**
     * Obtiene el mes de la fecha
     */
    public Integer getMes() {
        return fecha != null ? fecha.getMonthValue() : null;
    }

    /**
     * Obtiene el año de la fecha
     */
    public Integer getAño() {
        return fecha != null ? fecha.getYear() : null;
    }

    public Integer getNumeroPago() {
        return numeroPago;
    }

    public void setNumeroPago(Integer numeroPago) {
        this.numeroPago = numeroPago;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
        // Recalcular montoConIva si es null cuando se establece el monto
        if (this.montoConIva == null && monto != null) {
            this.montoConIva = calcularMontoConIva(monto);
        }
    }

    public BigDecimal getMontoConIva() {
        return montoConIva;
    }

    public void setMontoConIva(BigDecimal montoConIva) {
        this.montoConIva = montoConIva;
    }

    public String getFactura() {
        return factura;
    }

    public void setFactura(String factura) {
        this.factura = factura;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}

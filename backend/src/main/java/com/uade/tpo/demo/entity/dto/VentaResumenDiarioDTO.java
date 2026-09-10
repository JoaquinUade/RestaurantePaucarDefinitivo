package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class VentaResumenDiarioDTO {

    private LocalDate fecha;
    private String dia;  // Campo nuevo
    private BigDecimal ventaTotal;
    private BigDecimal transferencia;
    private BigDecimal debe;
    private BigDecimal efectivo;
    private BigDecimal mercadoPago;
    private BigDecimal debito;
    private BigDecimal credito;
    private BigDecimal deudaPagada; // Nuevo campo para DEUDA_PAGADA
    private BigDecimal gastosVariables;
    private BigDecimal gastosFijos;
    private BigDecimal gastosIndividuales;

    // Constructores existentes...
   public VentaResumenDiarioDTO(LocalDate fecha) {

    this.fecha = fecha;

    this.ventaTotal = BigDecimal.ZERO;
    this.transferencia = BigDecimal.ZERO;
    this.debe = BigDecimal.ZERO;
    this.efectivo = BigDecimal.ZERO;
    this.mercadoPago = BigDecimal.ZERO;
    this.debito = BigDecimal.ZERO;
    this.credito = BigDecimal.ZERO;
    this.deudaPagada = BigDecimal.ZERO;

    this.gastosVariables = BigDecimal.ZERO;
    this.gastosFijos = BigDecimal.ZERO;
    this.gastosIndividuales = BigDecimal.ZERO;
}

    // GETTERS Y SETTERS PARA TODOS LOS CAMPOS (incluyendo dia)
    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public BigDecimal getVentaTotal() {
        return ventaTotal;
    }

    public void setVentaTotal(BigDecimal ventaTotal) {
        this.ventaTotal = ventaTotal;
    }

    public BigDecimal getTransferencia() {
        return transferencia;
    }

    public void setTransferencia(BigDecimal transferencia) {
        this.transferencia = transferencia;
    }

    public BigDecimal getDebe() {
        return debe;
    }

    public void setDebe(BigDecimal debe) {
        this.debe = debe;
    }

    public BigDecimal getEfectivo() {
        return efectivo;
    }

    public void setEfectivo(BigDecimal efectivo) {
        this.efectivo = efectivo;
    }

    public BigDecimal getMercadoPago() {
        return mercadoPago;
    }

    public void setMercadoPago(BigDecimal mercadoPago) {
        this.mercadoPago = mercadoPago;
    }

    public BigDecimal getDebito() {
        return debito;
    }

    public void setDebito(BigDecimal debito) {
        this.debito = debito;
    }

    public BigDecimal getCredito() {
        return credito;
    }

    public void setCredito(BigDecimal credito) {
        this.credito = credito;
    }

    public BigDecimal getDeudaPagada() {
        return deudaPagada;
    }

    public void setDeudaPagada(BigDecimal deudaPagada) {
        this.deudaPagada = deudaPagada;
    }

    public BigDecimal getGastosVariables() {
        return gastosVariables;
    }

    public void setGastosVariables(BigDecimal gastosVariables) {
        this.gastosVariables = gastosVariables;
    }

    public BigDecimal getGastosFijos() {
        return gastosFijos;
    }

    public void setGastosFijos(BigDecimal gastosFijos) {
        this.gastosFijos = gastosFijos;
    }

    public BigDecimal getGastosIndividuales() {
        return gastosIndividuales;
    }

    public void setGastosIndividuales(BigDecimal gastosIndividuales) {
        this.gastosIndividuales = gastosIndividuales;
    }
}

package com.uade.tpo.demo.entity;

import jakarta.persistence.*;
import com.uade.tpo.demo.entity.TipoPeriodicidad;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCliente;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cliente", nullable = false)
    private TipoCliente tipoCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "periodicidad_pago")
    private TipoPeriodicidad periodicidadPago;

    public Cliente() {
    }

    public Cliente(String nombre, TipoCliente tipoCliente) {
        this.nombre = nombre;
        this.tipoCliente = tipoCliente;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoCliente getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(TipoCliente tipoCliente) {
        this.tipoCliente = tipoCliente;
    }

    public TipoPeriodicidad getPeriodicidadPago() {
        return periodicidadPago;
    }

    public void setPeriodicidadPago(TipoPeriodicidad periodicidadPago) {
        this.periodicidadPago = periodicidadPago;
    }
}

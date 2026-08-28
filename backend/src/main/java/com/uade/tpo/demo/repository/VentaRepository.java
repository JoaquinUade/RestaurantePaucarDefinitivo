package com.uade.tpo.demo.repository;

import java.time.LocalDateTime;
import com.uade.tpo.demo.entity.Venta;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    @Query("""
        SELECT v
        FROM Venta v
        WHERE v.cliente.idCliente = :clienteId
        AND MONTH(v.fecha) = :mes
        AND YEAR(v.fecha) = :anio
        """)
    List<Venta> obtenerVentasPorClienteYMes(
            @Param("clienteId") Long clienteId,
            @Param("mes") int mes,
            @Param("anio") int anio);
/**
     * Devuelve las ventas cuya fecha está en el rango [inicio, fin).
     * Permite filtrar en la base (no en memoria) por día, mes o año.
     */
    @Query("""
        SELECT v
        FROM Venta v
        WHERE v.fecha >= :inicio
        AND v.fecha < :fin
        """)
    List<Venta> findByRangoFecha(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
}

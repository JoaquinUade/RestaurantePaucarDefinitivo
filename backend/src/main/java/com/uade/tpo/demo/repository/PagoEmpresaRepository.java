package com.uade.tpo.demo.repository;

import com.uade.tpo.demo.entity.PagoEmpresa;
import com.uade.tpo.demo.entity.TipoPeriodicidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoEmpresaRepository extends JpaRepository<PagoEmpresa, Long> {

    /**
     * Obtiene todos los pagos de una empresa en un mes específico
     */
    @Query("SELECT p FROM PagoEmpresa p WHERE p.empresaId = :empresaId AND MONTH(p.fecha) = :mes AND YEAR(p.fecha) = :anio")
    List<PagoEmpresa> obtenerPagosPorMesYAño(
            @Param("empresaId") Long empresaId,
            @Param("mes") Integer mes,
            @Param("anio") Integer anio
    );

    /**
     * Cuenta los pagos de una empresa por tipo de periodicidad en un mes específico
     */
    @Query("SELECT COUNT(p) FROM PagoEmpresa p WHERE p.empresaId = :empresaId AND p.tipoPeriodicidad = :tipoPeriodicidad AND MONTH(p.fecha) = :mes AND YEAR(p.fecha) = :anio")
    Long contarPagosPorTipoYMes(
            @Param("empresaId") Long empresaId, 
            @Param("tipoPeriodicidad") TipoPeriodicidad tipoPeriodicidad, 
            @Param("mes") Integer mes, 
            @Param("anio") Integer anio
    );

    /**
     * Obtiene todos los pagos de una empresa por tipo de periodicidad en un mes
     */
    @Query("SELECT p FROM PagoEmpresa p WHERE p.empresaId = :empresaId AND p.tipoPeriodicidad = :tipoPeriodicidad AND MONTH(p.fecha) = :mes AND YEAR(p.fecha) = :anio")
    List<PagoEmpresa> obtenerPagosPorTipoYMes(
            @Param("empresaId") Long empresaId,
            @Param("tipoPeriodicidad") TipoPeriodicidad tipoPeriodicidad,
            @Param("mes") Integer mes,
            @Param("anio") Integer anio
    );

    /**
     * Obtiene todos los pagos de una empresa ordenados por fecha
     */
    @Query("SELECT p FROM PagoEmpresa p WHERE p.empresaId = :empresaId ORDER BY p.fecha DESC")
    List<PagoEmpresa> obtenerTodosPorEmpresa(@Param("empresaId") Long empresaId);
}
package com.uade.tpo.demo.service;

import com.uade.tpo.demo.entity.PagoEmpresa;
import com.uade.tpo.demo.entity.TipoPeriodicidad;

import java.util.List;
import java.util.Optional;

public interface PagoEmpresaService {

    /**
     * Crea un nuevo pago empresarial con validaciones de límites según tipo de periodicidad.
     * 
     * Nota: Los campos mes y año se calculan automáticamente a partir de la fecha.
     * 
     * Reglas de negocio:
     * - MENSUAL: máximo 1 por mes
     * - QUINCENAL: máximo 2 por mes
     * - SEMANAL: máximo 5 por mes
     * - CONSUMOVARIOSDIAS: sin límite
     * 
     * @param pagoEmpresa el pago a crear (fecha es obligatoria)
     * @return el pago creado
     * @throws IllegalArgumentException si se excede el límite permitido
     */
    PagoEmpresa crearPagoEmpresa(PagoEmpresa pagoEmpresa);

    /**
     * Modifica un pago existente (con validaciones)
     */
    PagoEmpresa modificarPagoEmpresa(Long id, PagoEmpresa pagoEmpresa);

    /**
     * Obtiene un pago por su ID
     */
    Optional<PagoEmpresa> obtenerPagoById(Long id);

    /**
     * Obtiene todos los pagos
     */
    List<PagoEmpresa> obtenerTodosPagos();

    /**
     * Obtiene todos los pagos de una empresa
     */
    List<PagoEmpresa> obtenerPagosPorEmpresa(Long empresaId);

    /**
     * Obtiene pagos de una empresa en un mes específico
     */
    List<PagoEmpresa> obtenerPagosPorMes(Long empresaId, Integer mes, Integer año);

    /**
     * Obtiene pagos de una empresa por tipo de periodicidad en un mes
     */
    List<PagoEmpresa> obtenerPagosPorTipoYMes(Long empresaId, TipoPeriodicidad tipoPeriodicidad, Integer mes, Integer año);

    /**
     * Elimina un pago
     */
    void eliminarPago(Long id);

    /**
     * Verifica si una empresa puede crear un nuevo pago del tipo especificado en el mes/año dado
     */
    boolean puedeCrearPago(Long empresaId, TipoPeriodicidad tipoPeriodicidad, Integer mes, Integer año);

    /**
     * Obtiene la cantidad de pagos permitidos para un tipo de periodicidad
     */
    Integer obtenerLimitePorTipo(TipoPeriodicidad tipoPeriodicidad);

    /**
     * Obtiene la cantidad actual de pagos para un tipo en un mes
     */
    Long obtenerCantidadActual(Long empresaId, TipoPeriodicidad tipoPeriodicidad, Integer mes, Integer año);
}

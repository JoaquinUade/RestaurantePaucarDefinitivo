package com.uade.tpo.demo.service;

import com.uade.tpo.demo.entity.PagoEmpresa;
import com.uade.tpo.demo.entity.TipoPeriodicidad;
import com.uade.tpo.demo.repository.PagoEmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PagoEmpresaServiceImpl implements PagoEmpresaService {

    @Autowired
    private PagoEmpresaRepository pagoEmpresaRepository;

    // Límites de pagos por tipo de periodicidad y mes
    private static final Integer LIMITE_MENSUAL = 1;
    private static final Integer LIMITE_QUINCENAL = 2;
    private static final Integer LIMITE_SEMANAL = 5;
    private static final Integer LIMITE_CONSUMO_VARIOS_DIAS = Integer.MAX_VALUE; // Sin límite

    @Override
    public PagoEmpresa crearPagoEmpresa(PagoEmpresa pagoEmpresa) {
        validarPagoEmpresa(pagoEmpresa);
        return pagoEmpresaRepository.save(pagoEmpresa);
    }

    @Override
    public PagoEmpresa modificarPagoEmpresa(Long id, PagoEmpresa pagoEmpresa) {
        return pagoEmpresaRepository.findById(id).map(existing -> {

            // Si se está cambiando el tipo de periodicidad o la fecha, validar nuevamente
            if ((pagoEmpresa.getTipoPeriodicidad() != null && !existing.getTipoPeriodicidad().equals(pagoEmpresa.getTipoPeriodicidad())) ||
                (pagoEmpresa.getFecha() != null && !existing.getFecha().equals(pagoEmpresa.getFecha()))) {
                
                validarPagoEmpresaParaModificacion(pagoEmpresa, id);
            }

            // Actualizar campos
            if (pagoEmpresa.getNombre() != null) {
                existing.setNombre(pagoEmpresa.getNombre());
            }
            if (pagoEmpresa.getTipoPeriodicidad() != null) {
                existing.setTipoPeriodicidad(pagoEmpresa.getTipoPeriodicidad());
            }
            if (pagoEmpresa.getCuit() != null) {
                existing.setCuit(pagoEmpresa.getCuit());
            }
            if (pagoEmpresa.getFecha() != null) {
                existing.setFecha(pagoEmpresa.getFecha());
            }
            if (pagoEmpresa.getNumeroPago() != null) {
                existing.setNumeroPago(pagoEmpresa.getNumeroPago());
            }
            if (pagoEmpresa.getMonto() != null) {
                existing.setMonto(pagoEmpresa.getMonto());
            }
            if (pagoEmpresa.getMontoConIva() != null) {
                existing.setMontoConIva(pagoEmpresa.getMontoConIva());
            }
            if (pagoEmpresa.getFactura() != null) {
                existing.setFactura(pagoEmpresa.getFactura());
            }
            if (pagoEmpresa.getEstado() != null) {
                existing.setEstado(pagoEmpresa.getEstado());
            }
            if (pagoEmpresa.getObservacion() != null) {
                existing.setObservacion(pagoEmpresa.getObservacion());
            }

            return pagoEmpresaRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Pago de empresa no encontrado con id: " + id));
    }

    @Override
    public Optional<PagoEmpresa> obtenerPagoById(Long id) {
        return pagoEmpresaRepository.findById(id);
    }

    @Override
    public List<PagoEmpresa> obtenerTodosPagos() {
        return pagoEmpresaRepository.findAll();
    }

    @Override
    public List<PagoEmpresa> obtenerPagosPorEmpresa(Long empresaId) {
        return pagoEmpresaRepository.obtenerTodosPorEmpresa(empresaId);
    }

    @Override
    public List<PagoEmpresa> obtenerPagosPorMes(Long empresaId, Integer mes, Integer año) {
        return pagoEmpresaRepository.obtenerPagosPorMesYAño(empresaId, mes, año);
    }

    @Override
    public List<PagoEmpresa> obtenerPagosPorTipoYMes(Long empresaId, TipoPeriodicidad tipoPeriodicidad, Integer mes, Integer año) {
        return pagoEmpresaRepository.obtenerPagosPorTipoYMes(empresaId, tipoPeriodicidad, mes, año);
    }

    @Override
    public void eliminarPago(Long id) {
        pagoEmpresaRepository.deleteById(id);
    }

    @Override
    public boolean puedeCrearPago(Long empresaId, TipoPeriodicidad tipoPeriodicidad, Integer mes, Integer año) {
        Long cantidadActual = obtenerCantidadActual(empresaId, tipoPeriodicidad, mes, año);
        Integer limite = obtenerLimitePorTipo(tipoPeriodicidad);
        return cantidadActual < limite;
    }

    @Override
    public Integer obtenerLimitePorTipo(TipoPeriodicidad tipoPeriodicidad) {
        switch (tipoPeriodicidad) {
            case MENSUAL:
                return LIMITE_MENSUAL;
            case QUINCENAL:
                return LIMITE_QUINCENAL;
            case SEMANAL:
                return LIMITE_SEMANAL;
            case CONSUMOVARIOSDIAS:
                return LIMITE_CONSUMO_VARIOS_DIAS;
            default:
                throw new IllegalArgumentException("Tipo de periodicidad desconocido: " + tipoPeriodicidad);
        }
    }

    @Override
    public Long obtenerCantidadActual(Long empresaId, TipoPeriodicidad tipoPeriodicidad, Integer mes, Integer año) {
        Long total = pagoEmpresaRepository.contarPagosPorTipoYMes(empresaId, tipoPeriodicidad, mes, año);
        return total != null ? total : 0L;
    }

    /**
     * Valida las reglas de negocio para crear un nuevo pago
     */
    private void validarPagoEmpresa(PagoEmpresa pagoEmpresa) {
        if (pagoEmpresa.getEmpresaId() == null) {
            throw new IllegalArgumentException("El ID de la empresa es obligatorio");
        }
        if (pagoEmpresa.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (pagoEmpresa.getTipoPeriodicidad() == null) {
            throw new IllegalArgumentException("El tipo de periodicidad es obligatorio");
        }

        // Se extraen mes y año directamente del objeto LocalDateTime de la fecha
        Integer mes = pagoEmpresa.getFecha().getMonthValue();
        Integer año = pagoEmpresa.getFecha().getYear();

        // Validar límites según tipo de periodicidad
        if (!puedeCrearPago(pagoEmpresa.getEmpresaId(), pagoEmpresa.getTipoPeriodicidad(), mes, año)) {

            Long cantidadActual = obtenerCantidadActual(pagoEmpresa.getEmpresaId(),
                    pagoEmpresa.getTipoPeriodicidad(),
                    mes, año);
            Integer limite = obtenerLimitePorTipo(pagoEmpresa.getTipoPeriodicidad());

            throw new IllegalArgumentException(
                    String.format("La empresa ya tiene %d pago(s) %s en %d/%d. Límite permitido: %d",
                            cantidadActual, pagoEmpresa.getTipoPeriodicidad(), mes, año, limite)
            );
        }
    }

    /**
     * Valida las reglas de negocio para modificar un pago existente (excluyendo
     * el pago actual de la validación)
     */
    private void validarPagoEmpresaParaModificacion(PagoEmpresa pagoEmpresa, Long pagoActualId) {
        if (pagoEmpresa.getEmpresaId() == null) {
            throw new IllegalArgumentException("El ID de la empresa es obligatorio");
        }
        if (pagoEmpresa.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (pagoEmpresa.getTipoPeriodicidad() == null) {
            throw new IllegalArgumentException("El tipo de periodicidad es obligatorio");
        }

        Integer mes = pagoEmpresa.getFecha().getMonthValue();
        Integer año = pagoEmpresa.getFecha().getYear();

        Long cantidadActual = obtenerCantidadActual(pagoEmpresa.getEmpresaId(), 
                                                     pagoEmpresa.getTipoPeriodicidad(), 
                                                     mes, año);

        // Descontar el registro que se está editando en caso de que ya exista
        if (cantidadActual > 0) {
            cantidadActual = cantidadActual - 1;
        }

        Integer limite = obtenerLimitePorTipo(pagoEmpresa.getTipoPeriodicidad());

        if (cantidadActual >= limite) {
            throw new IllegalArgumentException(
                    String.format("La empresa ya tiene %d pago(s) %s en %d/%d. Límite permitido: %d",
                            cantidadActual, pagoEmpresa.getTipoPeriodicidad(), mes, año, limite)
            );
        }
    }
}

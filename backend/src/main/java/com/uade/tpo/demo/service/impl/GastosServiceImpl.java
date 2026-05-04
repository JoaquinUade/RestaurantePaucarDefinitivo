package com.uade.tpo.demo.service.impl;

import com.uade.tpo.demo.entity.Gastos;
import com.uade.tpo.demo.entity.dto.GastoRequest;
import com.uade.tpo.demo.repository.GastosRepository;
import com.uade.tpo.demo.service.GastosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class GastosServiceImpl implements GastosService {

    @Autowired
    private GastosRepository gastosRepository;

    @Override
    public Gastos crearGasto(GastoRequest gastoRequest) {
        Gastos gasto = new Gastos();
        gasto.setNombre(gastoRequest.getNombre());
        gasto.setDescripcion(gastoRequest.getDescripcion());
        gasto.setMonto(gastoRequest.getMonto());
        gasto.setPagar(gastoRequest.getPagar() != null ? gastoRequest.getPagar() : false);
        gasto.setObservacion(gastoRequest.getObservacion());
        gasto.setFecha(LocalDateTime.now());
        gasto.setDia(LocalDate.now().getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, 
                    new Locale("es", "ES")));
        return gastosRepository.save(gasto);
    }

    @Override
    public Gastos modificarGasto(Long id, Gastos gastosActualizado) {
        Optional<Gastos> gastoExistente = gastosRepository.findById(id);
        if (gastoExistente.isPresent()) {
            Gastos gasto = gastoExistente.get();
            
            if (gastosActualizado.getNombre() != null) {
                gasto.setNombre(gastosActualizado.getNombre());
            }
            if (gastosActualizado.getDescripcion() != null) {
                gasto.setDescripcion(gastosActualizado.getDescripcion());
            }
            if (gastosActualizado.getMonto() != null) {
                gasto.setMonto(gastosActualizado.getMonto());
            }
            if (gastosActualizado.getPagar() != null) {
                gasto.setPagar(gastosActualizado.getPagar());
            }
            if (gastosActualizado.getFecha() != null) {
                gasto.setFecha(gastosActualizado.getFecha());
            }
            if (gastosActualizado.getObservacion() != null) {
                gasto.setObservacion(gastosActualizado.getObservacion());
                gasto.setDia(gastosActualizado.getFecha().getDayOfWeek()
                            .getDisplayName(TextStyle.FULL, 
                            new Locale("es", "ES")));
            }
            
            return gastosRepository.save(gasto);
        }
        throw new IllegalArgumentException("Gasto no encontrado con id: " + id);
    }

    @Override
    public void borrarGasto(Long id) {
        if (gastosRepository.existsById(id)) {
            gastosRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Gasto no encontrado con id: " + id);
        }
    }

    @Override
    public List<Gastos> obtenerGastosPorAnioYMes(int anio, int mes) {
        return gastosRepository.findByAnioAndMes(anio, mes);
    }

    @Override
    public List<Gastos> obtenerGastosPorAnio(int anio) {
        return gastosRepository.findByAnio(anio);
    }

    @Override
    public List<Gastos> obtenerTodosLosGastos() {
        return gastosRepository.findAll();
    }

    @Override
    public Gastos obtenerGastoPorId(Long id) {
        return gastosRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gasto no encontrado con id: " + id));
    }
}

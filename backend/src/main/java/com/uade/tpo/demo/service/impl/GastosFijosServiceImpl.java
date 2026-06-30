package com.uade.tpo.demo.service.impl;

import com.uade.tpo.demo.entity.GastosFijos;
import com.uade.tpo.demo.entity.dto.GastoFijoRequest;
import com.uade.tpo.demo.repository.GastosFijosRepository;
import com.uade.tpo.demo.service.GastosFijosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GastosFijosServiceImpl implements GastosFijosService {

    @Autowired
    private GastosFijosRepository gastosFijosRepository;

    @Override
    public GastosFijos crearGastoFijo(GastoFijoRequest request) {
        GastosFijos gasto = new GastosFijos();
        gasto.setFecha(request.getFecha() != null ? request.getFecha() : LocalDate.now());
        gasto.setDetalle(request.getDetalle());
        gasto.setEstado(request.getEstado() != null ? request.getEstado() : false);
        gasto.setMonto(request.getMonto());
        gasto.setEsPersonal(request.getEsPersonal() != null ? request.getEsPersonal() : false);
        gasto.setObservacion(request.getObservacion());
        return gastosFijosRepository.save(gasto);
    }

    @Override
    public GastosFijos modificarGastoFijo(Long id, GastosFijos gastoActualizado) {
        Optional<GastosFijos> optional = gastosFijosRepository.findById(id);
        if (optional.isPresent()) {
            GastosFijos gasto = optional.get();
            if (gastoActualizado.getFecha() != null) gasto.setFecha(gastoActualizado.getFecha());
            if (gastoActualizado.getDetalle() != null) gasto.setDetalle(gastoActualizado.getDetalle());
            if (gastoActualizado.getEstado() != null) gasto.setEstado(gastoActualizado.getEstado());
            if (gastoActualizado.getMonto() != null) gasto.setMonto(gastoActualizado.getMonto());
            if (gastoActualizado.getEsPersonal() != null) gasto.setEsPersonal(gastoActualizado.getEsPersonal());
            if (gastoActualizado.getObservacion() != null) gasto.setObservacion(gastoActualizado.getObservacion());
            return gastosFijosRepository.save(gasto);
        }
        throw new IllegalArgumentException("Gasto fijo no encontrado con id: " + id);
    }

    @Override
    public void borrarGastoFijo(Long id) {
        if (gastosFijosRepository.existsById(id)) {
            gastosFijosRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Gasto fijo no encontrado con id: " + id);
        }
    }

    @Override
    public List<GastosFijos> obtenerGastosFijosPorAnioYMes(int anio, int mes) {
        return gastosFijosRepository.findAll()
                .stream()
                .filter(g -> g.getFecha() != null && g.getFecha().getMonthValue() == mes && g.getFecha().getYear() == anio)
                .collect(Collectors.toList());
    }

    @Override
    public List<GastosFijos> obtenerGastosFijosPorAnio(int anio) {
        return gastosFijosRepository.findAll()
                .stream()
                .filter(g -> g.getFecha() != null && g.getFecha().getYear() == anio)
                .collect(Collectors.toList());
    }

    @Override
    public List<GastosFijos> obtenerTodosLosGastosFijos() {
        return gastosFijosRepository.findAll();
    }

    @Override
    public GastosFijos obtenerGastoFijoPorId(Long id) {
        return gastosFijosRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gasto fijo no encontrado con id: " + id));
    }
}

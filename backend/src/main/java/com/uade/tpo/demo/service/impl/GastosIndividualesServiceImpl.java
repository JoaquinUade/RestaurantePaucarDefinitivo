package com.uade.tpo.demo.service.impl;

import com.uade.tpo.demo.entity.Empleado;
import com.uade.tpo.demo.entity.GastosIndividuales;
import com.uade.tpo.demo.entity.dto.GastoIndividualRequest;
import com.uade.tpo.demo.repository.GastosIndividualesRepository;
import com.uade.tpo.demo.repository.EmpleadoRepository;
import com.uade.tpo.demo.service.GastosIndividualesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GastosIndividualesServiceImpl implements GastosIndividualesService {

    @Autowired
    private GastosIndividualesRepository gastosIndividualesRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Override
    public GastosIndividuales crearGastoIndividual(GastoIndividualRequest request) {
        Empleado empleado = empleadoRepository.findById(request.getEmpleadoId())
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado con id: " + request.getEmpleadoId()));
        GastosIndividuales gasto = new GastosIndividuales();
        gasto.setFecha(request.getFecha() != null ? request.getFecha() : LocalDate.now());
        gasto.setDetalle(request.getDetalle());
        gasto.setMonto(request.getMonto());
        gasto.setEmpleado(empleado);
        return gastosIndividualesRepository.save(gasto);
    }

    @Override
    public GastosIndividuales modificarGastoIndividual(Long id, GastosIndividuales gastoActualizado) {
        Optional<GastosIndividuales> optional = gastosIndividualesRepository.findById(id);
        if (optional.isPresent()) {
            GastosIndividuales gasto = optional.get();
            if (gastoActualizado.getFecha() != null) gasto.setFecha(gastoActualizado.getFecha());
            if (gastoActualizado.getDetalle() != null) gasto.setDetalle(gastoActualizado.getDetalle());
            if (gastoActualizado.getMonto() != null) gasto.setMonto(gastoActualizado.getMonto());
            if (gastoActualizado.getEmpleado() != null) gasto.setEmpleado(gastoActualizado.getEmpleado());
            return gastosIndividualesRepository.save(gasto);
        }
        throw new IllegalArgumentException("Gasto individual no encontrado con id: " + id);
    }

    @Override
    public void borrarGastoIndividual(Long id) {
        if (gastosIndividualesRepository.existsById(id)) {
            gastosIndividualesRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Gasto individual no encontrado con id: " + id);
        }
    }

    @Override
    public List<GastosIndividuales> obtenerGastosIndividualesPorAnioYMes(int anio, int mes) {
        return gastosIndividualesRepository.findAll()
                .stream()
                .filter(g -> g.getFecha() != null && g.getFecha().getMonthValue() == mes && g.getFecha().getYear() == anio)
                .collect(Collectors.toList());
    }

    @Override
    public List<GastosIndividuales> obtenerGastosIndividualesPorAnio(int anio) {
        return gastosIndividualesRepository.findAll()
                .stream()
                .filter(g -> g.getFecha() != null && g.getFecha().getYear() == anio)
                .collect(Collectors.toList());
    }

    @Override
    public List<GastosIndividuales> obtenerTodosLosGastosIndividuales() {
        return gastosIndividualesRepository.findAll();
    }

    @Override
    public GastosIndividuales obtenerGastoIndividualPorId(Long id) {
        return gastosIndividualesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gasto individual no encontrado con id: " + id));
    }
}

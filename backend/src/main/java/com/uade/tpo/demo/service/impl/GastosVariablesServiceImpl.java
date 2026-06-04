package com.uade.tpo.demo.service.impl;

import com.uade.tpo.demo.entity.GastosVariables;
import com.uade.tpo.demo.entity.GastoVariableRequest;
import com.uade.tpo.demo.repository.GastosVariablesRepository;
import com.uade.tpo.demo.entity.CategoriaGastoVariable;
import com.uade.tpo.demo.repository.CategoriaGastoVariableRepository;
import com.uade.tpo.demo.service.GastosVariablesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GastosVariablesServiceImpl implements GastosVariablesService {

    @Autowired
    private GastosVariablesRepository gastosVariablesRepository;

    @Autowired
    private CategoriaGastoVariableRepository categoriaRepo;

    @Override
    public GastosVariables crearGastoVariable(GastoVariableRequest request) {
        GastosVariables gasto = new GastosVariables();
        gasto.setFecha(request.getFecha() != null ? request.getFecha() : LocalDate.now());
        gasto.setProducto(request.getProducto());
        gasto.setCantidad(request.getCantidad());
        gasto.setMedida(request.getMedida());
        gasto.setMonto(request.getMonto());
        if (request.getCategoriaId() != null) {
            CategoriaGastoVariable cat = categoriaRepo.findById(request.getCategoriaId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada con id: " + request.getCategoriaId()));
            gasto.setCategoria(cat);
        }
        return gastosVariablesRepository.save(gasto);
    }

    @Override
    public GastosVariables modificarGastoVariable(Long id, GastosVariables gastoActualizado) {
        Optional<GastosVariables> optional = gastosVariablesRepository.findById(id);
        if (optional.isPresent()) {
            GastosVariables gasto = optional.get();
            if (gastoActualizado.getFecha() != null) gasto.setFecha(gastoActualizado.getFecha());
            if (gastoActualizado.getProducto() != null) gasto.setProducto(gastoActualizado.getProducto());
            if (gastoActualizado.getCantidad() != null) gasto.setCantidad(gastoActualizado.getCantidad());
            if (gastoActualizado.getMedida() != null) gasto.setMedida(gastoActualizado.getMedida());
            if (gastoActualizado.getMonto() != null) gasto.setMonto(gastoActualizado.getMonto());
            if (gastoActualizado.getCategoria() != null) gasto.setCategoria(gastoActualizado.getCategoria());
            return gastosVariablesRepository.save(gasto);
        }
        throw new IllegalArgumentException("Gasto variable no encontrado con id: " + id);
    }

    @Override
    public void borrarGastoVariable(Long id) {
        if (gastosVariablesRepository.existsById(id)) {
            gastosVariablesRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Gasto variable no encontrado con id: " + id);
        }
    }

    @Override
    public List<GastosVariables> obtenerGastosVariablesPorAnioYMes(int anio, int mes) {
        return gastosVariablesRepository.findAll()
                .stream()
                .filter(g -> g.getFecha() != null && g.getFecha().getMonthValue() == mes && g.getFecha().getYear() == anio)
                .collect(Collectors.toList());
    }

    @Override
    public List<GastosVariables> obtenerGastosVariablesPorAnio(int anio) {
        return gastosVariablesRepository.findAll()
                .stream()
                .filter(g -> g.getFecha() != null && g.getFecha().getYear() == anio)
                .collect(Collectors.toList());
    }

    @Override
    public List<GastosVariables> obtenerTodosLosGastosVariables() {
        return gastosVariablesRepository.findAll();
    }

    @Override
    public GastosVariables obtenerGastoVariablePorId(Long id) {
        return gastosVariablesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gasto variable no encontrado con id: " + id));
    }
}

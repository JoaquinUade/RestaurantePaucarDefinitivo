package com.uade.tpo.demo.service.impl;

import com.uade.tpo.demo.entity.GastosVariables;
import com.uade.tpo.demo.entity.GastoVariableRequest;
import com.uade.tpo.demo.repository.GastosVariablesRepository;
import com.uade.tpo.demo.repository.StockRepository;
import com.uade.tpo.demo.entity.CategoriaGastoVariable;
import com.uade.tpo.demo.repository.CategoriaGastoVariableRepository;
import com.uade.tpo.demo.service.GastosVariablesService;
import com.uade.tpo.demo.entity.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GastosVariablesServiceImpl implements GastosVariablesService {

    @Autowired
    private GastosVariablesRepository gastosVariablesRepository;

    @Autowired
    private CategoriaGastoVariableRepository categoriaRepo;

    @Autowired
    private StockRepository stockRepository;

    @Override
    public GastosVariables crearGastoVariable(GastoVariableRequest request) {
        GastosVariables gasto = new GastosVariables();
        gasto.setFecha(request.getFecha() != null ? request.getFecha() : LocalDate.now());
        gasto.setProducto(request.getProducto());
        gasto.setCantComprada(request.getCantComprada());
        gasto.setMedida(request.getMedida());
        gasto.setMonto(request.getMonto());
        if (request.getCargadoEnStock() != null) {
            gasto.setCargadoEnStock(request.getCargadoEnStock());
        } else {
            gasto.setCargadoEnStock(false);
        }
        if (request.getCategoriaId() != null) {
            CategoriaGastoVariable cat = categoriaRepo.findById(request.getCategoriaId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada con id: " + request.getCategoriaId()));
            gasto.setCategoria(cat);
        }
        if (request.getStockId() != null) {

            Stock stock = stockRepository
                    .findById(request.getStockId())
                    .orElseThrow(()
                            -> new IllegalArgumentException(
                            "Stock no encontrado"));

            gasto.setStock(stock);
        }
        return gastosVariablesRepository.save(gasto);
    }

    @Override
    public GastosVariables modificarGastoVariable(
            Long id,
            GastoVariableRequest request) {

        GastosVariables gasto
                = gastosVariablesRepository.findById(id)
                        .orElseThrow(()
                                -> new IllegalArgumentException(
                                "Gasto variable no encontrado con id: "
                                + id));

        if (request.getFecha() != null) {
            gasto.setFecha(request.getFecha());
        }

        if (request.getProducto() != null) {
            gasto.setProducto(request.getProducto());
        }

        if (request.getCantComprada() != null) {
            gasto.setCantComprada(request.getCantComprada());
        }

        if (request.getMedida() != null) {
            gasto.setMedida(request.getMedida());
        }

        if (request.getMonto() != null) {
            gasto.setMonto(request.getMonto());
        }

        if (request.getCargadoEnStock() != null) {
            gasto.setCargadoEnStock(
                    request.getCargadoEnStock());
        }

        if (request.getCategoriaId() != null) {

            CategoriaGastoVariable categoria
                    = categoriaRepo.findById(
                            request.getCategoriaId())
                            .orElseThrow(()
                                    -> new IllegalArgumentException(
                                    "Categoria no encontrada"));

            gasto.setCategoria(categoria);
        }

        if (request.getStockId() != null) {

            Stock stock
                    = stockRepository.findById(
                            request.getStockId())
                            .orElseThrow(()
                                    -> new IllegalArgumentException(
                                    "Stock no encontrado"));

            gasto.setStock(stock);
        }

        return gastosVariablesRepository.save(gasto);
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
    public List<GastosVariables> obtenerGastosVariablesNoCargadosPorAnioYMes(int anio, int mes) {
        return gastosVariablesRepository.findNoCargadosEnStockPorAnioYMes(anio, mes);
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

@Override
public List<GastosVariables> obtenerPorCategoria(
        Long idCategoria) {

    return gastosVariablesRepository
            .findAll()
            .stream()
            .filter(g ->
                    g.getCategoria() != null
                    && g.getCategoria()
                            .getIdCategoria()
                            .equals(idCategoria))
            .toList();
}
@Override
public void desvincularStock(Long idGastoVariable) {

    GastosVariables gasto =
            gastosVariablesRepository
                    .findById(idGastoVariable)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Gasto variable no encontrado"));

    gasto.setStock(null);
    gasto.setCargadoEnStock(false);

    gastosVariablesRepository.save(gasto);
}
@Override
public List<GastosVariables> obtenerPorStock(
        Long idStock) {

    return gastosVariablesRepository
            .findByStock_IdStock(idStock);
}
}

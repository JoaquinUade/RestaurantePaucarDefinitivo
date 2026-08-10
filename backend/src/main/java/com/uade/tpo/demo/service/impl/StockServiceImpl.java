package com.uade.tpo.demo.service.impl;

import com.uade.tpo.demo.entity.CategoriaGastoVariable;
import com.uade.tpo.demo.entity.GastosVariables;
import com.uade.tpo.demo.entity.HistorialStock;
import com.uade.tpo.demo.entity.Stock;
import com.uade.tpo.demo.entity.dto.StockRequest;
import com.uade.tpo.demo.repository.CategoriaGastoVariableRepository;
import com.uade.tpo.demo.repository.GastosVariablesRepository;
import com.uade.tpo.demo.repository.HistorialStockRepository;
import com.uade.tpo.demo.repository.StockRepository;
import com.uade.tpo.demo.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private CategoriaGastoVariableRepository categoriaRepository;

    @Autowired
    private GastosVariablesRepository gastosVariablesRepository;
    @Autowired
    private HistorialStockRepository historialStockRepository;

    @Override
    public Stock agregarProductoAStock(StockRequest request) {
        if (request.getCategoriaId() == null) {
            throw new IllegalArgumentException("La categoria es obligatoria");
        }
        if (request.getNombreProducto() == null || request.getNombreProducto().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }

        CategoriaGastoVariable categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada con id: " + request.getCategoriaId()));

        BigDecimal cantComprada = request.getCantComprada() != null
                ? request.getCantComprada()
                : BigDecimal.ZERO;

        BigDecimal cantidad = request.getCantidad() != null
                ? request.getCantidad()
                : BigDecimal.ZERO;
        BigDecimal stockMinimo = request.getStockMinimo() != null
                ? request.getStockMinimo()
                : BigDecimal.ZERO;

        System.out.println("UNIDAD CANTIDAD = " + request.getUnidadCantidad());
        if (cantidad.signum() < 0) {
            throw new IllegalArgumentException("No se puede crear stock con una cantidad negativa");
        }

        Stock stock = new Stock(
                categoria,
                request.getNombreProducto().trim(),
                cantComprada,
                cantidad,
                stockMinimo,
                request.getUnidadCantComprada(),
                request.getUnidadCantidad()
        );
        System.out.println("STOCK UNIDAD = " + stock.getUnidadCantidad());
        stock.setFecha(
                request.getFecha() != null
                ? request.getFecha()
                : LocalDate.now()
        );

        Stock stockGuardado = stockRepository.save(stock);

        HistorialStock historial = new HistorialStock();

        historial.setStock(stockGuardado);

        historial.setMovimiento(BigDecimal.ZERO);

        historial.setCantidad(stock.getCantidad());

        historial.setFecha(stockGuardado.getFecha());

        historialStockRepository.save(historial);

        return stockGuardado;
    }

    @Override
    public Stock ajustarStockDisponible(
            Long id,
            BigDecimal stockDisponible,
            LocalDate fecha) {

        Stock stock = stockRepository.findById(id)
                .orElseThrow(()
                        -> new IllegalArgumentException(
                        "Stock no encontrado"));

        BigDecimal stockActual
                = stock.getCantidad();

        BigDecimal movimiento
                = stockDisponible.subtract(
                        stockActual
                );

        HistorialStock historial
                = new HistorialStock();

        historial.setStock(stock);

        historial.setFecha(fecha);

        historial.setMovimiento(movimiento);

        historialStockRepository.save(historial);

        recalcularHistorial(id);

        return stockRepository
                .findById(id)
                .orElseThrow();
    }

    @Override
    public Stock modificarStock(Long id, Stock stockActualizado) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stock no encontrado con id: " + id));

        if (stockActualizado.getCantComprada() != null) {
            stock.setCantComprada(stockActualizado.getCantComprada());
        }
        if (stockActualizado.getCantidad() != null) {
            stock.setCantidad(stockActualizado.getCantidad());
        }
        if (stockActualizado.getStockMinimo() != null) {
            stock.setStockMinimo(stockActualizado.getStockMinimo());
        }
        if (stockActualizado.getFecha() != null) {
            stock.setFecha(stockActualizado.getFecha());
        }
        if (stockActualizado.getNombreProducto() != null && !stockActualizado.getNombreProducto().isBlank()) {
            stock.setNombreProducto(stockActualizado.getNombreProducto().trim());
        }
        if (stockActualizado.getCategoriaGastoVariable() != null && stockActualizado.getCategoriaGastoVariable().getIdCategoria() != null) {
            CategoriaGastoVariable categoria = categoriaRepository.findById(stockActualizado.getCategoriaGastoVariable().getIdCategoria())
                    .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada con id: " + stockActualizado.getCategoriaGastoVariable().getIdCategoria()));
            stock.setCategoriaGastoVariable(categoria);
        }
        if (stockActualizado.getUnidadCantComprada() != null) {
            stock.setUnidadCantComprada(stockActualizado.getUnidadCantComprada());
        }

        if (stockActualizado.getUnidadCantidad() != null) {
            stock.setUnidadCantidad(stockActualizado.getUnidadCantidad());
        }

        return stockRepository.save(stock);
    }

    @Override
    public void eliminarStock(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(()
                        -> new IllegalArgumentException(
                        "Stock no encontrado con id: " + id));
        List<GastosVariables> gastos
                = gastosVariablesRepository.findByStock(stock);

        for (GastosVariables gasto : gastos) {

            gasto.setStock(null);

            gastosVariablesRepository.save(gasto);

        }
        stockRepository.delete(stock);
    }

    @Override
    public Optional<Stock> obtenerStockPorId(Long id) {
        return stockRepository.findById(id);
    }

    @Override
    public List<Stock> obtenerTodosLosStocks() {
        return stockRepository.findAll();
    }

    @Override
    public List<Stock> obtenerProductosEnFaltaDeStock() {
        return stockRepository.findStockBajoMinimo();
    }

    @Override
    public List<HistorialStock> obtenerHistorialPorStock(
            Long idStock) {

        return historialStockRepository
                .findByStock_IdStockOrderByFechaAscIdAsc(
                        idStock);
    }

    @Override
    public List<HistorialStock> obtenerHistorialMes(
            LocalDate desde,
            LocalDate hasta) {
        System.out.println(
                "OBTENIENDO HISTORIAL MES: "
                + desde
                + " - "
                + hasta);
        return historialStockRepository
                .findByFechaBetweenOrderByFechaAsc(
                        desde,
                        hasta);
    }

    @Override
    public Stock sumarStock(
            Long idStock,
            BigDecimal cantidadASumar,
            LocalDate fecha,
            Long idGastoVariable) {

        Stock stock = stockRepository.findById(idStock)
                .orElseThrow(()
                        -> new IllegalArgumentException(
                        "Stock no encontrado"));

        List<HistorialStock> historiales
                = historialStockRepository
                        .findByStock_IdStockOrderByFechaAscIdAsc(
                                idStock);

        BigDecimal ultimaCantidad = BigDecimal.ZERO;
        GastosVariables gastoVariable = null;

        if (idGastoVariable != null) {

    gastoVariable = gastosVariablesRepository
            .findById(idGastoVariable)
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "Gasto variable no encontrado"));

    if (Boolean.TRUE.equals(
            gastoVariable.getCargadoEnStock())) {

        throw new IllegalArgumentException(
                "El gasto ya fue cargado al stock");
    }

    gastoVariable.setCargadoEnStock(true);
    gastoVariable.setStock(stock);

    gastosVariablesRepository.save(gastoVariable);
}
        for (HistorialStock h : historiales) {

            if (!h.getFecha().isAfter(fecha)) {
                ultimaCantidad = h.getCantidad();
            }
        }
        HistorialStock nuevo = new HistorialStock();

        nuevo.setStock(stock);
        nuevo.setFecha(fecha);
        nuevo.setMovimiento(cantidadASumar);
        nuevo.setCantidad(
                ultimaCantidad.add(cantidadASumar));

        nuevo.setGastoVariable(gastoVariable);

        historialStockRepository.save(nuevo);
        recalcularHistorial(idStock);
        stock.setCantidad(
                ultimaCantidad.add(cantidadASumar)
        );

        return stockRepository.save(stock);
    }

    private void recalcularHistorial(
            Long idStock) {

        List<HistorialStock> historiales
                = historialStockRepository
                        .findByStock_IdStockOrderByFechaAscIdAsc(
                                idStock);

        BigDecimal acumulado
                = BigDecimal.ZERO;

        for (HistorialStock h : historiales) {

            acumulado
                    = acumulado.add(
                            h.getMovimiento()
                    );

            h.setCantidad(acumulado);

            historialStockRepository.save(h);
        }

        Stock stock
                = stockRepository.findById(idStock)
                        .orElseThrow();

        stock.setCantidad(acumulado);

        stockRepository.save(stock);
    }

    @Override
    public Stock restarStock(
            Long idStock,
            BigDecimal cantidadARestar) {

        Stock stock = stockRepository.findById(idStock)
                .orElseThrow(()
                        -> new IllegalArgumentException(
                        "Stock no encontrado"));

        if (cantidadARestar == null
                || cantidadARestar.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad a restar debe ser mayor a cero");
        }

        if (cantidadARestar.compareTo(stock.getCantidad()) > 0) {

            throw new IllegalArgumentException(
                    "No hay stock suficiente");
        }

        HistorialStock historial
                = new HistorialStock();

        historial.setStock(stock);

        historial.setFecha(LocalDate.now());

        historial.setMovimiento(
                cantidadARestar.negate()
        );
        historial.setCantidad(
                stock.getCantidad()
                        .subtract(cantidadARestar)
        );
        historialStockRepository.save(historial);

        recalcularHistorial(idStock);

        return stockRepository
                .findById(idStock)
                .orElseThrow();
    }
}

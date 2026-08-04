package com.uade.tpo.demo.service.impl;

import com.uade.tpo.demo.entity.CategoriaGastoVariable;
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
        historial.setCantidad(stock.getCantComprada());

        historial.setFecha(stockGuardado.getFecha());

        historialStockRepository.save(historial);

        return stockGuardado;
    }

    @Override
    public Stock ajustarStockDisponible(Long id, BigDecimal stockDisponible, LocalDate fecha) {
        System.out.println(
                "AJUSTANDO STOCK: "
                + id
                + " -> "
                + stockDisponible);
        Stock stock = stockRepository.findById(id)
                .orElseThrow(()
                        -> new IllegalArgumentException(
                        "Stock no encontrado con id: "
                        + id));

        if (stockDisponible == null) {
            throw new IllegalArgumentException(
                    "La cantidad no puede ser nula");
        }

        if (stockDisponible.signum() < 0) {
            throw new IllegalArgumentException(
                    "La cantidad no puede ser negativa");
        }

        stock.setCantidad(stockDisponible);

        Optional<HistorialStock> historialExistente
                = historialStockRepository
                        .findByStock_IdStockAndFecha(
                                stock.getIdStock(),
                                fecha);

        if (historialExistente.isPresent()) {

            HistorialStock historial
                    = historialExistente.get();

            historial.setCantidad(stockDisponible);

            historialStockRepository.save(historial);

        } else {

            HistorialStock historial
                    = new HistorialStock();

            historial.setStock(stock);
            historial.setCantidad(stockDisponible);
            historial.setFecha(fecha);

            historialStockRepository.save(historial);
        }
        return stockRepository.save(stock);
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
        if (!stockRepository.existsById(id)) {
            throw new IllegalArgumentException("Stock no encontrado con id: " + id);
        }
        stockRepository.deleteById(id);
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
                .findByStock_IdStockOrderByFechaAsc(idStock);
    }
}

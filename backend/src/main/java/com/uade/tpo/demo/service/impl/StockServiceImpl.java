package com.uade.tpo.demo.service.impl;

import com.uade.tpo.demo.entity.CategoriaGastoVariable;
import com.uade.tpo.demo.entity.Stock;
import com.uade.tpo.demo.entity.dto.StockRequest;
import com.uade.tpo.demo.repository.CategoriaGastoVariableRepository;
import com.uade.tpo.demo.repository.StockRepository;
import com.uade.tpo.demo.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private CategoriaGastoVariableRepository categoriaRepository;

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

        BigDecimal cantidad = request.getCantidad() != null ? request.getCantidad() : BigDecimal.ZERO;
        BigDecimal stockMinimo = request.getStockMinimo() != null ? request.getStockMinimo() : BigDecimal.ZERO;
        if (cantidad.signum() == 0) {
            throw new IllegalArgumentException("La cantidad debe ser distinta de cero");
        }

        Optional<Stock> stockExistente = stockRepository.findByCategoriaGastoVariable_IdCategoria(categoria.getIdCategoria());
        if (stockExistente.isPresent()) {
    Stock stock = stockExistente.get();

    stock.setNombreProducto(
            request.getNombreProducto().trim());

    stock.setStockMinimo(stockMinimo);

    stock.setUnidadStockMinimo(
            request.getUnidadStockMinimo());

    return stockRepository.save(stock);
}

        if (cantidad.signum() < 0) {
            throw new IllegalArgumentException("No se puede crear stock con una cantidad negativa");
        }

        Stock stock = new Stock(
                categoria,
                request.getNombreProducto().trim(),
                cantidad,
                stockMinimo,
                request.getUnidadCantidad(),
                request.getUnidadStockMinimo()
        );
        return stockRepository.save(stock);
    }

    @Override
    public Stock ajustarCantidadStock(Long id, BigDecimal cantidad) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stock no encontrado con id: " + id));

        aplicarCambioCantidad(stock, cantidad);
        return stockRepository.save(stock);
    }

    private void aplicarCambioCantidad(Stock stock, BigDecimal cantidad) {
        if (cantidad == null || cantidad.signum() == 0) {
            throw new IllegalArgumentException("La cantidad debe ser distinta de cero");
        }

        BigDecimal nuevaCantidad = stock.getCantidad().add(cantidad);
        if (nuevaCantidad.signum() < 0) {
            throw new IllegalArgumentException("No hay stock suficiente para descontar esa cantidad");
        }

        stock.setCantidad(nuevaCantidad);
    }

    @Override
    public Stock modificarStock(Long id, Stock stockActualizado) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stock no encontrado con id: " + id));

        if (stockActualizado.getCantidad() != null) {
            stock.setCantidad(stockActualizado.getCantidad());
        }
        if (stockActualizado.getStockMinimo() != null) {
            stock.setStockMinimo(stockActualizado.getStockMinimo());
        }
        if (stockActualizado.getNombreProducto() != null && !stockActualizado.getNombreProducto().isBlank()) {
            stock.setNombreProducto(stockActualizado.getNombreProducto().trim());
        }
        if (stockActualizado.getCategoriaGastoVariable() != null && stockActualizado.getCategoriaGastoVariable().getIdCategoria() != null) {
            CategoriaGastoVariable categoria = categoriaRepository.findById(stockActualizado.getCategoriaGastoVariable().getIdCategoria())
                    .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada con id: " + stockActualizado.getCategoriaGastoVariable().getIdCategoria()));
            stock.setCategoriaGastoVariable(categoria);
        }
        if (stockActualizado.getUnidadCantidad() != null) {
            stock.setUnidadCantidad(stockActualizado.getUnidadCantidad());
        }

        if (stockActualizado.getUnidadStockMinimo() != null) {
            stock.setUnidadStockMinimo(stockActualizado.getUnidadStockMinimo());
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
}

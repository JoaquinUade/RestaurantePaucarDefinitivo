package com.uade.tpo.demo.service.impl;

import com.uade.tpo.demo.entity.Stock;
import com.uade.tpo.demo.entity.dto.StockRequest;
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

    @Override
    public Stock agregarProductoAStock(StockRequest request) {
        if (request.getNombreProducto() == null || request.getNombreProducto().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }

        BigDecimal cantidad = request.getCantidad() != null ? request.getCantidad() : BigDecimal.ZERO;
        BigDecimal stockMinimo = request.getStockMinimo() != null ? request.getStockMinimo() : BigDecimal.ZERO;

        String nombreProducto = request.getNombreProducto().trim();
        Optional<Stock> stockExistente = stockRepository.findByNombreProductoIgnoreCase(nombreProducto);
        if (stockExistente.isPresent()) {
            Stock stock = stockExistente.get();
            stock.setCantidad(stock.getCantidad().add(cantidad));
            stock.setStockMinimo(stockMinimo);
            return stockRepository.save(stock);
        }

        Stock stock = new Stock(nombreProducto, cantidad, stockMinimo);
        return stockRepository.save(stock);
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

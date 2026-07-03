package com.uade.tpo.demo.service;

import com.uade.tpo.demo.entity.Stock;
import com.uade.tpo.demo.entity.dto.StockRequest;

import java.util.List;
import java.util.Optional;

public interface StockService {
    Stock agregarProductoAStock(StockRequest request);
    Stock modificarStock(Long id, Stock stock);
    void eliminarStock(Long id);
    Optional<Stock> obtenerStockPorId(Long id);
    List<Stock> obtenerTodosLosStocks();
    List<Stock> obtenerProductosEnFaltaDeStock();
}

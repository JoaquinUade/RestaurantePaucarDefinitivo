package com.uade.tpo.demo.service;

import com.uade.tpo.demo.entity.Stock;
import com.uade.tpo.demo.entity.dto.StockRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import com.uade.tpo.demo.entity.HistorialStock;

public interface StockService {

    Stock agregarProductoAStock(StockRequest request);

    Stock ajustarStockDisponible(Long id, BigDecimal cantidad, LocalDate fecha);

    Stock modificarStock(Long id, Stock stock);

    void eliminarStock(Long id);

    Optional<Stock> obtenerStockPorId(Long id);

    List<Stock> obtenerTodosLosStocks();

    List<Stock> obtenerProductosEnFaltaDeStock();

    List<HistorialStock> obtenerHistorialPorStock(Long idStock);
}

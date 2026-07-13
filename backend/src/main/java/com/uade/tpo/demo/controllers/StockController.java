package com.uade.tpo.demo.controllers;

import com.uade.tpo.demo.entity.HistorialStock;
import com.uade.tpo.demo.entity.Stock;
import com.uade.tpo.demo.entity.dto.StockRequest;
import com.uade.tpo.demo.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    @Autowired
    private StockService stockService;

    @PostMapping
    public ResponseEntity<Stock> agregarProductoAStock(@RequestBody StockRequest request) {
        Stock stock = stockService.agregarProductoAStock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(stock);
    }

    @PatchMapping("/{id}/cantidad")
    public ResponseEntity<Stock> ajustarCantidadStock(@PathVariable Long id, @RequestParam BigDecimal cantidad) {
        return ResponseEntity.ok(stockService.ajustarCantidadStock(id, cantidad));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Stock> modificarStock(@PathVariable Long id, @RequestBody Stock stock) {
        return ResponseEntity.ok(stockService.modificarStock(id, stock));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarStock(@PathVariable Long id) {
        stockService.eliminarStock(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Stock>> obtenerTodosLosStocks() {
        return ResponseEntity.ok(stockService.obtenerTodosLosStocks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Stock> obtenerStockPorId(@PathVariable Long id) {
        return stockService.obtenerStockPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/faltante")
    public ResponseEntity<List<Stock>> obtenerProductosEnFaltaDeStock() {
        return ResponseEntity.ok(stockService.obtenerProductosEnFaltaDeStock());
    }
    @GetMapping("/{id}/historial")
public ResponseEntity<List<HistorialStock>> obtenerHistorial(
        @PathVariable Long id) {

    return ResponseEntity.ok(
            stockService.obtenerHistorialPorStock(id)
    );
}
}

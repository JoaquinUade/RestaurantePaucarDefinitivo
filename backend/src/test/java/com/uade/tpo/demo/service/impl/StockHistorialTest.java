package com.uade.tpo.demo.service.impl;

import com.uade.tpo.demo.entity.*;
import com.uade.tpo.demo.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StockHistorialTest {
    StockServiceImpl service;
    HistorialStockRepository historial;
    StockRepository stocks;
    Stock stock;
    HistorialStock primero;
    HistorialStock segundo;

    @BeforeEach
    void preparar() {
        service = new StockServiceImpl();
        historial = mock(HistorialStockRepository.class);
        stocks = mock(StockRepository.class);
        ReflectionTestUtils.setField(service, "historialStockRepository", historial);
        ReflectionTestUtils.setField(service, "stockRepository", stocks);
        ReflectionTestUtils.setField(service, "gastosVariablesRepository", mock(GastosVariablesRepository.class));
        stock = new Stock();
        ReflectionTestUtils.setField(stock, "idStock", 1L);
        primero = movimiento("2026-09-01", "0", "10");
        segundo = movimiento("2026-09-02", "5", "15");
        when(historial.findById(2L)).thenReturn(Optional.of(segundo));
        when(historial.findByStock_IdStockOrderByFechaAscIdAsc(1L)).thenAnswer(call -> {
            List<HistorialStock> lista = new ArrayList<>(List.of(primero, segundo));
            lista.sort(Comparator.comparing(HistorialStock::getFecha));
            return lista;
        });
    }

    HistorialStock movimiento(String fecha, String cambio, String cantidad) {
        HistorialStock h = new HistorialStock();
        h.setStock(stock);
        h.setFecha(LocalDate.parse(fecha));
        h.setMovimiento(new BigDecimal(cambio));
        h.setCantidad(new BigDecimal(cantidad));
        return h;
    }

    @Test
    void cambiarFechaGuardaYReordenaSinPerderSaldoInicial() {
        HistorialStock cambios = movimiento("2026-08-30", "5", "15");
        service.editarMovimientoHistorial(2L, cambios);
        assertEquals(cambios.getFecha(), segundo.getFecha());
        assertEquals(new BigDecimal("15"), primero.getCantidad());
        assertEquals(new BigDecimal("15"), stock.getCantidad());
        verify(historial).saveAndFlush(segundo);
        verify(stocks).save(stock);
    }

    @Test
    void cambiarMovimientoActualizaStock() {
        service.editarMovimientoHistorial(2L, movimiento("2026-09-02", "7", "15"));
        assertEquals(new BigDecimal("17"), segundo.getCantidad());
        assertEquals(new BigDecimal("17"), stock.getCantidad());
    }

    @Test
    void fechaVaciaNoGuarda() {
        HistorialStock cambios = movimiento("2026-09-02", "7", "15");
        cambios.setFecha(null);
        assertThrows(IllegalArgumentException.class, () -> service.editarMovimientoHistorial(2L, cambios));
        verifyNoInteractions(historial, stocks);
    }
}

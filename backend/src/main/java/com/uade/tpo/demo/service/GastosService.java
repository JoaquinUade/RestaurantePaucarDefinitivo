package com.uade.tpo.demo.service;

import com.uade.tpo.demo.entity.Gastos;
import com.uade.tpo.demo.entity.dto.GastoRequest;
import java.util.List;

public interface GastosService {
    Gastos crearGasto(GastoRequest gastoRequest);
    Gastos modificarGasto(Long id, Gastos gasto);
    void borrarGasto(Long id);
    List<Gastos> obtenerGastosPorAnioYMes(int anio, int mes);
    List<Gastos> obtenerGastosPorAnio(int anio);
    List<Gastos> obtenerTodosLosGastos();
    Gastos obtenerGastoPorId(Long id);
}

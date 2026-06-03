package com.uade.tpo.demo.service;

import com.uade.tpo.demo.entity.GastosFijos;
import com.uade.tpo.demo.entity.dto.GastoFijoRequest;

import java.util.List;

public interface GastosFijosService {
    GastosFijos crearGastoFijo(GastoFijoRequest request);
    GastosFijos modificarGastoFijo(Long id, GastosFijos gasto);
    void borrarGastoFijo(Long id);
    List<GastosFijos> obtenerGastosFijosPorAnioYMes(int anio, int mes);
    List<GastosFijos> obtenerGastosFijosPorAnio(int anio);
    List<GastosFijos> obtenerTodosLosGastosFijos();
    GastosFijos obtenerGastoFijoPorId(Long id);
}

package com.uade.tpo.demo.service;

import com.uade.tpo.demo.entity.GastosIndividuales;
import com.uade.tpo.demo.entity.dto.GastoIndividualRequest;

import java.util.List;

public interface GastosIndividualesService {
    GastosIndividuales crearGastoIndividual(GastoIndividualRequest request);
    GastosIndividuales modificarGastoIndividual(Long id, GastosIndividuales gasto);
    void borrarGastoIndividual(Long id);
    List<GastosIndividuales> obtenerGastosIndividualesPorAnioYMes(int anio, int mes);
    List<GastosIndividuales> obtenerGastosIndividualesPorAnio(int anio);
    List<GastosIndividuales> obtenerTodosLosGastosIndividuales();
    GastosIndividuales obtenerGastoIndividualPorId(Long id);
}

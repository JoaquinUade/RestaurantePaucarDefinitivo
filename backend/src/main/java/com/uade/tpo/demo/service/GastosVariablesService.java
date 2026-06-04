package com.uade.tpo.demo.service;

import com.uade.tpo.demo.entity.GastosVariables;
import com.uade.tpo.demo.entity.GastoVariableRequest;

import java.util.List;

public interface GastosVariablesService {
    GastosVariables crearGastoVariable(GastoVariableRequest request);
    GastosVariables modificarGastoVariable(Long id, GastosVariables gasto);
    void borrarGastoVariable(Long id);
    List<GastosVariables> obtenerGastosVariablesPorAnioYMes(int anio, int mes);
    List<GastosVariables> obtenerGastosVariablesPorAnio(int anio);
    List<GastosVariables> obtenerTodosLosGastosVariables();
    GastosVariables obtenerGastoVariablePorId(Long id);
}

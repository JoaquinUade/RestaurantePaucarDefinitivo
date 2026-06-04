package com.uade.tpo.demo.service;

import com.uade.tpo.demo.entity.CategoriaGastoVariable;

import java.util.List;

public interface CategoriaGastoVariableService {
    CategoriaGastoVariable crearCategoria(CategoriaGastoVariable categoria);
    CategoriaGastoVariable modificarCategoria(Long id, CategoriaGastoVariable categoria);
    void borrarCategoria(Long id);
    List<CategoriaGastoVariable> obtenerTodas();
    CategoriaGastoVariable obtenerPorId(Long id);
}

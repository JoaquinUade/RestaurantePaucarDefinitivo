package com.uade.tpo.demo.service;

import com.uade.tpo.demo.entity.Empleado;

import java.util.List;

public interface EmpleadoService {
    Empleado crearEmpleado(Empleado empleado);
    Empleado modificarEmpleado(Long id, Empleado empleado);
    void borrarEmpleado(Long id);
    List<Empleado> obtenerTodosLosEmpleados();
    Empleado obtenerEmpleadoPorId(Long id);
}

package com.uade.tpo.demo.service.impl;

import com.uade.tpo.demo.entity.Empleado;
import com.uade.tpo.demo.repository.EmpleadoRepository;
import com.uade.tpo.demo.service.EmpleadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmpleadoServiceImpl implements EmpleadoService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Override
    public Empleado crearEmpleado(Empleado empleado) {
        return empleadoRepository.save(empleado);
    }

    @Override
    public Empleado modificarEmpleado(Long id, Empleado empleadoActualizado) {
        Optional<Empleado> optional = empleadoRepository.findById(id);
        if (optional.isPresent()) {
            Empleado emp = optional.get();
            if (empleadoActualizado.getNombre() != null) emp.setNombre(empleadoActualizado.getNombre());
            if (empleadoActualizado.getApellido() != null) emp.setApellido(empleadoActualizado.getApellido());
            return empleadoRepository.save(emp);
        }
        throw new IllegalArgumentException("Empleado no encontrado con id: " + id);
    }

    @Override
    public void borrarEmpleado(Long id) {
        if (empleadoRepository.existsById(id)) {
            empleadoRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Empleado no encontrado con id: " + id);
        }
    }

    @Override
    public List<Empleado> obtenerTodosLosEmpleados() {
        return empleadoRepository.findAll();
    }

    @Override
    public Empleado obtenerEmpleadoPorId(Long id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado con id: " + id));
    }
}

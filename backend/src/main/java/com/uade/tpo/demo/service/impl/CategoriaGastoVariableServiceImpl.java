package com.uade.tpo.demo.service.impl;

import com.uade.tpo.demo.entity.CategoriaGastoVariable;
import com.uade.tpo.demo.repository.CategoriaGastoVariableRepository;
import com.uade.tpo.demo.service.CategoriaGastoVariableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaGastoVariableServiceImpl implements CategoriaGastoVariableService {

    @Autowired
    private CategoriaGastoVariableRepository categoriaRepository;

    @Override
    public CategoriaGastoVariable crearCategoria(CategoriaGastoVariable categoria) {
        return categoriaRepository.save(categoria);
    }

    @Override
    public CategoriaGastoVariable modificarCategoria(Long id, CategoriaGastoVariable categoriaActualizada) {
        Optional<CategoriaGastoVariable> optional = categoriaRepository.findById(id);
        if (optional.isPresent()) {
            CategoriaGastoVariable cat = optional.get();
            if (categoriaActualizada.getNombre() != null) cat.setNombre(categoriaActualizada.getNombre());
            return categoriaRepository.save(cat);
        }
        throw new IllegalArgumentException("Categoria no encontrada con id: " + id);
    }

    @Override
    public void borrarCategoria(Long id) {
        if (categoriaRepository.existsById(id)) {
            categoriaRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Categoria no encontrada con id: " + id);
        }
    }

    @Override
    public List<CategoriaGastoVariable> obtenerTodas() {
        return categoriaRepository.findAll();
    }

    @Override
    public CategoriaGastoVariable obtenerPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada con id: " + id));
    }
}

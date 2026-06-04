package com.uade.tpo.demo.controllers;

import com.uade.tpo.demo.entity.CategoriaGastoVariable;
import com.uade.tpo.demo.service.CategoriaGastoVariableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias-gasto-variable")
public class CategoriaGastoVariableController {

    @Autowired
    private CategoriaGastoVariableService categoriaService;

    @PostMapping
    public ResponseEntity<CategoriaGastoVariable> crear(@RequestBody CategoriaGastoVariable request) {
        CategoriaGastoVariable creado = categoriaService.crearCategoria(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CategoriaGastoVariable> modificar(@PathVariable Long id, @RequestBody CategoriaGastoVariable categoria) {
        CategoriaGastoVariable mod = categoriaService.modificarCategoria(id, categoria);
        return ResponseEntity.ok(mod);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id) {
        categoriaService.borrarCategoria(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<CategoriaGastoVariable>> obtenerTodas() {
        return ResponseEntity.ok(categoriaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaGastoVariable> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.obtenerPorId(id));
    }
}

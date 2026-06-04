package com.uade.tpo.demo.controllers;

import com.uade.tpo.demo.entity.GastosVariables;
import com.uade.tpo.demo.entity.GastoVariableRequest;
import com.uade.tpo.demo.service.GastosVariablesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gastos-variables")
public class GastosVariablesController {

    @Autowired
    private GastosVariablesService gastosVariablesService;

    @PostMapping
    public ResponseEntity<GastosVariables> crear(@RequestBody GastoVariableRequest request) {
        GastosVariables creado = gastosVariablesService.crearGastoVariable(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GastosVariables> modificar(@PathVariable Long id, @RequestBody GastosVariables gasto) {
        GastosVariables mod = gastosVariablesService.modificarGastoVariable(id, gasto);
        return ResponseEntity.ok(mod);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id) {
        gastosVariablesService.borrarGastoVariable(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<GastosVariables>> obtenerTodos() {
        return ResponseEntity.ok(gastosVariablesService.obtenerTodosLosGastosVariables());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GastosVariables> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(gastosVariablesService.obtenerGastoVariablePorId(id));
    }

    @GetMapping("/anio/{anio}")
    public ResponseEntity<List<GastosVariables>> obtenerPorAnio(@PathVariable int anio) {
        return ResponseEntity.ok(gastosVariablesService.obtenerGastosVariablesPorAnio(anio));
    }

    @GetMapping("/anio/{anio}/mes/{mes}")
    public ResponseEntity<List<GastosVariables>> obtenerPorAnioYMes(@PathVariable int anio, @PathVariable int mes) {
        return ResponseEntity.ok(gastosVariablesService.obtenerGastosVariablesPorAnioYMes(anio, mes));
    }
}

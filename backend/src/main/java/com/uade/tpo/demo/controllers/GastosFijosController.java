package com.uade.tpo.demo.controllers;

import com.uade.tpo.demo.entity.GastosFijos;
import com.uade.tpo.demo.entity.dto.GastoFijoRequest;
import com.uade.tpo.demo.service.GastosFijosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gastos-fijos")
public class GastosFijosController {

    @Autowired
    private GastosFijosService gastosFijosService;

    @PostMapping
    public ResponseEntity<GastosFijos> crearGastoFijo(@RequestBody GastoFijoRequest request) {
        GastosFijos creado = gastosFijosService.crearGastoFijo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GastosFijos> modificarGastoFijo(@PathVariable Long id, @RequestBody GastosFijos gasto) {
        GastosFijos mod = gastosFijosService.modificarGastoFijo(id, gasto);
        return ResponseEntity.ok(mod);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrarGastoFijo(@PathVariable Long id) {
        gastosFijosService.borrarGastoFijo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<GastosFijos>> obtenerTodos() {
        return ResponseEntity.ok(gastosFijosService.obtenerTodosLosGastosFijos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GastosFijos> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(gastosFijosService.obtenerGastoFijoPorId(id));
    }

    @GetMapping("/anio/{anio}")
    public ResponseEntity<List<GastosFijos>> obtenerPorAnio(@PathVariable int anio) {
        return ResponseEntity.ok(gastosFijosService.obtenerGastosFijosPorAnio(anio));
    }

    @GetMapping("/anio/{anio}/mes/{mes}")
    public ResponseEntity<List<GastosFijos>> obtenerPorAnioYMes(@PathVariable int anio, @PathVariable int mes) {
        return ResponseEntity.ok(gastosFijosService.obtenerGastosFijosPorAnioYMes(anio, mes));
    }
}

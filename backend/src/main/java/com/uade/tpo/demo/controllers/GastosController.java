package com.uade.tpo.demo.controllers;

import com.uade.tpo.demo.entity.Gastos;
import com.uade.tpo.demo.entity.dto.GastoRequest;
import com.uade.tpo.demo.service.GastosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gastos")
public class GastosController {

    @Autowired
    private GastosService gastosService;

    @PostMapping
    public ResponseEntity<Gastos> crearGasto(@RequestBody GastoRequest gastoRequest) {
        Gastos nuevoGasto = gastosService.crearGasto(gastoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoGasto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Gastos> modificarGasto(@PathVariable Long id, @RequestBody Gastos gasto) {
        Gastos gastoModificado = gastosService.modificarGasto(id, gasto);
        return ResponseEntity.ok(gastoModificado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrarGasto(@PathVariable Long id) {
        gastosService.borrarGasto(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Gastos>> obtenerTodosLosGastos() {
        List<Gastos> gastos = gastosService.obtenerTodosLosGastos();
        return ResponseEntity.ok(gastos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Gastos> obtenerGastoPorId(@PathVariable Long id) {
        Gastos gasto = gastosService.obtenerGastoPorId(id);
        return ResponseEntity.ok(gasto);
    }

    @GetMapping("/anio/{anio}")
    public ResponseEntity<List<Gastos>> obtenerGastosPorAnio(@PathVariable int anio) {
        List<Gastos> gastos = gastosService.obtenerGastosPorAnio(anio);
        return ResponseEntity.ok(gastos);
    }

    @GetMapping("/anio/{anio}/mes/{mes}")
    public ResponseEntity<List<Gastos>> obtenerGastosPorAnioYMes(@PathVariable int anio, @PathVariable int mes) {
        List<Gastos> gastos = gastosService.obtenerGastosPorAnioYMes(anio, mes);
        return ResponseEntity.ok(gastos);
    }
}

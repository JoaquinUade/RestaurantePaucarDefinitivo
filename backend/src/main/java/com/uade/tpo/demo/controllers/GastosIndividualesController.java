package com.uade.tpo.demo.controllers;

import com.uade.tpo.demo.entity.GastosIndividuales;
import com.uade.tpo.demo.entity.dto.GastoIndividualRequest;
import com.uade.tpo.demo.service.GastosIndividualesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gastos-individuales")
public class GastosIndividualesController {

    @Autowired
    private GastosIndividualesService gastosIndividualesService;

    @PostMapping
    public ResponseEntity<GastosIndividuales> crearGasto(@RequestBody GastoIndividualRequest request) {
        GastosIndividuales creado = gastosIndividualesService.crearGastoIndividual(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GastosIndividuales> modificarGasto(@PathVariable Long id, @RequestBody GastosIndividuales gasto) {
        GastosIndividuales mod = gastosIndividualesService.modificarGastoIndividual(id, gasto);
        return ResponseEntity.ok(mod);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrarGasto(@PathVariable Long id) {
        gastosIndividualesService.borrarGastoIndividual(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<GastosIndividuales>> obtenerTodos() {
        return ResponseEntity.ok(gastosIndividualesService.obtenerTodosLosGastosIndividuales());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GastosIndividuales> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(gastosIndividualesService.obtenerGastoIndividualPorId(id));
    }

    @GetMapping("/anio/{anio}")
    public ResponseEntity<List<GastosIndividuales>> obtenerPorAnio(@PathVariable int anio) {
        return ResponseEntity.ok(gastosIndividualesService.obtenerGastosIndividualesPorAnio(anio));
    }

    @GetMapping("/anio/{anio}/mes/{mes}")
    public ResponseEntity<List<GastosIndividuales>> obtenerPorAnioYMes(@PathVariable int anio, @PathVariable int mes) {
        return ResponseEntity.ok(gastosIndividualesService.obtenerGastosIndividualesPorAnioYMes(anio, mes));
    }
}

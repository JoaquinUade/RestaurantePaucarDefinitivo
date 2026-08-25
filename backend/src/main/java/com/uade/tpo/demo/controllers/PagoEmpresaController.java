package com.uade.tpo.demo.controllers;

import com.uade.tpo.demo.entity.PagoEmpresa;
import com.uade.tpo.demo.entity.TipoPeriodicidad;
import com.uade.tpo.demo.service.PagoEmpresaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pagos-empresa")
public class PagoEmpresaController {

    @Autowired
    private PagoEmpresaService pagoEmpresaService;

    // Crear pago
    @PostMapping
    public ResponseEntity<PagoEmpresa> crearPago(@RequestBody PagoEmpresa pagoEmpresa) {
        PagoEmpresa nuevoPago = pagoEmpresaService.crearPagoEmpresa(pagoEmpresa);
        return ResponseEntity.ok(nuevoPago);
    }

    // Obtener un pago por ID
    @GetMapping("/{id}")
    public ResponseEntity<PagoEmpresa> obtenerPagoPorId(@PathVariable Long id) {
        return pagoEmpresaService.obtenerPagoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener todos los pagos
    @GetMapping
    public ResponseEntity<List<PagoEmpresa>> obtenerTodos() {
        return ResponseEntity.ok(pagoEmpresaService.obtenerTodosPagos());
    }

    // Obtener pagos de una empresa
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<PagoEmpresa>> obtenerPorEmpresa(
            @PathVariable Long empresaId) {

        return ResponseEntity.ok(
                pagoEmpresaService.obtenerPagosPorEmpresa(empresaId));
    }

    // Obtener pagos por mes y año
    @GetMapping("/empresa/{empresaId}/mes")
    public ResponseEntity<List<PagoEmpresa>> obtenerPorMes(
            @PathVariable Long empresaId,
            @RequestParam Integer mes,
            @RequestParam Integer anio) {

        return ResponseEntity.ok(
                pagoEmpresaService.obtenerPagosPorMes(empresaId, mes, anio));
    }

    // Obtener pagos por tipo, mes y año
    @GetMapping("/empresa/{empresaId}/tipo")
    public ResponseEntity<List<PagoEmpresa>> obtenerPorTipo(
            @PathVariable Long empresaId,
            @RequestParam TipoPeriodicidad tipoPeriodicidad,
            @RequestParam Integer mes,
            @RequestParam Integer anio) {

        return ResponseEntity.ok(
                pagoEmpresaService.obtenerPagosPorTipoYMes(
                        empresaId,
                        tipoPeriodicidad,
                        mes,
                        anio));
    }

    // Modificar pago
    @PutMapping("/{id}")
    public ResponseEntity<PagoEmpresa> modificarPago(
            @PathVariable Long id,
            @RequestBody PagoEmpresa pagoEmpresa) {

        PagoEmpresa actualizado =
                pagoEmpresaService.modificarPagoEmpresa(id, pagoEmpresa);

        return ResponseEntity.ok(actualizado);
    }

    // Eliminar pago
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPago(@PathVariable Long id) {
        pagoEmpresaService.eliminarPago(id);
        return ResponseEntity.noContent().build();
    }

    // Verificar si se puede crear un pago
    @GetMapping("/validar")
    public ResponseEntity<Boolean> puedeCrearPago(
            @RequestParam Long empresaId,
            @RequestParam TipoPeriodicidad tipoPeriodicidad,
            @RequestParam Integer mes,
            @RequestParam Integer anio) {

        return ResponseEntity.ok(
                pagoEmpresaService.puedeCrearPago(
                        empresaId,
                        tipoPeriodicidad,
                        mes,
                        anio));
    }
}

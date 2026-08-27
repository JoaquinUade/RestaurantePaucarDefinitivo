package com.uade.tpo.demo.controllers;

import com.uade.tpo.demo.entity.PagoParcial;
import com.uade.tpo.demo.entity.dto.PagoParcialRequest;
import com.uade.tpo.demo.service.PagoParcialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos-parciales")
public class PagoParcialController {

    @Autowired
    private PagoParcialService pagoParcialService;

    @PostMapping
    public ResponseEntity<PagoParcial> crearPagoParcial(@RequestBody PagoParcialRequest request) {
        PagoParcial pago = pagoParcialService.registrarPagoParcial(request);
        return ResponseEntity.ok(pago);
    }

    @GetMapping
    public ResponseEntity<List<PagoParcial>> obtenerTodos() {
        return ResponseEntity.ok(pagoParcialService.obtenerTodos());
    }
}
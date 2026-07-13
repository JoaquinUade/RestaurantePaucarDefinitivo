package com.uade.tpo.demo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.HistorialStock;
import com.uade.tpo.demo.repository.HistorialStockRepository;

@RestController
@RequestMapping("/api/historial-stock")
public class HistorialStockController {

    @Autowired
    private HistorialStockRepository historialStockRepository;

    @GetMapping
    public ResponseEntity<List<HistorialStock>> obtenerHistorial() {

        return ResponseEntity.ok(
                historialStockRepository.findAll());
    }
}
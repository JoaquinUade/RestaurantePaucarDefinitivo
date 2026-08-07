package com.uade.tpo.demo.repository;

import com.uade.tpo.demo.entity.HistorialStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HistorialStockRepository
        extends JpaRepository<HistorialStock, Long> {

    List<HistorialStock> findByStock_IdStockOrderByFechaAsc(
            Long idStock);

    Optional<HistorialStock> findByStock_IdStockAndFecha(
            Long idStock,
            LocalDate fecha);

    List<HistorialStock> findByFechaBetweenOrderByFechaAsc(
            LocalDate desde,
            LocalDate hasta);
}

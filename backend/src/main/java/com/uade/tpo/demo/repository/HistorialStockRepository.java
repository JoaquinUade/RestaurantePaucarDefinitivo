package com.uade.tpo.demo.repository;

import com.uade.tpo.demo.entity.HistorialStock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistorialStockRepository
        extends JpaRepository<HistorialStock, Long> {
             List<HistorialStock> findByStock_IdStockOrderByFechaAsc(Long idStock);
}
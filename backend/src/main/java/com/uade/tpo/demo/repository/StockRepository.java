package com.uade.tpo.demo.repository;

import com.uade.tpo.demo.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByNombreProductoIgnoreCase(String nombreProducto);

    @Query("SELECT s FROM Stock s WHERE s.cantidad <= s.stockMinimo")
    List<Stock> findStockBajoMinimo();
}

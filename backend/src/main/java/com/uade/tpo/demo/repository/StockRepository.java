package com.uade.tpo.demo.repository;

import com.uade.tpo.demo.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    List<Stock> findByCategoriaGastoVariable_IdCategoria(
            Long idCategoria);

@Query("""
SELECT s
FROM Stock s
WHERE s.stockMinimo <= 0
""")
    List<Stock> findStockBajoMinimo();
}

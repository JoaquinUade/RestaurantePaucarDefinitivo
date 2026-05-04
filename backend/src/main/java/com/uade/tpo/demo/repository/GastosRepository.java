package com.uade.tpo.demo.repository;

import com.uade.tpo.demo.entity.Gastos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GastosRepository extends JpaRepository<Gastos, Long> {

    @Query(value = "SELECT * FROM gastos WHERE YEAR(fecha) = :anio AND MONTH(fecha) = :mes", nativeQuery = true)
    List<Gastos> findByAnioAndMes(@Param("anio") int anio, @Param("mes") int mes);

    @Query(value = "SELECT * FROM gastos WHERE YEAR(fecha) = :anio", nativeQuery = true)
    List<Gastos> findByAnio(@Param("anio") int anio);
}

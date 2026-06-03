package com.uade.tpo.demo.repository;

import com.uade.tpo.demo.entity.GastosFijos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GastosFijosRepository extends JpaRepository<GastosFijos, Long> {

    @Query(value = "SELECT * FROM gastos_fijos WHERE YEAR(fecha) = :anio AND MONTH(fecha) = :mes", nativeQuery = true)
    List<GastosFijos> findByAnioAndMes(@Param("anio") int anio, @Param("mes") int mes);

    @Query(value = "SELECT * FROM gastos_fijos WHERE YEAR(fecha) = :anio", nativeQuery = true)
    List<GastosFijos> findByAnio(@Param("anio") int anio);
}

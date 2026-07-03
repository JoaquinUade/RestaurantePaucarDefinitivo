package com.uade.tpo.demo.repository;

import com.uade.tpo.demo.entity.GastosVariables;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GastosVariablesRepository extends JpaRepository<GastosVariables, Long> {
	@Query("SELECT g FROM GastosVariables g " +
			"WHERE g.fecha IS NOT NULL " +
			"AND FUNCTION('YEAR', g.fecha) = :anio " +
			"AND FUNCTION('MONTH', g.fecha) = :mes " +
			"AND (g.cargadoEnStock = false OR g.cargadoEnStock IS NULL)")
	List<GastosVariables> findNoCargadosEnStockPorAnioYMes(@Param("anio") int anio, @Param("mes") int mes);
}

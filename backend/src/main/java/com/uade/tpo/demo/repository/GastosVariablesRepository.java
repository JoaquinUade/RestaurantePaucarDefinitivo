package com.uade.tpo.demo.repository;

import com.uade.tpo.demo.entity.GastosVariables;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GastosVariablesRepository extends JpaRepository<GastosVariables, Long> {
}

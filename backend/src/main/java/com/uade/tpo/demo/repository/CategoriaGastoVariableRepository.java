package com.uade.tpo.demo.repository;

import com.uade.tpo.demo.entity.CategoriaGastoVariable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaGastoVariableRepository extends JpaRepository<CategoriaGastoVariable, Long> {
}

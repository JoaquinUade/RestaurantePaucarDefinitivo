package com.uade.tpo.demo.repository;

import com.uade.tpo.demo.entity.GastosIndividuales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GastosIndividualesRepository extends JpaRepository<GastosIndividuales, Long> {
}

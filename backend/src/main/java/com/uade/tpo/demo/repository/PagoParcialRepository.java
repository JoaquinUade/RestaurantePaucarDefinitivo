package com.uade.tpo.demo.repository;

import com.uade.tpo.demo.entity.PagoParcial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoParcialRepository extends JpaRepository<PagoParcial, Long> {
}
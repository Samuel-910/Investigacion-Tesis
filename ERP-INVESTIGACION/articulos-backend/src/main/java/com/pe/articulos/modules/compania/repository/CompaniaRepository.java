package com.pe.articulos.modules.compania.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pe.articulos.modules.compania.entity.Compania;

import com.pe.articulos.core.enums.EstadoGeneral;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface CompaniaRepository extends JpaRepository<Compania, Long> {
    Page<Compania> findByEstado(EstadoGeneral estado, Pageable pageable);
}

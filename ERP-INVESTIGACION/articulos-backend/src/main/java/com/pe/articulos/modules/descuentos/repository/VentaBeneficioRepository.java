package com.pe.articulos.modules.descuentos.repository;

import com.pe.articulos.modules.descuentos.entity.VentaBeneficio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface VentaBeneficioRepository extends JpaRepository<VentaBeneficio, Long> {
    Page<VentaBeneficio> findByVenta_IdVenta(Long idVenta, Pageable pageable);
}

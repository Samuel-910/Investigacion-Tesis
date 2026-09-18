package com.pe.articulos.modules.descuentos.repository;

import com.pe.articulos.modules.descuentos.entity.DescuentoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface DescuentoDetalleRepository extends JpaRepository<DescuentoDetalle, Long> {
    Page<DescuentoDetalle> findByDescuentoId(Long idDescuento, Pageable pageable);
}

package com.pe.articulos.modules.compras.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pe.articulos.modules.compras.entity.CronogramaPago;

import java.time.LocalDate;
import com.pe.articulos.core.enums.EstadoGeneral;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface CronogramaPagoRepository extends JpaRepository<CronogramaPago, Long> {
    Page<CronogramaPago> findByCompraId(Long idCompra, Pageable pageable);

    Page<CronogramaPago> findByFechaVencimientoBetweenAndEstado(LocalDate inicio, LocalDate fin, EstadoGeneral estado, Pageable pageable);
}

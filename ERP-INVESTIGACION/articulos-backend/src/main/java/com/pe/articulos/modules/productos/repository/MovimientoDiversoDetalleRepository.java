package com.pe.articulos.modules.productos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pe.articulos.modules.productos.entity.MovimientoDiversoDetalle;
import java.util.List;


public interface MovimientoDiversoDetalleRepository extends JpaRepository<MovimientoDiversoDetalle, Long> {

    List<MovimientoDiversoDetalle> findByMovimientoId(Long movimientoId);
}

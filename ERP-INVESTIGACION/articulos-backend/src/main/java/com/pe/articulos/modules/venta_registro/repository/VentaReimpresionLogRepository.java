package com.pe.articulos.modules.venta_registro.repository;

import com.pe.articulos.modules.venta_registro.entity.VentaReimpresionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaReimpresionLogRepository extends JpaRepository<VentaReimpresionLog, Long> {
    List<VentaReimpresionLog> findByVenta_IdVentaOrderByFechaReimpresionDesc(Long idVenta);
}

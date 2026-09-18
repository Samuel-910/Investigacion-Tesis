package com.pe.articulos.modules.proveedores.repository;

import com.pe.articulos.modules.proveedores.entity.CuentaProveedorMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CuentaProveedorMovimientoRepository extends JpaRepository<CuentaProveedorMovimiento, Long> {
    Page<CuentaProveedorMovimiento> findByCuentaIdOrderByFechaRegistroDesc(Long idCuenta, Pageable pageable);

    List<CuentaProveedorMovimiento> findByCuentaId(Long idCuenta);
}

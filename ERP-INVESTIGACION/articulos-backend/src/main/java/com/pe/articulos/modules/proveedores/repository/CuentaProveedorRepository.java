package com.pe.articulos.modules.proveedores.repository;

import com.pe.articulos.modules.proveedores.entity.CuentaProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface CuentaProveedorRepository extends JpaRepository<CuentaProveedor, Long> {
    Optional<CuentaProveedor> findByProveedorId(Long idProveedor);

    @Query("SELECT SUM(cp.saldoTotal) FROM CuentaProveedor cp")
    BigDecimal totalCuentasPorPagar();

    @Query("SELECT cp.proveedor.razonSocial, cp.saldoTotal "
            +
            "FROM CuentaProveedor cp WHERE cp.saldoTotal > 0 ORDER BY cp.saldoTotal DESC")
    List<Object[]> deudasPorProveedorRaw();

    List<CuentaProveedor> findAllBySaldoTotalGreaterThan(BigDecimal saldo);
}

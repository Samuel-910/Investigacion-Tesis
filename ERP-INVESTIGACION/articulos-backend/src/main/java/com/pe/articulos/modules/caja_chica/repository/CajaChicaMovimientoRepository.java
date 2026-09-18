package com.pe.articulos.modules.caja_chica.repository;

import com.pe.articulos.modules.caja_chica.entity.CajaChicaMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CajaChicaMovimientoRepository extends JpaRepository<CajaChicaMovimiento, Long> {
        Page<CajaChicaMovimiento> findByCajaChicaIdOrderByFechaDesc(Long cajaChicaId, Pageable pageable);

        List<CajaChicaMovimiento> findByCajaChicaId(Long cajaChicaId);

        @Query("SELECT m.fecha, SUM(m.monto) " +
                        "FROM CajaChicaMovimiento m WHERE m.cajaChica.idSucursal = :idSucursal AND m.tipo = 'INGRESO' "
                        +
                        "GROUP BY m.fecha ORDER BY m.fecha")
        List<Object[]> ingresosDiariosRaw(@Param("idSucursal") Long idSucursal);

        @Query("SELECT m.fecha, SUM(m.monto) " +
                        "FROM CajaChicaMovimiento m WHERE m.cajaChica.idSucursal = :idSucursal AND m.tipo = 'EGRESO' " +
                        "GROUP BY m.fecha ORDER BY m.fecha")
        List<Object[]> egresosDiariosRaw(@Param("idSucursal") Long idSucursal);

        @Query("SELECT COALESCE(m.descripcion, 'Otro'), SUM(m.monto) " +
                        "FROM CajaChicaMovimiento m WHERE m.cajaChica.idSucursal = :idSucursal AND m.tipo = 'EGRESO' " +
                        "GROUP BY m.descripcion ORDER BY SUM(m.monto) DESC")
        Page<Object[]> gastosPorConceptoRaw(@Param("idSucursal") Long idSucursal, Pageable pageable);


        @Query("SELECT m.metodoPago.descripcion, SUM(m.monto) " +
                        "FROM CajaChicaMovimiento m WHERE m.cajaChica.idSucursal = :idSucursal " +
                        "GROUP BY m.metodoPago.descripcion")
        List<Object[]> movimientosPorMetodoPagoRaw(@Param("idSucursal") Long idSucursal);

        @Query("SELECT m FROM CajaChicaMovimiento m WHERE m.cajaChica.idSucursal = :idSucursal " +
                        "AND MONTH(m.fecha) = :mes AND YEAR(m.fecha) = :anio ORDER BY m.fecha DESC")
        List<CajaChicaMovimiento> findBySucursalAndPeriodo(@Param("idSucursal") Long idSucursal,
                        @Param("mes") Integer mes, @Param("anio") Integer anio);
}

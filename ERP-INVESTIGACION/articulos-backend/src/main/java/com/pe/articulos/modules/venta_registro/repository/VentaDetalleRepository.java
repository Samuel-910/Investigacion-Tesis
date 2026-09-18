package com.pe.articulos.modules.venta_registro.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.modules.venta_registro.entity.VentaDetalle;

@Repository
public interface VentaDetalleRepository extends JpaRepository<VentaDetalle, Long> {

        @Query("SELECT d FROM VentaDetalle d WHERE d.ventaRegistro.idVenta = :idVenta")
        List<VentaDetalle> findByIdVenta(@Param("idVenta") Long idVenta);

        List<VentaDetalle> findByIdCatalogo(Long idCatalogo);

        List<VentaDetalle> findByEstado(EstadoGeneral estado);

        List<VentaDetalle> findByIdOrden(String idOrden);

        @Query("SELECT d FROM VentaDetalle d WHERE d.ventaRegistro.idSucursal = :idSucursal " +
                        "AND MONTH(d.ventaRegistro.fecha) = :mes AND YEAR(d.ventaRegistro.fecha) = :anio " +
                        "AND (d.descuento > 0 OR d.descuentoEsp > 0 OR d.montoDescuento > 0)")
        List<VentaDetalle> findWithDescuentos(@Param("idSucursal") Long idSucursal, @Param("mes") Integer mes,
                        @Param("anio") Integer anio);
}

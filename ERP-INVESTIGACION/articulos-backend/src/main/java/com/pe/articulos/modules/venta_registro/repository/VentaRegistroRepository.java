package com.pe.articulos.modules.venta_registro.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pe.articulos.modules.venta_registro.entity.VentaRegistro;
import com.pe.articulos.core.enums.EstadoGeneral;

@Repository
public interface VentaRegistroRepository
                extends JpaRepository<VentaRegistro, Long>, JpaSpecificationExecutor<VentaRegistro> {

        Page<VentaRegistro> findByEstado(EstadoGeneral estado, Pageable pageable);

        Page<VentaRegistro> findByPunto(Long punto, Pageable pageable);

        Page<VentaRegistro> findByEstadoAndPunto(EstadoGeneral estado, Long punto, Pageable pageable);

        @Query("SELECT v FROM VentaRegistro v WHERE " +
                        "(:q IS NULL OR CAST(v.idPersonal AS string) LIKE CONCAT('%', :q, '%') OR " +
                        "UPPER(v.serie) LIKE UPPER(CONCAT('%', :q, '%')) OR " +
                        "CAST(v.numero AS string) LIKE CONCAT('%', :q, '%'))")
        Page<VentaRegistro> search(@Param("q") String q, Pageable pageable);

        @Query("SELECT DISTINCT v FROM VentaRegistro v JOIN v.detalles d " +
                        "WHERE d.idOrden IS NULL AND d.estado = com.pe.articulos.core.enums.EstadoGeneral.ACTIVO")
        List<VentaRegistro> findPendingSales();

        List<VentaRegistro> findByIdPersonal(Long idPersonal);

        List<VentaRegistro> findByIdPersonalUser(String idPersonalUser);

        List<VentaRegistro> findByFecha(LocalDate fecha);

        @Query("SELECT v FROM VentaRegistro v WHERE v.fecha BETWEEN :fechaInicio AND :fechaFin")
        List<VentaRegistro> findByFechaBetween(
                        @Param("fechaInicio") LocalDate fechaInicio,
                        @Param("fechaFin") LocalDate fechaFin);

        List<VentaRegistro> findByEstado(EstadoGeneral estado);

        Optional<VentaRegistro> findBySerieAndNumero(String serie, Integer numero);

        @Query("SELECT COUNT(v) FROM VentaRegistro v WHERE v.fecha = :fecha")
        Long countByFecha(@Param("fecha") LocalDate fecha);

        @Query("SELECT v FROM VentaRegistro v WHERE v.idPersonal = :idPersonal AND v.estado = com.pe.articulos.core.enums.EstadoGeneral.VIGENTE")
        List<VentaRegistro> findVentasVigentesByPaciente(@Param("idPersonal") Long idPersonal);

        @Query("SELECT DISTINCT v FROM VentaRegistro v LEFT JOIN FETCH v.detalles WHERE v.idVenta = :idVenta")
        Optional<VentaRegistro> findByIdVentaWithDetalles(@Param("idVenta") Long idVenta);

        Optional<VentaRegistro> findByIdOrdenProc(String idOrdenProc);


        @Query("SELECT SUM(v.total) FROM VentaRegistro v WHERE v.idSucursal = :idSucursal AND (:idPuntoVenta IS NULL OR v.punto = :idPuntoVenta) AND v.estado = com.pe.articulos.core.enums.EstadoGeneral.VIGENTE")
        java.math.BigDecimal totalVentas(@Param("idSucursal") Long idSucursal, @Param("idPuntoVenta") Long idPuntoVenta);

        @Query("SELECT MONTH(v.fecha), SUM(v.total) FROM VentaRegistro v " +
                        "WHERE v.idSucursal = :idSucursal AND v.estado = com.pe.articulos.core.enums.EstadoGeneral.VIGENTE "
                        +
                        "AND YEAR(v.fecha) = YEAR(CURRENT_DATE) GROUP BY MONTH(v.fecha) ORDER BY MONTH(v.fecha)")
        List<Object[]> ventasMensualesRaw(@Param("idSucursal") Long idSucursal, @Param("idPuntoVenta") Long idPuntoVenta);

        @Query("SELECT COALESCE(d.glosa, d.descripcion, 'Sin nombre'), SUM(d.cantidad) " +
                        "FROM VentaRegistro v JOIN v.detalles d " +
                        "WHERE v.idSucursal = :idSucursal AND v.estado = com.pe.articulos.core.enums.EstadoGeneral.VIGENTE "
                        +
                        "GROUP BY COALESCE(d.glosa, d.descripcion, 'Sin nombre') ORDER BY SUM(d.cantidad) DESC")
        List<Object[]> topProductosVendidosRaw(@Param("idSucursal") Long idSucursal, @Param("idPuntoVenta") Long idPuntoVenta, Pageable pageable);

        @Query("SELECT v.idPersonal, SUM(v.total) FROM VentaRegistro v " +
                        "WHERE v.idSucursal = :idSucursal AND v.estado = com.pe.articulos.core.enums.EstadoGeneral.VIGENTE "
                        +
                        "GROUP BY v.idPersonal ORDER BY SUM(v.total) DESC")
        List<Object[]> ventasPorUsuarioRaw(@Param("idSucursal") Long idSucursal);

        @Query("SELECT v.fecha, SUM(v.total) FROM VentaRegistro v " +
                        "WHERE v.idSucursal = :idSucursal AND v.estado = com.pe.articulos.core.enums.EstadoGeneral.VIGENTE "
                        +
                        "AND v.fecha >= :fechaInicio GROUP BY v.fecha ORDER BY v.fecha")
        List<Object[]> ventasDiariasRaw(@Param("idSucursal") Long idSucursal,
                        @Param("fechaInicio") LocalDate fechaInicio);

        @Query("SELECT v.fecha, COUNT(v) FROM VentaRegistro v " +
                        "WHERE v.idSucursal = :idSucursal AND v.estado = com.pe.articulos.core.enums.EstadoGeneral.VIGENTE "
                        +
                        "AND v.fecha >= :fechaInicio GROUP BY v.fecha ORDER BY v.fecha")
        List<Object[]> cantidadVentasDiariasRaw(@Param("idSucursal") Long idSucursal,
                        @Param("fechaInicio") LocalDate fechaInicio);

        @Query("SELECT SUM(d.total - (d.cantidad * COALESCE(p.precioCompra, 0))) " +
                        "FROM VentaRegistro v JOIN v.detalles d JOIN Producto p ON d.idArticulo = p.idProducto " +
                        "WHERE v.idSucursal = :idSucursal AND (:idPuntoVenta IS NULL OR v.punto = :idPuntoVenta) AND v.estado = com.pe.articulos.core.enums.EstadoGeneral.VIGENTE")
        java.math.BigDecimal utilidadNetaVentas(@Param("idSucursal") Long idSucursal);

        @Query("SELECT AVG(v.total) FROM VentaRegistro v WHERE v.idSucursal = :idSucursal AND (:idPuntoVenta IS NULL OR v.punto = :idPuntoVenta) AND v.estado = com.pe.articulos.core.enums.EstadoGeneral.VIGENTE")
        java.math.BigDecimal ticketPromedioVentas(@Param("idSucursal") Long idSucursal);

        @Query("SELECT SUM(v.descuento) FROM VentaRegistro v WHERE v.idSucursal = :idSucursal AND (:idPuntoVenta IS NULL OR v.punto = :idPuntoVenta) AND v.estado = com.pe.articulos.core.enums.EstadoGeneral.VIGENTE")
        java.math.BigDecimal descuentosTotalesVentas(@Param("idSucursal") Long idSucursal);

        @Query("SELECT (SUM(d.total - (d.cantidad * COALESCE(p.precioCompra, 0))) / NULLIF(SUM(d.total), 0)) * 100 " +
               "FROM VentaRegistro v JOIN v.detalles d JOIN Producto p ON d.idArticulo = p.idProducto " +
               "WHERE v.idSucursal = :idSucursal AND (:idPuntoVenta IS NULL OR v.punto = :idPuntoVenta) AND v.estado = com.pe.articulos.core.enums.EstadoGeneral.VIGENTE")
        java.math.BigDecimal rentabilidadVentas(@Param("idSucursal") Long idSucursal);

        @Query("SELECT COALESCE(d.glosa, d.descripcion, 'Sin nombre'), SUM(d.total - (d.cantidad * COALESCE(p.precioCompra, 0))) " +
               "FROM VentaRegistro v JOIN v.detalles d JOIN Producto p ON d.idArticulo = p.idProducto " +
               "WHERE v.idSucursal = :idSucursal AND v.estado = com.pe.articulos.core.enums.EstadoGeneral.VIGENTE " +
               "GROUP BY COALESCE(d.glosa, d.descripcion, 'Sin nombre') HAVING SUM(d.total - (d.cantidad * COALESCE(p.precioCompra, 0))) < 0 " +
               "ORDER BY SUM(d.total - (d.cantidad * COALESCE(p.precioCompra, 0))) ASC")
        List<Object[]> articulosConPerdidaRaw(@Param("idSucursal") Long idSucursal, Pageable pageable);


        @Query("SELECT COALESCE(v.metodoPago.descripcion, 'Otro'), SUM(v.total) FROM VentaRegistro v " +
                        "WHERE v.idSucursal = :idSucursal AND v.estado = com.pe.articulos.core.enums.EstadoGeneral.VIGENTE " +
                        "GROUP BY v.metodoPago.descripcion ORDER BY SUM(v.total) DESC")
        List<Object[]> ventasPorMetodoPagoRaw(@Param("idSucursal") Long idSucursal, Pageable pageable);

        @Query("SELECT COALESCE(v.nombrePac, 'Público General'), SUM(v.total) FROM VentaRegistro v " +
                        "WHERE v.idSucursal = :idSucursal AND v.estado = com.pe.articulos.core.enums.EstadoGeneral.VIGENTE " +
                        "GROUP BY v.nombrePac ORDER BY SUM(v.total) DESC")
        List<Object[]> ventasPorClienteRaw(@Param("idSucursal") Long idSucursal, Pageable pageable);

        @Query("SELECT DAYNAME(v.fecha), SUM(v.total) FROM VentaRegistro v " +
                        "WHERE v.idSucursal = :idSucursal AND v.estado = com.pe.articulos.core.enums.EstadoGeneral.VIGENTE " +
                        "GROUP BY DAYNAME(v.fecha) ORDER BY SUM(v.total) DESC")
        List<Object[]> ventasPorDiaSemanaRaw(@Param("idSucursal") Long idSucursal);

        @Query("SELECT COALESCE(p.laboratorio.descripcion, 'Sin Marca'), SUM(d.cantidad) FROM VentaRegistro v " +
                        "JOIN v.detalles d JOIN Producto p ON d.idArticulo = p.idProducto " +
                        "WHERE v.idSucursal = :idSucursal AND v.estado = com.pe.articulos.core.enums.EstadoGeneral.VIGENTE " +
                        "GROUP BY p.laboratorio.descripcion ORDER BY SUM(d.cantidad) DESC")
        List<Object[]> ventasPorMarcaRaw(@Param("idSucursal") Long idSucursal, Pageable pageable);
        @Query("SELECT v.tipoDoc, v.serie, MIN(v.numero), MAX(v.numero), COUNT(v), " +
                        "SUM(v.baseImp), SUM(v.igv), SUM(v.valorExo), SUM(v.valorInaf), SUM(v.total) " +
                        "FROM VentaRegistro v WHERE v.fecha BETWEEN :fechaInicio AND :fechaFin " +
                        "AND (:idSucursal IS NULL OR v.idSucursal = :idSucursal) " +
            "AND (:puntoId IS NULL OR v.punto = :puntoId) " +
                        "AND (:tipoDoc IS NULL OR :tipoDoc = '' OR v.tipoDoc = :tipoDoc) " +
                        "AND v.estado = com.pe.articulos.core.enums.EstadoGeneral.VIGENTE " +
                        "GROUP BY v.tipoDoc, v.serie")
        List<Object[]> obtenerCorrelatividadRaw(
                        @Param("fechaInicio") LocalDate fechaInicio,
                        @Param("fechaFin") LocalDate fechaFin,
                        @Param("idSucursal") Long idSucursal,
            @Param("puntoId") Long puntoId,
                        @Param("tipoDoc") String tipoDoc);

        @Query("SELECT v FROM VentaRegistro v WHERE v.idSucursal = :idSucursal " +
                        "AND v.fecha BETWEEN :inicio AND :fin ORDER BY v.fecha DESC")
        List<VentaRegistro> findSireVentas(@Param("idSucursal") Long idSucursal, @Param("inicio") LocalDate inicio,
                        @Param("fin") LocalDate fin);

        @Query("SELECT COUNT(v), AVG(v.total) FROM VentaRegistro v WHERE v.idSucursal = :idSucursal " +
                        "AND v.fecha BETWEEN :inicio AND :fin AND v.estado = com.pe.articulos.core.enums.EstadoGeneral.VIGENTE")
        Object[] getStatsBasics(@Param("idSucursal") Long idSucursal, @Param("inicio") LocalDate inicio,
                        @Param("fin") LocalDate fin);

        @Query("SELECT COUNT(DISTINCT v.idPersonal) FROM VentaRegistro v WHERE v.idSucursal = :idSucursal " +
                        "AND v.fecha BETWEEN :inicio AND :fin")
        Long countNuevosClientes(@Param("idSucursal") Long idSucursal, @Param("inicio") LocalDate inicio,
                        @Param("fin") LocalDate fin);

        @Query("SELECT COUNT(v) FROM VentaRegistro v WHERE v.idSucursal = :idSucursal " +
                        "AND v.fecha BETWEEN :inicio AND :fin AND v.estado = com.pe.articulos.core.enums.EstadoGeneral.ANULADO")
        Long countAnulaciones(@Param("idSucursal") Long idSucursal, @Param("inicio") LocalDate inicio,
                        @Param("fin") LocalDate fin);

        @Query("SELECT v FROM VentaRegistro v WHERE v.fecha BETWEEN :fechaInicio AND :fechaFin " +
                        "AND (:idSucursal IS NULL OR v.idSucursal = :idSucursal) " +
            "AND (:puntoId IS NULL OR v.punto = :puntoId) " +
                        "AND v.tipoDoc = :tipoDoc " +
                        "AND v.serie = :serie " +
                        "AND v.estado = com.pe.articulos.core.enums.EstadoGeneral.VIGENTE " +
                        "ORDER BY v.numero ASC")
        List<VentaRegistro> findDetalleCorrelatividad(
                        @Param("fechaInicio") LocalDate fechaInicio,
                        @Param("fechaFin") LocalDate fechaFin,
                        @Param("idSucursal") Long idSucursal,
            @Param("puntoId") Long puntoId,
                        @Param("tipoDoc") String tipoDoc,
                        @Param("serie") String serie);
}

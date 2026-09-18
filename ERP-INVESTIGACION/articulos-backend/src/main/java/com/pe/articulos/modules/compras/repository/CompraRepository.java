
package com.pe.articulos.modules.compras.repository;

import com.pe.articulos.modules.compras.entity.Compra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

        @Query("SELECT c FROM Compra c WHERE c.idSucursal = :idSucursal AND c.estado != com.pe.articulos.core.enums.EstadoGeneral.REGISTRADO")
        Page<Compra> listarComprasFinales(@Param("idSucursal") Long idSucursal, Pageable pageable);

        @Query("SELECT c FROM Compra c WHERE c.idSucursal = :idSucursal AND c.estado <> com.pe.articulos.core.enums.EstadoGeneral.ANULADO")
        Page<Compra> listarActivas(@Param("idSucursal") Long idSucursal, Pageable pageable);

        @Query("SELECT c FROM Compra c " +
                        "WHERE c.idSucursal = :idSucursal " +
                        "AND (:idProveedor IS NULL OR c.proveedor.id = :idProveedor) " +
                        "AND (:fechaInicio IS NULL OR c.fechaEmision >= :fechaInicio) " +
                        "AND (:fechaFin IS NULL OR c.fechaEmision <= :fechaFin) " +
                        "AND (:estado IS NULL OR c.estado = :estado) " +
                        "AND (c.estado != com.pe.articulos.core.enums.EstadoGeneral.REGISTRADO OR :estado = com.pe.articulos.core.enums.EstadoGeneral.REGISTRADO)")
        Page<Compra> buscar(@Param("idSucursal") Long idSucursal,
                        @Param("idProveedor") Long idProveedor,
                        @Param("fechaInicio") LocalDate fechaInicio,
                        @Param("fechaFin") LocalDate fechaFin,
                        @Param("estado") com.pe.articulos.core.enums.EstadoGeneral estado,
                        Pageable pageable);

        @Query("SELECT SUM(c.totalPagar) FROM Compra c WHERE c.idSucursal = :idSucursal AND c.estado <> com.pe.articulos.core.enums.EstadoGeneral.ANULADO")
        java.math.BigDecimal totalCompras(@Param("idSucursal") Long idSucursal);

        @Query("SELECT COUNT(c) FROM Compra c WHERE c.idSucursal = :idSucursal AND c.estado <> com.pe.articulos.core.enums.EstadoGeneral.ANULADO")
        Long cantidadCompras(@Param("idSucursal") Long idSucursal);

        @Query("SELECT MONTH(c.fechaEmision), SUM(c.totalPagar) FROM Compra c " +
                        "WHERE c.idSucursal = :idSucursal AND c.estado != com.pe.articulos.core.enums.EstadoGeneral.ANULADO "
                        +
                        "AND YEAR(c.fechaEmision) = YEAR(CURRENT_DATE) " +
                        "GROUP BY MONTH(c.fechaEmision) ORDER BY MONTH(c.fechaEmision)")
        Page<Object[]> comprasMensualesRaw(@Param("idSucursal") Long idSucursal, Pageable pageable);

        @Query("SELECT c FROM Compra c WHERE c.idSucursal = :idSucursal " +
                        "AND c.fechaEmision BETWEEN :inicio AND :fin ORDER BY c.fechaEmision DESC")
        Page<Compra> findSireCompras(@Param("idSucursal") Long idSucursal, @Param("inicio") LocalDate inicio,
                        @Param("fin") LocalDate fin, Pageable pageable);

        @Query("SELECT COUNT(c), AVG(c.totalPagar) FROM Compra c WHERE c.idSucursal = :idSucursal " +
                        "AND c.fechaEmision BETWEEN :inicio AND :fin AND c.estado != com.pe.articulos.core.enums.EstadoGeneral.ANULADO")
        Object[] getStatsBasics(@Param("idSucursal") Long idSucursal, @Param("inicio") LocalDate inicio,
                        @Param("fin") LocalDate fin);

        @Query("SELECT COUNT(DISTINCT c.proveedor.id) FROM Compra c WHERE c.idSucursal = :idSucursal " +
                        "AND c.fechaEmision BETWEEN :inicio AND :fin")
        Long countNuevosProveedores(@Param("idSucursal") Long idSucursal, @Param("inicio") LocalDate inicio,
                        @Param("fin") LocalDate fin);

        @Query("SELECT COUNT(c) FROM Compra c WHERE c.idSucursal = :idSucursal " +
                        "AND c.fechaEmision BETWEEN :inicio AND :fin AND c.estado = com.pe.articulos.core.enums.EstadoGeneral.ANULADO")
        Long countAnulaciones(@Param("idSucursal") Long idSucursal, @Param("inicio") LocalDate inicio,
                        @Param("fin") LocalDate fin);

        @Query("SELECT c.proveedor.razonSocial, SUM(c.totalPagar) "
                        +
                        "FROM Compra c WHERE c.idSucursal = :idSucursal AND c.estado <> com.pe.articulos.core.enums.EstadoGeneral.ANULADO "
                        +
                        "GROUP BY c.proveedor.razonSocial ORDER BY SUM(c.totalPagar) DESC")
        Page<Object[]> comprasPorProveedorRaw(@Param("idSucursal") Long idSucursal, Pageable pageable);

        @Query("SELECT c.fechaEmision, SUM(c.totalPagar) "
                        +
                        "FROM Compra c WHERE c.idSucursal = :idSucursal AND c.estado <> com.pe.articulos.core.enums.EstadoGeneral.ANULADO "
                        +
                        "AND c.fechaEmision >= :fechaInicio GROUP BY c.fechaEmision ORDER BY c.fechaEmision")
        Page<Object[]> comprasDiariasRaw(@Param("idSucursal") Long idSucursal,
                        @Param("fechaInicio") LocalDate fechaInicio, Pageable pageable);

        @Query("SELECT c.usuarioCreacion, SUM(c.totalPagar) FROM Compra c " +
                        "WHERE c.idSucursal = :idSucursal AND c.estado <> com.pe.articulos.core.enums.EstadoGeneral.ANULADO " +
                        "GROUP BY c.usuarioCreacion ORDER BY SUM(c.totalPagar) DESC")
        Page<Object[]> comprasPorUsuarioRaw(@Param("idSucursal") Long idSucursal, Pageable pageable);

        @Query("SELECT CAST(c.idSucursal AS string), SUM(c.totalPagar) FROM Compra c " +
                        "WHERE c.estado <> com.pe.articulos.core.enums.EstadoGeneral.ANULADO " +
                        "GROUP BY c.idSucursal ORDER BY SUM(c.totalPagar) DESC")
        Page<Object[]> comprasPorSucursalRaw(Pageable pageable);

        @Query("SELECT MONTH(c.fechaEmision), COUNT(c) FROM Compra c " +
                        "WHERE c.idSucursal = :idSucursal AND c.estado != com.pe.articulos.core.enums.EstadoGeneral.ANULADO " +
                        "AND YEAR(c.fechaEmision) = YEAR(CURRENT_DATE) " +
                        "GROUP BY MONTH(c.fechaEmision) ORDER BY MONTH(c.fechaEmision)")
        Page<Object[]> cantidadComprasMensualesRaw(@Param("idSucursal") Long idSucursal, Pageable pageable);

        @Query("SELECT AVG(c.totalPagar) FROM Compra c WHERE c.idSucursal = :idSucursal AND c.estado <> com.pe.articulos.core.enums.EstadoGeneral.ANULADO")
        java.math.BigDecimal ticketCompraPromedioRaw(@Param("idSucursal") Long idSucursal);

        @Query("SELECT COALESCE(n.nombre, 'Sin Categoria'), SUM(d.valorVenta) FROM Compra c " +
                        "JOIN c.detalles d JOIN d.producto p LEFT JOIN p.nivel n " +
                        "WHERE c.idSucursal = :idSucursal AND c.estado <> com.pe.articulos.core.enums.EstadoGeneral.ANULADO " +
                        "GROUP BY n.nombre ORDER BY SUM(d.valorVenta) DESC")
        Page<Object[]> comprasPorCategoriaRaw(@Param("idSucursal") Long idSucursal, Pageable pageable);

        // ==== MÉTRICAS DIMENSIONALES PARA COMPRAS_CANTIDAD ====
        // Dimensión: Proveedor (Cantidad de compras)
        @Query("SELECT c.proveedor.razonSocial, COUNT(c) FROM Compra c " +
                        "WHERE c.idSucursal = :idSucursal AND c.estado <> com.pe.articulos.core.enums.EstadoGeneral.ANULADO " +
                        "GROUP BY c.proveedor.razonSocial ORDER BY COUNT(c) DESC")
        Page<Object[]> conteoComprasPorProveedorRaw(@Param("idSucursal") Long idSucursal, Pageable pageable);

        // Dimensión: Usuario/Cajero (Cantidad de compras)
        @Query("SELECT c.usuarioCreacion, COUNT(c) FROM Compra c " +
                        "WHERE c.idSucursal = :idSucursal AND c.estado <> com.pe.articulos.core.enums.EstadoGeneral.ANULADO " +
                        "GROUP BY c.usuarioCreacion ORDER BY COUNT(c) DESC")
        Page<Object[]> cantidadComprasPorUsuarioRaw(@Param("idSucursal") Long idSucursal, Pageable pageable);

        @Query("SELECT CAST(c.idSucursal AS string), COUNT(c) FROM Compra c " +
                        "WHERE c.estado <> com.pe.articulos.core.enums.EstadoGeneral.ANULADO " +
                        "GROUP BY c.idSucursal ORDER BY COUNT(c) DESC")
        Page<Object[]> cantidadComprasPorSucursalRaw(Pageable pageable);

        @Query(value = "SELECT DISTINCT c.nombreGrupo FROM Compra c WHERE c.idSucursal = :idSucursal AND c.nombreGrupo IS NOT NULL AND c.nombreGrupo <> '' AND c.estado = com.pe.articulos.core.enums.EstadoGeneral.REGISTRADO ORDER BY c.nombreGrupo", countQuery = "SELECT COUNT(DISTINCT c.nombreGrupo) FROM Compra c WHERE c.idSucursal = :idSucursal AND c.nombreGrupo IS NOT NULL AND c.nombreGrupo <> '' AND c.estado = com.pe.articulos.core.enums.EstadoGeneral.REGISTRADO")
        Page<String> findUniqueGroupNames(@Param("idSucursal") Long idSucursal, Pageable pageable);

        Page<Compra> findByNombreGrupoAndIdSucursal(String nombreGrupo, Long idSucursal, Pageable pageable);

        @Query("SELECT c FROM Compra c WHERE c.fechaVencimiento IS NOT NULL AND c.fechaVencimiento BETWEEN :fechaInicio AND :fechaFin AND c.estado = :estado")
        List<Compra> findComprasPorVencerGlobal(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin, @Param("estado") com.pe.articulos.core.enums.EstadoGeneral estado);
}

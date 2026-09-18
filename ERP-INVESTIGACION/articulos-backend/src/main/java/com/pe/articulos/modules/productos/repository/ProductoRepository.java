package com.pe.articulos.modules.productos.repository;

import com.pe.articulos.modules.productos.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

     @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
     @org.springframework.data.jpa.repository.Query("SELECT p FROM Producto p WHERE p.idProducto = :idProducto")
     Optional<Producto> findByIdForUpdate(@org.springframework.data.repository.query.Param("idProducto") Long idProducto);


     // CORRECCIÓN: Se eliminó AND vd.idCatalogo <> '' y se cambió String idSucursal
     // por Long
     @Query("SELECT vd.idCatalogo FROM VentaDetalle vd JOIN vd.ventaRegistro v " +
               "WHERE v.idSucursal = :idSucursal AND v.estado = com.pe.articulos.core.enums.EstadoGeneral.VIGENTE " +
               "AND vd.idCatalogo IS NOT NULL " +
               "GROUP BY vd.idCatalogo ORDER BY SUM(vd.cantidad) DESC")
     List<Long> findTopSellingCatalogoIds(@Param("idSucursal") Long idSucursal, Pageable pageable);

     @Query("SELECT DISTINCT p.usuarioCrea FROM Producto p WHERE p.idSucursal = :idSucursal AND p.usuarioCrea IS NOT NULL")
     List<String> findDistinctUsuarioCreaByIdSucursal(@Param("idSucursal") Long idSucursal);

     List<Producto> findByIdSucursal(Long idSucursal);

     Page<Producto> findByIdSucursal(Long idSucursal, Pageable pageable);

     @Query("SELECT p FROM Producto p WHERE p.idSucursal = :idSucursal AND (:soloVenta = false OR (p.precioVentaUnitario IS NOT NULL AND p.precioVentaUnitario > 0 AND (p.fechaVencimiento IS NULL OR p.fechaVencimiento >= CURRENT_DATE)))")
     Page<Producto> findByIdSucursalWithFilter(@Param("idSucursal") Long idSucursal, @Param("soloVenta") boolean soloVenta, Pageable pageable);

     @Query("SELECT p FROM Producto p WHERE p.id IN (SELECT MIN(p2.id) FROM Producto p2 WHERE p2.idSucursal = :idSucursal AND p2.precioVentaUnitario > 0 AND (p2.fechaVencimiento IS NULL OR p2.fechaVencimiento >= CURRENT_DATE) GROUP BY p2.catalogo.id)")
     Page<Producto> findDistinctCatalogosForDescuentos(@Param("idSucursal") Long idSucursal, Pageable pageable);

     Optional<Producto> findByIdProductoAndIdSucursal(Long idProducto, Long idSucursal);

     Optional<Producto> findFirstByIdCatalogoAndIdSucursalAndNroLote(Long idCatalogo, Long idSucursal, String nroLote);

     Optional<Producto> findFirstByIdCatalogoAndIdSucursalAndIdAlmacenAndNroLote(Long idCatalogo, Long idSucursal,
               Long idAlmacen, String nroLote);

     @Query("SELECT p FROM Producto p WHERE p.idCatalogo = :idCatalogo AND p.idSucursal = :idSucursal " +
               "AND ((p.idAlmacen IS NULL AND :idAlmacen IS NULL) OR (p.idAlmacen = :idAlmacen)) " +
               "AND TRIM(BOTH FROM p.nroLote) = TRIM(BOTH FROM :nroLote) " +
               "AND p.fechaVencimiento = :fechaVenc " +
               "AND p.precioCompra = :costo")
     List<Producto> findExactLotList(
               @Param("idCatalogo") Long idCatalogo,
               @Param("idSucursal") Long idSucursal,
               @Param("idAlmacen") Long idAlmacen,
               @Param("nroLote") String nroLote,
               @Param("fechaVenc") LocalDate fechaVenc,
               @Param("costo") BigDecimal costo);

     default Optional<Producto> findExactLot(Long idCatalogo, Long idSucursal, Long idAlmacen, String nroLote,
               LocalDate fechaVenc, BigDecimal costo) {
          return findExactLotList(idCatalogo, idSucursal, idAlmacen, nroLote, fechaVenc, costo).stream().findFirst();
     }

     List<Producto> findByIdCatalogo(Long idCatalogo);

     List<Producto> findByIdCatalogoAndIdSucursal(Long idCatalogo, Long idSucursal);

     Optional<Producto> findFirstByIdCatalogoAndIdSucursalOrderByFechaRegDesc(Long idCatalogo, Long idSucursal);

     Optional<Producto> findFirstByIdCatalogoAndIdSucursalAndFechaVencimientoIsNotNullOrderByFechaVencimientoAsc(
               Long idCatalogo, Long idSucursal);

     @Query("SELECT p FROM Producto p WHERE p.idSucursal = :idSucursal AND " +
               "p.fechaVencimiento IS NOT NULL AND " +
               "p.fechaVencimiento BETWEEN :fechaInicio AND :fechaFin")
     List<Producto> findProductosProximosAVencer(@Param("idSucursal") Long idSucursal,
               @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

     @Query("SELECT p FROM Producto p WHERE p.idSucursal = :idSucursal AND p.fechaVencimiento < :fechaActual")
     List<Producto> findProductosVencidos(@Param("idSucursal") Long idSucursal,
               @Param("fechaActual") LocalDate fechaActual);

     @Query(value = "SELECT * FROM productos p WHERE p.id_sucursal = :idSucursal AND p.fecha_venc IS NOT NULL AND p.fecha_venc >= CAST(:fechaActual AS date) AND p.fecha_venc <= (CAST(:fechaActual AS date) + COALESCE(p.dias_alerta_vencimiento, 90))", nativeQuery = true)
     List<Producto> findProductosEnAlertaVencimientoPorSucursal(@Param("idSucursal") Long idSucursal, @Param("fechaActual") LocalDate fechaActual);

     @Query(value = "SELECT * FROM productos p WHERE p.fecha_venc IS NOT NULL AND p.fecha_venc >= CAST(:fechaActual AS date) AND p.fecha_venc <= (CAST(:fechaActual AS date) + COALESCE(p.dias_alerta_vencimiento, 90))", nativeQuery = true)
     List<Producto> findProductosEnAlertaVencimientoGlobal(@Param("fechaActual") LocalDate fechaActual);

     @Query("SELECT p FROM Producto p WHERE p.fechaVencimiento IS NOT NULL AND p.fechaVencimiento BETWEEN :fechaInicio AND :fechaFin")
     List<Producto> findProductosPorVencerGlobal(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

     @Query("SELECT p FROM Producto p WHERE p.stock <= p.stockMinimo AND p.stockMinimo > 0")
     List<Producto> findStockCriticoGlobal();

     @Query("SELECT p FROM Producto p WHERE p.idSucursal = :idSucursal AND p.stock <= p.stockMinimo AND p.stockMinimo > 0")
     List<Producto> findStockCriticoPorSucursal(@Param("idSucursal") Long idSucursal);

     @Query("SELECT p FROM Producto p WHERE p.stock = :stock")
     List<Producto> findProductosSinStock(@Param("stock") BigDecimal stock);

     @Query("SELECT p FROM Producto p JOIN p.catalogo c WHERE p.idSucursal = :idSucursal AND c.tipo = :tipo")
     Page<Producto> findBySucursalAndCatalogoTipo(@Param("idSucursal") Long idSucursal, @Param("tipo") com.pe.articulos.modules.catalogo.entity.Catalogo.Tipo tipo,
               Pageable pageable);

     @Query("SELECT p FROM Producto p JOIN p.catalogo c LEFT JOIN p.laboratorio l WHERE " +
               "p.idSucursal = :idSucursal AND (:soloVenta = false OR (p.precioVentaUnitario IS NOT NULL AND p.precioVentaUnitario > 0 AND (p.fechaVencimiento IS NULL OR p.fechaVencimiento >= CURRENT_DATE))) AND (" +
               "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
               "LOWER(c.codigo) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
               "LOWER(p.codigoBarra) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
               "LOWER(l.descripcion) LIKE LOWER(CONCAT('%', :q, '%'))" +
               ")")
     Page<Producto> searchBySucursal(@Param("idSucursal") Long idSucursal, @Param("q") String q, @Param("soloVenta") boolean soloVenta, Pageable pageable);

     @Query("SELECT COUNT(p) FROM Producto p WHERE p.idSucursal = :idSucursal AND p.stock <= p.stockMinimo AND p.stockMinimo > 0")
     java.math.BigDecimal contarStockCritico(@Param("idSucursal") Long idSucursal);

     @Query("SELECT COALESCE(l.descripcion, 'Sin Marca'), SUM(p.stock * p.precioCompra) FROM Producto p " +
               "LEFT JOIN p.laboratorio l WHERE p.idSucursal = :idSucursal " +
               "GROUP BY l.descripcion ORDER BY SUM(p.stock * p.precioCompra) DESC")
     Page<Object[]> inventarioPorMarcaRaw(@Param("idSucursal") Long idSucursal, Pageable pageable);


     @Query("SELECT p FROM Producto p JOIN p.catalogo c WHERE p.idSucursal = :idSucursal AND (:soloVenta = false OR (p.precioVentaUnitario IS NOT NULL AND p.precioVentaUnitario > 0 AND (p.fechaVencimiento IS NULL OR p.fechaVencimiento >= CURRENT_DATE))) AND LOWER(c.nombre) LIKE LOWER(CONCAT('%', :q, '%'))")
     Page<Producto> searchBySucursalAndNombre(@Param("idSucursal") Long idSucursal, @Param("q") String q, @Param("soloVenta") boolean soloVenta, Pageable pageable);

     @Query("SELECT p FROM Producto p JOIN p.catalogo c WHERE p.idSucursal = :idSucursal AND (:soloVenta = false OR (p.precioVentaUnitario IS NOT NULL AND p.precioVentaUnitario > 0 AND (p.fechaVencimiento IS NULL OR p.fechaVencimiento >= CURRENT_DATE))) AND (LOWER(c.codigo) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.codigoBarra) LIKE LOWER(CONCAT('%', :q, '%')))")
     Page<Producto> searchBySucursalAndCodigo(@Param("idSucursal") Long idSucursal, @Param("q") String q, @Param("soloVenta") boolean soloVenta, Pageable pageable);

     @Query("SELECT p FROM Producto p JOIN p.catalogo c LEFT JOIN p.laboratorio l WHERE p.idSucursal = :idSucursal AND (:soloVenta = false OR (p.precioVentaUnitario IS NOT NULL AND p.precioVentaUnitario > 0 AND (p.fechaVencimiento IS NULL OR p.fechaVencimiento >= CURRENT_DATE))) AND LOWER(l.descripcion) LIKE LOWER(CONCAT('%', :q, '%'))")
     Page<Producto> searchBySucursalAndLaboratorio(@Param("idSucursal") Long idSucursal, @Param("q") String q, @Param("soloVenta") boolean soloVenta, Pageable pageable);

     @Query("SELECT c.nombre, SUM(p.stock) FROM Producto p JOIN p.catalogo c WHERE p.idSucursal = :idSucursal GROUP BY c.nombre ORDER BY SUM(p.stock) DESC")
     List<Object[]> stockPorCategoriaRaw(@Param("idSucursal") Long idSucursal);

     @Query("SELECT c.nombre, SUM(p.stock * p.precioCompra) FROM Producto p JOIN p.catalogo c WHERE p.idSucursal = :idSucursal GROUP BY c.nombre ORDER BY SUM(p.stock * p.precioCompra) DESC")
     List<Object[]> valorInventarioPorCategoriaRaw(@Param("idSucursal") Long idSucursal);

     @Query("SELECT l.descripcion, COUNT(p) FROM Producto p LEFT JOIN p.laboratorio l WHERE p.idSucursal = :idSucursal AND p.fechaVencimiento < CURRENT_DATE GROUP BY l.descripcion")
     List<Object[]> productosVencidosPorLaboratorioRaw(@Param("idSucursal") Long idSucursal);
}
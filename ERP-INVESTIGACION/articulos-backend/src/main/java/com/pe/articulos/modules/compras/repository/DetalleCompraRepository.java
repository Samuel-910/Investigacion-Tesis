
package com.pe.articulos.modules.compras.repository;

import com.pe.articulos.modules.catalogo.entity.Catalogo;
import com.pe.articulos.modules.compras.entity.DetalleCompra;
import com.pe.articulos.modules.proveedores.entity.Proveedor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, Long> {
        Page<DetalleCompra> findByCompraId(Long idCompra, Pageable pageable);

        @Query("SELECT d FROM DetalleCompra d JOIN d.producto p " +
                        "WHERE (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :term, '%')) " +
                        "OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :term, '%'))) " +
                        "AND d.compra.idSucursal = :idSucursal " +
                        "AND (d.registradoEnAlmacen IS NULL OR d.registradoEnAlmacen = false) " +
                        "AND d.compra.estado <> com.pe.articulos.core.enums.EstadoGeneral.ANULADO")
        Page<DetalleCompra> buscarPorProducto(@Param("term") String term, @Param("idSucursal") Long idSucursal, Pageable pageable);

        @Query("SELECT MAX(d.precioUnitario) FROM DetalleCompra d WHERE d.producto.id = :idCatalogo")
        java.math.BigDecimal obtenerPrecioMaximo(@Param("idCatalogo") Long idCatalogo);

        @Query("SELECT d.compra.proveedor FROM DetalleCompra d WHERE d.producto.id = :idProducto AND d.lote = :lote AND d.compra.estado <> com.pe.articulos.core.enums.EstadoGeneral.ANULADO ORDER BY d.compra.fechaEmision DESC")
        Page<Proveedor> findProveedorByProductoAndLote(
                        @Param("idProducto") Long idProducto,
                        @Param("lote") String lote,
                        Pageable pageable);

        @Query("SELECT d FROM DetalleCompra d WHERE d.producto.id = :idCatalogo AND d.compra.estado <> com.pe.articulos.core.enums.EstadoGeneral.ANULADO AND d.compra.estado <> com.pe.articulos.core.enums.EstadoGeneral.ELIMINADO ORDER BY d.compra.fechaEmision DESC")
        Page<DetalleCompra> findLatestByProducto(@Param("idCatalogo") Long idCatalogo,
                        Pageable pageable);

        @Query("SELECT DISTINCT d.producto FROM DetalleCompra d WHERE d.compra.nombreGrupo = :nombreGrupo AND d.compra.idSucursal = :idSucursal AND d.compra.estado <> com.pe.articulos.core.enums.EstadoGeneral.ELIMINADO")
        Page<Catalogo> findUniqueProductsByGroupName(@Param("nombreGrupo") String nombreGrupo,
                        @Param("idSucursal") Long idSucursal, Pageable pageable);
}

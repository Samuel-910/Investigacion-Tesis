package com.pe.articulos.modules.kardex.repository;

import com.pe.articulos.modules.kardex.entity.Kardex;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KardexRepository extends JpaRepository<Kardex, String> {

        @Query(value = "SELECT * FROM kardex WHERE id_catalogo = :idCatalogo AND id_sucursal = :idSucursal ORDER BY fecha DESC, id_articulo_kardex DESC LIMIT 1", nativeQuery = true)
        Optional<Kardex> findLastMovement(@Param("idCatalogo") Long idCatalogo,
                        @Param("idSucursal") Long idSucursal);

        @Query(value = "SELECT * FROM kardex WHERE id_catalogo = :idCatalogo AND id_sucursal = :idSucursal AND nro_lote = :nroLote ORDER BY fecha DESC, id_articulo_kardex DESC LIMIT 1", nativeQuery = true)
        Optional<Kardex> findLastMovementByLot(@Param("idCatalogo") Long idCatalogo,
                        @Param("idSucursal") Long idSucursal,
                        @Param("nroLote") String nroLote);

        Page<Kardex> findByIdCatalogoAndIdSucursalOrderByFechaDescIdArticuloKardexDesc(
                        Long idCatalogo, Long idSucursal, Pageable pageable);

        Page<Kardex> findByIdCatalogoAndIdSucursalAndFechaBetweenOrderByFechaDescIdArticuloKardexDesc(
                        Long idCatalogo, Long idSucursal, LocalDateTime start, LocalDateTime end,
                        Pageable pageable);

        List<Kardex> findByIdCatalogoAndIdSucursalAndFechaBetweenOrderByFechaDescIdArticuloKardexDesc(
                        Long idCatalogo, Long idSucursal, LocalDateTime start, LocalDateTime end);

        List<Kardex> findByIdCatalogoAndIdSucursalOrderByFechaDescIdArticuloKardexDesc(
                        Long idCatalogo, Long idSucursal);

        @Query(value = "SELECT k.* FROM kardex k " +
                        "LEFT JOIN catalogo p ON p.id = k.id_catalogo " +
                        "LEFT JOIN inventario_clasificaciones c ON c.id = k.id_clasificacion " +
                        "WHERE k.id_sucursal = :idSucursal " +
                        "AND (CAST(:desde AS date) IS NULL OR k.fecha >= :desde) " +
                        "AND (CAST(:hasta AS date) IS NULL OR k.fecha <= :hasta) " +
                        "AND (CAST(:idClasificacion AS bigint) IS NULL OR k.id_clasificacion = :idClasificacion) " +
                        "AND (CAST(:signo AS varchar) IS NULL OR k.signo = :signo) " +
                        "AND (CAST(:nombre AS varchar) IS NULL OR " +
                        "    LOWER(COALESCE(k.num_doc, '')::text) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
                        "    OR LOWER(COALESCE(p.nombre, '')::text) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
                        "    OR LOWER(COALESCE(p.codigo, '')::text) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
                        "    OR LOWER(COALESCE(k.nro_lote, '')::text) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
                        "    OR LOWER(COALESCE(c.nombre, '')::text) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
                        "ORDER BY k.fecha DESC, k.id_articulo_kardex DESC", countQuery = "SELECT COUNT(*) FROM kardex k "
                                        +
                                        "LEFT JOIN catalogo p ON p.id = k.id_catalogo " +
                                        "LEFT JOIN inventario_clasificaciones c ON c.id = k.id_clasificacion " +
                                        "WHERE k.id_sucursal = :idSucursal " +
                                        "AND (CAST(:desde AS date) IS NULL OR k.fecha >= :desde) " +
                                        "AND (CAST(:hasta AS date) IS NULL OR k.fecha <= :hasta) " +
                                        "AND (CAST(:idClasificacion AS bigint) IS NULL OR k.id_clasificacion = :idClasificacion) "
                                        +
                                        "AND (CAST(:signo AS varchar) IS NULL OR k.signo = :signo) " +
                                        "AND (CAST(:nombre AS varchar) IS NULL OR " +
                                        "    LOWER(COALESCE(k.num_doc, '')::text) LIKE LOWER(CONCAT('%', :nombre, '%')) "
                                        +
                                        "    OR LOWER(COALESCE(p.nombre, '')::text) LIKE LOWER(CONCAT('%', :nombre, '%')) "
                                        +
                                        "    OR LOWER(COALESCE(p.codigo, '')::text) LIKE LOWER(CONCAT('%', :nombre, '%')) "
                                        +
                                        "    OR LOWER(COALESCE(k.nro_lote, '')::text) LIKE LOWER(CONCAT('%', :nombre, '%')) "
                                        +
                                        "    OR LOWER(COALESCE(c.nombre, '')::text) LIKE LOWER(CONCAT('%', :nombre, '%')))", nativeQuery = true)
        Page<Kardex> searchGlobal(
                        @Param("idSucursal") Long idSucursal,
                        @Param("desde") LocalDateTime desde,
                        @Param("hasta") LocalDateTime hasta,
                        @Param("signo") String signo,
                        @Param("nombre") String nombre,
                        @Param("idClasificacion") Long idClasificacion,
                        Pageable pageable);
}

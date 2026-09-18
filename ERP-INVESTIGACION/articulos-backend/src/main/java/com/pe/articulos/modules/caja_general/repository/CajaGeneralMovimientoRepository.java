package com.pe.articulos.modules.caja_general.repository;

import com.pe.articulos.modules.caja_general.entity.CajaGeneralMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CajaGeneralMovimientoRepository extends JpaRepository<CajaGeneralMovimiento, Long> {
    Page<CajaGeneralMovimiento> findByCajaGeneralIdOrderByFechaDesc(Long cajaGeneralId, Pageable pageable);
    Page<CajaGeneralMovimiento> findByCajaGeneralIdAndMetodoPagoDescripcionOrderByFechaDesc(Long cajaGeneralId, String metodoPago, Pageable pageable);

    @Query("SELECT m FROM CajaGeneralMovimiento m WHERE m.cajaGeneral.id = :cajaGeneralId " +
           "AND (:metodoPago IS NULL OR :metodoPago = '' OR UPPER(m.metodoPago.descripcion) = UPPER(:metodoPago)) " +
           "AND (:query IS NULL OR :query = '' OR " +
           "  (:searchType = 'DESCRIPCION' AND LOWER(m.descripcion) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "  (:searchType = 'REFERENCIA' AND LOWER(m.referencia) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "  (:searchType = 'USUARIO' AND LOWER(m.usuario) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "  (:searchType = 'TIPO' AND LOWER(m.tipo) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "  ((:searchType IS NULL OR :searchType = 'ALL' OR :searchType = '') AND (" +
           "    LOWER(m.descripcion) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "    LOWER(m.referencia) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "    LOWER(m.usuario) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "    LOWER(m.metodoPago.descripcion) LIKE LOWER(CONCAT('%', :query, '%'))" +
           "  ))" +
           ") ORDER BY m.fecha DESC")
    Page<CajaGeneralMovimiento> buscarMovimientosFiltrados(
            @Param("cajaGeneralId") Long cajaGeneralId,
            @Param("metodoPago") String metodoPago,
            @Param("query") String query,
            @Param("searchType") String searchType,
            Pageable pageable);
}

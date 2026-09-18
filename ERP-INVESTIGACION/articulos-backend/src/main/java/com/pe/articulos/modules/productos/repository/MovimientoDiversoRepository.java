package com.pe.articulos.modules.productos.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pe.articulos.modules.productos.entity.MovimientoDiverso;
import com.pe.articulos.core.enums.EstadoGeneral;
import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoDiversoRepository extends JpaRepository<MovimientoDiverso, Long> {

       @Query("SELECT m FROM MovimientoDiverso m WHERE (:idSucursal IS NULL OR m.sucursal.id = :idSucursal) " +
                     "AND (CAST(:fechaInicio AS timestamp) IS NULL OR m.fecha >= :fechaInicio) " +
                     "AND (CAST(:fechaFin AS timestamp) IS NULL OR m.fecha <= :fechaFin) " +
                     "AND (:estado IS NULL OR m.estado = :estado) " +
                     "AND (:tipoDocumento IS NULL OR EXISTS (SELECT p FROM PuntoDocumento p WHERE p.serie = m.serie AND p.tipoDocumento.tipoDoc = :tipoDocumento)) " +
                     "AND (:serie IS NULL OR m.serie = :serie) " +
                     "AND (:numero IS NULL OR CAST(m.numero AS string) LIKE CONCAT('%', CAST(:numero AS string), '%')) " +
                     "AND (:busqueda IS NULL OR :busqueda = '' OR LOWER(m.motivo) LIKE LOWER(CONCAT('%', :busqueda, '%')) "
                     +
                     "    OR LOWER(m.numDocumento) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
                     "    OR LOWER(m.usuario.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " + // Corregido: nombre
                     "    OR LOWER(m.usuario.apepat) LIKE LOWER(CONCAT('%', :busqueda, '%')) " + // Corregido: apepat
                     "    OR LOWER(m.usuario.apemat) LIKE LOWER(CONCAT('%', :busqueda, '%'))) " + // Corregido: apemat
                     "ORDER BY m.fecha DESC")
       Page<MovimientoDiverso> buscarPaginado(
                     @Param("idSucursal") Long idSucursal,
                     @Param("fechaInicio") LocalDateTime fechaInicio,
                     @Param("fechaFin") LocalDateTime fechaFin,
                     @Param("estado") EstadoGeneral estado,
                     @Param("busqueda") String busqueda,
                     @Param("tipoDocumento") String tipoDocumento,
                     @Param("serie") String serie,
                     @Param("numero") String numero,
                     @Param("pageable") Pageable pageable);

       @Query("SELECT CASE WHEN d.tipo = 'INGRESO' THEN 'ID' ELSE 'SD' END, m.serie, " +
                     "MIN(m.numero), MAX(m.numero), COUNT(DISTINCT m), SUM(d.cantidad * d.costoUnitario) " +
                     "FROM MovimientoDiverso m JOIN m.detalles d " +
                     "WHERE m.fecha BETWEEN :fechaInicio AND :fechaFin " +
                     "AND (:idSucursal IS NULL OR m.sucursal.idSucursal = :idSucursal) " +
                     "AND m.estado = com.pe.articulos.core.enums.EstadoGeneral.ACTIVO " +
                     "GROUP BY d.tipo, m.serie")
       List<Object[]> obtenerCorrelatividadRaw(
                     @Param("fechaInicio") LocalDateTime fechaInicio,
                     @Param("fechaFin") LocalDateTime fechaFin,
                     @Param("idSucursal") Long idSucursal);

       @Query("SELECT m FROM MovimientoDiverso m JOIN m.detalles d " +
                     "WHERE m.fecha BETWEEN :fechaInicio AND :fechaFin " +
                     "AND (:idSucursal IS NULL OR m.sucursal.idSucursal = :idSucursal) " +
                     "AND m.estado = com.pe.articulos.core.enums.EstadoGeneral.ACTIVO " +
                     "AND (:tipo = 'ID' AND d.tipo = 'INGRESO' OR :tipo = 'SD' AND d.tipo = 'SALIDA') " +
                     "AND m.serie = :serie " +
                     "ORDER BY m.numero ASC")
       List<MovimientoDiverso> findDetalleCorrelatividad(
                     @Param("fechaInicio") LocalDateTime fechaInicio,
                     @Param("fechaFin") LocalDateTime fechaFin,
                     @Param("idSucursal") Long idSucursal,
                     @Param("tipo") String tipo,
                     @Param("serie") String serie);
}

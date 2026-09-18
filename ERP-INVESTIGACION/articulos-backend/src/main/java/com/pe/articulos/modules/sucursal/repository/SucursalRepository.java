package com.pe.articulos.modules.sucursal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.modules.sucursal.entity.Sucursal;

import java.util.List;
import java.util.Optional;

@Repository
public interface SucursalRepository extends JpaRepository<Sucursal, Long> {

    // ========== BÚSQUEDA SIN PAGINACIÓN ==========

    Optional<Sucursal> findByNombreSucursal(String nombreSucursal);

    boolean existsByNombreSucursal(String nombreSucursal);

    List<Sucursal> findByNombreSucursalContainingIgnoreCase(String nombreSucursal);

    List<Sucursal> findByEstado(EstadoGeneral estado);

    @Query("SELECT s FROM Sucursal s WHERE s.estado = EstadoGeneral.ACTIVO")
    List<Sucursal> findAllActivas();

    @Query("SELECT s FROM Sucursal s WHERE s.idSucursal NOT IN " +
            "(SELECT DISTINCT d.idSucursal FROM DatosMedico d WHERE d.idSucursal IS NOT NULL)")
    List<Sucursal> findSucursalesSinPersonal();

    // ========== BÚSQUEDA CON PAGINACIÓN ==========

    Page<Sucursal> findByEstado(EstadoGeneral estado, Pageable pageable);

    Page<Sucursal> findByNombreSucursalContainingIgnoreCase(String nombreSucursal, Pageable pageable);

    Page<Sucursal> findByTelefonoContaining(String telefono, Pageable pageable);

    Page<Sucursal> findByDireccionContainingIgnoreCase(String direccion, Pageable pageable);

    @Query("SELECT s FROM Sucursal s WHERE s.estado = EstadoGeneral.ACTIVO")
    Page<Sucursal> findAllActivas(Pageable pageable);

    // ========== CONSULTAS ESPECIALES ==========

    @Query("""
                SELECT s, COUNT(d.id)
                FROM Sucursal s
                LEFT JOIN s.personal d
                WHERE d.estado = com.pe.articulos.core.enums.EstadoGeneral.ACTIVO OR d.estado IS NULL
                GROUP BY s
                ORDER BY COUNT(d.id) DESC
            """)
    List<Object[]> findSucursalesConCantidadPersonal();

    @Query("""
                SELECT s, COUNT(d.id)
                FROM Sucursal s
                LEFT JOIN s.personal d
                WHERE d.estado = com.pe.articulos.core.enums.EstadoGeneral.ACTIVO OR d.estado IS NULL
                GROUP BY s
                ORDER BY COUNT(d.id) DESC
            """)
    Page<Object[]> findSucursalesConCantidadPersonal(Pageable pageable);

    @Query("SELECT s FROM Sucursal s LEFT JOIN FETCH s.personal WHERE s.idSucursal = :id")
    Optional<Sucursal> findByIdWithPersonal(@Param("id") Long id);

    @Query("SELECT COUNT(s) FROM Sucursal s WHERE s.estado = :estado")
    Long countByEstado(@Param("estado") EstadoGeneral estado);

    @Query("SELECT COUNT(s) FROM Sucursal s WHERE s.estado = EstadoGeneral.ACTIVO")
    Long countActivas();

    @Query("""
                SELECT s FROM Sucursal s
                WHERE s.idSucursal IN (
                    SELECT DISTINCT d.idSucursal
                    FROM DatosMedico d
                    WHERE d.idSucursal IS NOT NULL AND d.estado = EstadoGeneral.ACTIVO
                )
            """)
    List<Sucursal> findSucursalesConPersonalActivo();

    @Query("""
                SELECT COUNT(d)
                FROM DatosMedico d
                WHERE d.idSucursal = :idSucursal AND d.estado = EstadoGeneral.ACTIVO
            """)
    Long countPersonalActivoBySucursal(@Param("idSucursal") Long idSucursal);
}
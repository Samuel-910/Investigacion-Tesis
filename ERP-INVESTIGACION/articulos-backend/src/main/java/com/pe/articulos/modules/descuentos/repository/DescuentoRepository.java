package com.pe.articulos.modules.descuentos.repository;

import com.pe.articulos.modules.descuentos.entity.Descuento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DescuentoRepository extends JpaRepository<Descuento, Long> {

        @Query(value = "SELECT * FROM venta_descuentos WHERE (:nombre IS NULL OR LOWER(nombre::text) LIKE LOWER(CONCAT('%', :nombre, '%'))) "
                        +
                        "AND (:activo IS NULL OR activo = :activo) "
                        +
                        "AND (:idCompania IS NULL OR id_compania = :idCompania)", countQuery = "SELECT COUNT(*) FROM venta_descuentos WHERE (:nombre IS NULL OR LOWER(nombre::text) LIKE LOWER(CONCAT('%', :nombre, '%'))) "
                                        +
                                        "AND (:activo IS NULL OR activo = :activo) "
                                        +
                                        "AND (:idCompania IS NULL OR id_compania = :idCompania)", nativeQuery = true)
        Page<Descuento> listarFiltros(@Param("nombre") String nombre, @Param("activo") Boolean activo,
                        @Param("idCompania") Long idCompania, Pageable pageable);

        @Query("SELECT d FROM Descuento d WHERE d.activo = true AND d.fechaInicio <= :ahora AND d.fechaFin >= :ahora")
        Page<Descuento> listarVigentes(@Param("ahora") LocalDateTime ahora, Pageable pageable);

        @Query("SELECT d FROM Descuento d WHERE d.activo = true AND d.fechaInicio <= :ahora AND d.fechaFin >= :ahora")
        List<Descuento> findAllVigentes(@Param("ahora") LocalDateTime ahora);
}

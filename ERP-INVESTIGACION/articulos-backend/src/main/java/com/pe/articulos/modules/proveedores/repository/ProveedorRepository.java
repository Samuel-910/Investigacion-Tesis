package com.pe.articulos.modules.proveedores.repository;

import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.modules.proveedores.entity.Proveedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    Optional<Proveedor> findByNumDocIdent(String numDocIdent);

    boolean existsByNumDocIdent(String numDocIdent);

    @Query("SELECT p FROM Proveedor p WHERE " +
            "(:estado IS NULL OR p.estado = :estado) AND " +
            "(LOWER(p.razonSocial) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.numDocIdent) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.telefono) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.distrito) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Proveedor> search(@Param("searchTerm") String searchTerm, @Param("estado") EstadoGeneral estado, Pageable pageable);
}

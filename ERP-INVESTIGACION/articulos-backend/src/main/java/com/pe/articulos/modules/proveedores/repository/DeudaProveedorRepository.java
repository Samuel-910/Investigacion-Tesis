package com.pe.articulos.modules.proveedores.repository;

import com.pe.articulos.modules.proveedores.entity.DeudaProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface DeudaProveedorRepository extends JpaRepository<DeudaProveedor, Long> {
    
    // Find pending debts for a specific account, ordered by creation date ascending (oldest first for FIFO)
    List<DeudaProveedor> findByCuentaProveedorIdAndEstadoOrderByFechaEmisionAsc(Long idCuentaProveedor, String estado);

    @Query("SELECT d FROM DeudaProveedor d JOIN FETCH d.cuentaProveedor c JOIN FETCH c.proveedor p " +
           "WHERE d.estado = :estado " +
           "AND (:searchTerm IS NULL OR :searchTerm = '' " +
           "OR LOWER(p.razonSocial) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR p.numDocIdent LIKE CONCAT('%', :searchTerm, '%'))")
    Page<DeudaProveedor> findPendientesConFiltro(@Param("estado") String estado, 
                                                @Param("searchTerm") String searchTerm, 
                                                Pageable pageable);

}

package com.pe.articulos.modules.empresa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pe.articulos.modules.empresa.entity.Empresa;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    @Query("SELECT e FROM Empresa e WHERE " +
            "(:q IS NULL OR LOWER(e.direccion) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(e.codigo) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(e.abrev) LIKE LOWER(CONCAT('%', :q, '%')))")

    Page<Empresa> search(@Param("q") String q, Pageable pageable);
}

package com.pe.articulos.modules.clinica.repository;

import com.pe.articulos.modules.clinica.entity.ClinicaAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ClinicaAuditoriaRepository extends JpaRepository<ClinicaAuditoria, Long> {
    Page<ClinicaAuditoria> findByIdClinica(Long idClinica, Pageable pageable);
}

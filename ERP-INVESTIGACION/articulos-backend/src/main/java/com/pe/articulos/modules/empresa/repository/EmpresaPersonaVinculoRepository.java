package com.pe.articulos.modules.empresa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pe.articulos.modules.empresa.entity.EmpresaPersonaVinculo;

@Repository
public interface EmpresaPersonaVinculoRepository extends JpaRepository<EmpresaPersonaVinculo, Long> {
    List<EmpresaPersonaVinculo> findByEmpresaIdEmpresa(Long idEmpresa);

    List<EmpresaPersonaVinculo> findByPersonalId(Long idPersonal);
}

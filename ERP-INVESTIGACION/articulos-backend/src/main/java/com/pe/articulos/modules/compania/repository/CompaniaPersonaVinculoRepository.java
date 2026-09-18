package com.pe.articulos.modules.compania.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pe.articulos.modules.compania.entity.CompaniaPersonaVinculo;

import com.pe.articulos.core.enums.EstadoGeneral;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface CompaniaPersonaVinculoRepository extends JpaRepository<CompaniaPersonaVinculo, Long> {
    Page<CompaniaPersonaVinculo> findByCompaniaIdAndEstado(Long idCompania, EstadoGeneral estado, Pageable pageable);

    List<CompaniaPersonaVinculo> findByPersonaIdAndEstado(Long idPersona, EstadoGeneral estado);
}

package com.pe.articulos.modules.catalogo.repository;

import com.pe.articulos.modules.catalogo.entity.UnidadMedida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import com.pe.articulos.core.enums.EstadoGeneral;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida, Long> {
    Optional<UnidadMedida> findByNombre(String nombre);
    Page<UnidadMedida> findByEstado(EstadoGeneral estado, Pageable pageable);
}

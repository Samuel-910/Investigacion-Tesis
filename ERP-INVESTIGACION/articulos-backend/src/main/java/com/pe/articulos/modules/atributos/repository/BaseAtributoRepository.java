package com.pe.articulos.modules.atributos.repository;

import com.pe.articulos.modules.atributos.entity.BaseAtributo;
import com.pe.articulos.core.enums.EstadoGeneral;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface BaseAtributoRepository<T extends BaseAtributo> extends JpaRepository<T, Long> {
    Page<T> findByDescripcionContainingIgnoreCase(String descripcion, Pageable pageable);

    Page<T> findByDescripcionContainingIgnoreCaseAndEstado(String descripcion, EstadoGeneral estado, Pageable pageable);

    boolean existsByDescripcionIgnoreCaseAndEstado(String descripcion, EstadoGeneral estado);

    Optional<T> findByDescripcionIgnoreCaseAndEstado(String descripcion, EstadoGeneral estado);

    Optional<T> findByDescripcionIgnoreCase(String descripcion);
}

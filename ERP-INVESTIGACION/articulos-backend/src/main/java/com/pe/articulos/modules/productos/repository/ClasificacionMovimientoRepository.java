package com.pe.articulos.modules.productos.repository;

import com.pe.articulos.modules.productos.entity.ClasificacionMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface ClasificacionMovimientoRepository extends JpaRepository<ClasificacionMovimiento, Long> {
    List<ClasificacionMovimiento> findByTipoIn(List<String> tipos);

    Page<ClasificacionMovimiento> findByTipo(String tipo, Pageable pageable);

    Page<ClasificacionMovimiento> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
}

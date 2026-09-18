package com.pe.articulos.modules.productos.repository;

import com.pe.articulos.modules.productos.entity.AjusteInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface AjusteInventarioRepository extends JpaRepository<AjusteInventario, Long> {
    Page<AjusteInventario> findByIdSucursal(Integer idSucursal, Pageable pageable);
}

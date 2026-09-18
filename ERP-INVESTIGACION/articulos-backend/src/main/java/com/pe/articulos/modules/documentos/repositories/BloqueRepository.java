package com.pe.articulos.modules.documentos.repositories;

import com.pe.articulos.modules.documentos.entities.Bloque;
import com.pe.articulos.modules.documentos.entities.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BloqueRepository extends JpaRepository<Bloque, Long> {
    boolean existsByNombreContainingIgnoreCase(String nombre);

    boolean existsByCategoriaContainingIgnoreCase(String categoria);

    boolean existsByNombreAndCategoria(String nombre, String categoria);

    java.util.Optional<Bloque> findByNombreAndCategoria(String nombre, String categoria);

    org.springframework.data.domain.Page<Bloque> findByModulosContaining(Modulo modulo,
            org.springframework.data.domain.Pageable pageable);
}

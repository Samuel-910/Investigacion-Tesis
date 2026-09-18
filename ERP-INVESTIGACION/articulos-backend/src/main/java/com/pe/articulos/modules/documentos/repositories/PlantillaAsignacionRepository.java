package com.pe.articulos.modules.documentos.repositories;

import com.pe.articulos.modules.documentos.entities.PlantillaAsignacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlantillaAsignacionRepository extends JpaRepository<PlantillaAsignacion, Long> {
    Page<PlantillaAsignacion> findByModulo(String modulo, Pageable pageable);
}
// Repositorio para gestión de plantillas
// Repositorio para gestión de plantillas

package com.pe.articulos.modules.documentos.repositories;

import com.pe.articulos.modules.documentos.entities.Modulo;
import com.pe.articulos.modules.documentos.entities.Plantilla;
import com.pe.articulos.modules.venta_registro.entity.TipoDocumento;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlantillaRepository extends JpaRepository<Plantilla, Long> {
    Optional<Plantilla> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    Page<Plantilla> findByModuloAndTipoDocumento(Modulo modulo,
            TipoDocumento tipoDocumento, Pageable pageable);

    Page<Plantilla> findByModulo(Modulo modulo,
            Pageable pageable);

    Optional<Plantilla> findByModuloAndTipoDocumentoAndIsDefaultTrue(Modulo modulo,
            TipoDocumento tipoDocumento);
}

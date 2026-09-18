package com.pe.articulos.modules.venta_registro.repository;

import com.pe.articulos.modules.venta_registro.entity.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Long> {
    java.util.Optional<TipoDocumento> findByTipoDoc(String tipoDoc);
}


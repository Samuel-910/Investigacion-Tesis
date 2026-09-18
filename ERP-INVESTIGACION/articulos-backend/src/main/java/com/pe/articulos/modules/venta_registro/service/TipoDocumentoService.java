package com.pe.articulos.modules.venta_registro.service;

import com.pe.articulos.modules.venta_registro.entity.TipoDocumento;
import java.util.List;
import java.util.Optional;

public interface TipoDocumentoService {
    List<TipoDocumento> findAll();

    Optional<TipoDocumento> findById(String id);

    TipoDocumento save(TipoDocumento tipoDocumento);

    void deleteById(String id);
}


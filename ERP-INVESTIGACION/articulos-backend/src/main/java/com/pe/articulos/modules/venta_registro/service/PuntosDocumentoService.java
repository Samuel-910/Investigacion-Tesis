package com.pe.articulos.modules.venta_registro.service;

import java.util.List;

import com.pe.articulos.modules.documentos.entities.Modulo;
import com.pe.articulos.modules.puntos.entity.PuntoDocumento;
import com.pe.articulos.modules.venta_registro.dto.SerieCorrelativoDto;

public interface PuntosDocumentoService {

    SerieCorrelativoDto generarCorrelativo(Long puntoId, String tipoDocId, Modulo modulo);

    SerieCorrelativoDto generarCorrelativoConPrefijo(Long puntoId, String tipoDocId, Modulo modulo, String seriePrefix);

    List<PuntoDocumento> findAll();

    PuntoDocumento save(PuntoDocumento puntosDocumento);
}


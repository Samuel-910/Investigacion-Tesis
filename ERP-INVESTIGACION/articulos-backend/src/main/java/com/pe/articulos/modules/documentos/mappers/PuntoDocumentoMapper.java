package com.pe.articulos.modules.documentos.mappers;

import com.pe.articulos.modules.documentos.dto.PuntoDocumentoDTO;
import com.pe.articulos.modules.puntos.entity.PuntoDocumento;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PuntoDocumentoMapper {
    PuntoDocumentoDTO toDTO(PuntoDocumento entity);
    PuntoDocumento toEntity(PuntoDocumentoDTO dto);
}

package com.pe.articulos.modules.documentos.mappers;

import com.pe.articulos.modules.documentos.dto.TipoDocumentoDTO;
import com.pe.articulos.modules.venta_registro.entity.TipoDocumento;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public interface TipoDocumentoMapper {
    TipoDocumentoDTO toDTO(TipoDocumento entity);
    TipoDocumento toEntity(TipoDocumentoDTO dto);
}

package com.pe.articulos.modules.documentos.mappers;

import com.pe.articulos.modules.documentos.dto.DocumentoFormatoDTO;
import com.pe.articulos.modules.documentos.entities.DocumentoFormato;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentoFormatoMapper {
    DocumentoFormatoDTO toDTO(DocumentoFormato entity);
    DocumentoFormato toEntity(DocumentoFormatoDTO dto);
}

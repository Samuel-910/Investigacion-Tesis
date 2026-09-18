package com.pe.articulos.modules.documentos.mappers;

import com.pe.articulos.modules.documentos.dto.BloqueDTO;
import com.pe.articulos.modules.documentos.entities.Bloque;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BloqueMapper {
    BloqueDTO toDTO(Bloque entity);
    Bloque toEntity(BloqueDTO dto);
}

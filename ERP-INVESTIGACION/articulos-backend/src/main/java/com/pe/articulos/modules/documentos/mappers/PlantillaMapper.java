package com.pe.articulos.modules.documentos.mappers;

import com.pe.articulos.modules.documentos.dto.PlantillaDTO;
import com.pe.articulos.modules.documentos.entities.Plantilla;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { DocumentoFormatoMapper.class,
        TipoDocumentoMapper.class })
public interface PlantillaMapper {
    PlantillaDTO toDTO(Plantilla entity);

    Plantilla toEntity(PlantillaDTO dto);
}

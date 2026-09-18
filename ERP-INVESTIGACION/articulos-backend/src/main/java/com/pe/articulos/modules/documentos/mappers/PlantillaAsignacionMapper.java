package com.pe.articulos.modules.documentos.mappers;

import com.pe.articulos.modules.documentos.dto.PlantillaAsignacionDTO;
import com.pe.articulos.modules.documentos.entities.PlantillaAsignacion;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {PlantillaMapper.class, TipoDocumentoMapper.class, PuntoDocumentoMapper.class})
public interface PlantillaAsignacionMapper {
    PlantillaAsignacionDTO toDTO(PlantillaAsignacion entity);
    PlantillaAsignacion toEntity(PlantillaAsignacionDTO dto);
}

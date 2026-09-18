package com.pe.articulos.modules.catalogo.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.pe.articulos.modules.catalogo.dto.UnidadMedidaRequest;
import com.pe.articulos.modules.catalogo.dto.UnidadMedidaResponse;
import com.pe.articulos.modules.catalogo.entity.UnidadMedida;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UnidadMedidaMapper {

    UnidadMedidaResponse toResponse(UnidadMedida entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "usuarioModificacion", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UnidadMedida toEntity(UnidadMedidaRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "usuarioCreacion", ignore = true)
    @Mapping(target = "usuarioModificacion", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget UnidadMedida entity, UnidadMedidaRequest request);
}

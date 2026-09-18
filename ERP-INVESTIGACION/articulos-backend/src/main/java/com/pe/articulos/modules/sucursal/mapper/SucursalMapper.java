package com.pe.articulos.modules.sucursal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.pe.articulos.modules.sucursal.entity.Sucursal;
import com.pe.articulos.modules.sucursal.dto.SucursalDto;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SucursalMapper {

    SucursalDto toDto(Sucursal entity);

    Sucursal toEntity(SucursalDto dto);

    void updateEntityFromDto(SucursalDto dto, @MappingTarget Sucursal entity);
}

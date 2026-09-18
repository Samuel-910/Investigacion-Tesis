package com.pe.articulos.modules.permissions.dto;

import org.mapstruct.Mapper;

import com.pe.articulos.modules.permissions.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    PermissionResponse toDto(Permission permission);

}

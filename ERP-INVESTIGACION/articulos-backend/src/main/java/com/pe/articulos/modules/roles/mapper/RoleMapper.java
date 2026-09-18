package com.pe.articulos.modules.roles.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.pe.articulos.modules.roles.entity.Role;
import com.pe.articulos.modules.roles.dto.RoleResponse;
import com.pe.articulos.modules.permissions.entity.Permission;
import com.pe.articulos.modules.permissions.dto.PermissionResponse;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "userCount", expression = "java(role.getUsers() != null ? role.getUsers().size() : 0)")
    @Mapping(target = "idAcceso", source = "acceso.id")
    @Mapping(target = "nombreAcceso", source = "acceso.nombre")
    RoleResponse toResponse(Role role);

    PermissionResponse toPermissionResponse(Permission permission);
    
    Set<PermissionResponse> toPermissionResponseSet(Set<Permission> permissions);
}

package com.pe.articulos.modules.users.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import com.pe.articulos.modules.users.entity.DatosPersonales;
import com.pe.articulos.modules.users.dto.UserResponse;
import com.pe.articulos.modules.users.dto.CreateUserRequest;
import com.pe.articulos.modules.users.dto.UpdateUserRequest;
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    @Mapping(target = "nombreCompleto", expression = "java(entity.getNombreCompleto())")
    @Mapping(target = "roles", expression = "java(entity.getRoleNames())")
    @Mapping(target = "permissions", expression = "java(entity.getPermissionNames())")
    @Mapping(target = "isCompania", expression = "java(entity.getCompania() != null)")
    @Mapping(target = "username", source = "login")
    @Mapping(target = "firstName", source = "nombre")
    @Mapping(target = "lastName", source = "apepat")
    @Mapping(target = "phone", source = "fonLocal")
    UserResponse toDto(DatosPersonales entity);
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "directPermissions", ignore = true)
    @Mapping(target = "sucursalesAsignadas", ignore = true)
    DatosPersonales toEntity(CreateUserRequest request);
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "directPermissions", ignore = true)
    @Mapping(target = "sucursalesAsignadas", ignore = true)
    void updateEntityFromDto(UpdateUserRequest request, @MappingTarget DatosPersonales entity);
}


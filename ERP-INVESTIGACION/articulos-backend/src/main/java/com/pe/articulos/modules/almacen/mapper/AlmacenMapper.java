package com.pe.articulos.modules.almacen.mapper;

import com.pe.articulos.modules.almacen.dto.AlmacenRequest;
import com.pe.articulos.modules.almacen.dto.AlmacenResponse;
import com.pe.articulos.modules.almacen.entity.Almacen;
import com.pe.articulos.modules.sucursal.entity.Sucursal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AlmacenMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", source = "request.estado")
    @Mapping(target = "sucursal", source = "sucursal")
    Almacen toEntity(AlmacenRequest request, Sucursal sucursal);

    @Mapping(target = "idSucursal", source = "sucursal.idSucursal")
    @Mapping(target = "nombreSucursal", source = "sucursal.nombreSucursal")
    AlmacenResponse toResponse(Almacen entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", source = "request.estado")
    @Mapping(target = "sucursal", source = "sucursal", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Almacen entity, AlmacenRequest request, Sucursal sucursal);
}

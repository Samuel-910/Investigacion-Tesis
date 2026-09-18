package com.pe.articulos.modules.proveedores.mapper;

import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.modules.proveedores.dto.ProveedorRequest;
import com.pe.articulos.modules.proveedores.dto.ProveedorResponse;
import com.pe.articulos.modules.proveedores.entity.Proveedor;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProveedorMapper {

    ProveedorResponse toResponse(Proveedor entity);

    Proveedor toEntity(ProveedorRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(ProveedorRequest request, @MappingTarget Proveedor entity);

    default Integer map(EstadoGeneral estado) {
        return estado != null ? estado.getValor() : null;
    }

    default EstadoGeneral map(Integer valor) {
        return valor != null ? EstadoGeneral.fromInt(valor) : null;
    }
}

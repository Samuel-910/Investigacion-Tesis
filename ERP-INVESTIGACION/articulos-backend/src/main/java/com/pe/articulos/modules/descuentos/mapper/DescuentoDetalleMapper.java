package com.pe.articulos.modules.descuentos.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.pe.articulos.modules.descuentos.dto.DescuentoDetalleDTO;
import com.pe.articulos.modules.descuentos.entity.DescuentoDetalle;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DescuentoDetalleMapper {

    @Mapping(target = "nombreCatalogo", source = "catalogo.nombre")
    DescuentoDetalleDTO toDTO(DescuentoDetalle entity);

    DescuentoDetalle toEntity(DescuentoDetalleDTO dto);
}

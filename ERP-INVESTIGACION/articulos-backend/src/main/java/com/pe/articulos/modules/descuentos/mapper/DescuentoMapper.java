package com.pe.articulos.modules.descuentos.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.pe.articulos.modules.descuentos.dto.DescuentoDTO;
import com.pe.articulos.modules.descuentos.entity.Descuento;

@Mapper(componentModel = "spring", uses = {DescuentoDetalleMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DescuentoMapper {

    DescuentoDTO toDTO(Descuento entity);

    Descuento toEntity(DescuentoDTO dto);
}

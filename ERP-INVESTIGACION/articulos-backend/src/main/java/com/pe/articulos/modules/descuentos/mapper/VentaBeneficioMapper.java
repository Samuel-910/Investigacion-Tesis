package com.pe.articulos.modules.descuentos.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.pe.articulos.modules.descuentos.dto.VentaBeneficioDTO;
import com.pe.articulos.modules.descuentos.entity.VentaBeneficio;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VentaBeneficioMapper {

    @Mapping(target = "idVenta", source = "venta.idVenta")
    VentaBeneficioDTO toDTO(VentaBeneficio entity);

    @Mapping(target = "venta.idVenta", source = "idVenta")
    VentaBeneficio toEntity(VentaBeneficioDTO dto);
}

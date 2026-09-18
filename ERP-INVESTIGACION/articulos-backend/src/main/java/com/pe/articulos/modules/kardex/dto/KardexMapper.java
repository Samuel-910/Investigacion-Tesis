package com.pe.articulos.modules.kardex.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.pe.articulos.modules.kardex.entity.Kardex;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface KardexMapper {
    @Mapping(source = "idAlmArticulo", target = "idAlmacen")
    KardexDTO toDto(Kardex kardex);
    
    @Mapping(source = "idAlmacen", target = "idAlmArticulo")
    Kardex toEntity(KardexDTO dto);
}

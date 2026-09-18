package com.pe.articulos.modules.empresa.dto;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.pe.articulos.modules.empresa.entity.Empresa;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmpresaMapper {
    EmpresaDTO toDto(Empresa empresa);
    Empresa toEntity(EmpresaDTO dto);
}

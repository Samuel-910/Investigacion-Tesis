package com.pe.articulos.modules.empresa.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.pe.articulos.modules.empresa.entity.EmpresaPersonaVinculo;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmpresaPersonaVinculoMapper {
    
    @Mapping(source = "empresa.idEmpresa", target = "idEmpresa")
    @Mapping(source = "personal.id", target = "idPersonal")
    EmpresaPersonaVinculoDTO toDto(EmpresaPersonaVinculo vinculo);
    
    @Mapping(source = "idEmpresa", target = "empresa.idEmpresa")
    @Mapping(source = "idPersonal", target = "personal.id")
    EmpresaPersonaVinculo toEntity(EmpresaPersonaVinculoDTO dto);
}

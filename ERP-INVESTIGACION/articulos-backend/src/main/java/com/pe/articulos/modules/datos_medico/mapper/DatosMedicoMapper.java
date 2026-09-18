package com.pe.articulos.modules.datos_medico.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.pe.articulos.modules.datos_medico.dto.DatosMedicoCreateDto;
import com.pe.articulos.modules.datos_medico.dto.DatosMedicoDto;
import com.pe.articulos.modules.datos_medico.dto.DatosMedicoUpdateDto;
import com.pe.articulos.modules.datos_medico.entity.DatosMedico;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DatosMedicoMapper {

    @Mapping(target = "nombreArea", source = "area.nombre")
    @Mapping(target = "nombreSucursal", source = "sucursal.nombreSucursal")
    DatosMedicoDto toDto(DatosMedico entity);

    DatosMedico createDtoToEntity(DatosMedicoCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(DatosMedicoUpdateDto dto, @MappingTarget DatosMedico entity);

}

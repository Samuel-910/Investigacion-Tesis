package com.pe.articulos.modules.puntos.mapper;

import com.pe.articulos.modules.puntos.dto.PuntoRequestDTO;
import com.pe.articulos.modules.puntos.dto.PuntoResponseDTO;
import com.pe.articulos.modules.puntos.entity.Punto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PuntoMapper {

    @Mapping(source = "sucursal.nombreSucursal", target = "nombreSucursal")
    @Mapping(source = "tipoRelacion.descripcion", target = "nombreTipo")
    @Mapping(source = "procesoRelacion.descripcion", target = "nombreProceso")
    @Mapping(source = "almacen.nombre", target = "nombreAlmacen")
    PuntoResponseDTO toResponseDTO(Punto punto);

    @Mapping(target = "punto", ignore = true)
    Punto toEntity(PuntoRequestDTO dto);

    @Mapping(target = "punto", ignore = true)
    void updateEntityFromDTO(PuntoRequestDTO dto, @MappingTarget Punto punto);
}

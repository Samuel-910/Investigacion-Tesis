package com.pe.articulos.modules.compania.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.pe.articulos.modules.compania.dto.CompaniaPersonaVinculoResponseDTO;
import com.pe.articulos.modules.compania.dto.CompaniaResponseDTO;
import com.pe.articulos.modules.compania.entity.Compania;
import com.pe.articulos.modules.compania.entity.CompaniaPersonaVinculo;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompaniaMapper {

    @Mapping(target = "ruc", source = "personaBase.ruc")
    @Mapping(target = "email", source = "personaBase.email")
    @Mapping(target = "direcc", source = "personaBase.direcc")
    @Mapping(target = "fonLocal", source = "personaBase.fonLocal")
    CompaniaResponseDTO toResponse(Compania entity);

    @Mapping(target = "idCompania", source = "compania.id")
    @Mapping(target = "companiaNombre", source = "compania.nombre")
    @Mapping(target = "idPaciente", source = "persona.id")
    @Mapping(target = "pacienteNombreCompleto", expression = "java(entity.getPersona() != null ? entity.getPersona().getNombreCompleto() : null)")
    @Mapping(target = "pacienteDni", source = "persona.numdoc")
    CompaniaPersonaVinculoResponseDTO toVinculoResponse(CompaniaPersonaVinculo entity);

}

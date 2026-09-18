package com.pe.articulos.modules.puntos.mapper;

import com.pe.articulos.modules.puntos.dto.PuntoDocumentoRequestDTO;
import com.pe.articulos.modules.puntos.dto.PuntoDocumentoResponseDTO;
import com.pe.articulos.modules.documentos.repositories.PlantillaRepository;
import com.pe.articulos.modules.puntos.entity.Punto;
import com.pe.articulos.modules.puntos.entity.PuntoDocumento;
import com.pe.articulos.modules.venta_registro.repository.TipoDocumentoRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", implementationName = "PuntoDocumentoPuntosMapperImpl", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PuntoDocumentoMapper {

    @Autowired
    protected TipoDocumentoRepository tipoDocumentoRepository;

    @Autowired
    protected PlantillaRepository plantillaRepository;

    @Mapping(source = "punto.punto", target = "puntoId")
    @Mapping(source = "punto.nombre", target = "puntoNombre")
    @Mapping(source = "tipoDocumento.tipoDoc", target = "tipoDoc")
    @Mapping(source = "tipoDocumento.nombre", target = "tipoDocumentoNombre")
    public abstract PuntoDocumentoResponseDTO toResponseDTO(PuntoDocumento documento);

    @AfterMapping
    protected void fillPlantillaInfo(PuntoDocumento documento, @MappingTarget PuntoDocumentoResponseDTO dto) {
        Long idPlantilla = documento.getIdPlantilla();
        if (idPlantilla != null) {
            plantillaRepository.findById(idPlantilla).ifPresent(p -> {
                dto.setPlantillaNombre(p.getNombre());
                if (p.getFormato() != null) {
                    dto.setPlantillaFormato(p.getFormato().getNombre());
                }
                dto.setPlantillaOrientacion(p.getOrientacion());
            });
        }
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "punto", source = "punto")
    @Mapping(target = "modulo", source = "dto.modulo")
    public abstract PuntoDocumento toEntity(PuntoDocumentoRequestDTO dto, Punto punto);

    @AfterMapping
    protected void fillTipoDocumento(PuntoDocumentoRequestDTO dto, @MappingTarget PuntoDocumento documento) {
        if (dto.getTipoDoc() != null) {
            tipoDocumentoRepository.findByTipoDoc(dto.getTipoDoc()).ifPresent(documento::setTipoDocumento);
        }
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "punto", source = "punto")
    @Mapping(target = "modulo", source = "dto.modulo")
    public abstract void updateEntityFromDTO(PuntoDocumentoRequestDTO dto, @MappingTarget PuntoDocumento documento, Punto punto);
}

package com.pe.articulos.modules.niveles.mapper;

import com.pe.articulos.modules.niveles.dto.NivelCreateDto;
import com.pe.articulos.modules.niveles.dto.NivelDto;
import com.pe.articulos.modules.niveles.dto.NivelTreeDto;
import com.pe.articulos.modules.niveles.entity.Nivel;
import com.pe.articulos.core.enums.EstadoGeneral;
import org.springframework.stereotype.Component;

@Component
public class NivelMapper {

    public NivelDto toDto(Nivel entity) {
        if (entity == null)
            return null;

        NivelDto dto = new NivelDto();
        dto.setIdNivel(entity.getIdNivel());
        dto.setNombre(entity.getNombre());
        dto.setNumNivel(entity.getNumNivel());
        dto.setTipo(entity.getTipo());
        dto.setIdTipoAte(entity.getIdTipoAte());
        dto.setCentroCosto(entity.getCentroCosto());
        dto.setCentCostLimite(entity.getCentCostLimite());
        dto.setEstado(entity.getEstado());
        dto.setIdNivelPadre(entity.getIdNivelPadre());
        dto.setNivelJerarquia(entity.getNivelJerarquia());
        dto.setOrden(entity.getOrden());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // Datos del padre
        if (entity.getNivelPadre() != null) {
            dto.setNombrePadre(entity.getNivelPadre().getNombre());
        }

        // Información de hijos
        dto.setTotalHijos(entity.getNivelesHijos() != null ? entity.getNivelesHijos().size() : 0);
        dto.setTieneHijos(dto.getTotalHijos() > 0);

        return dto;
    }

    public Nivel toEntity(NivelCreateDto dto) {
        if (dto == null)
            return null;

        Nivel entity = new Nivel();
        entity.setNombre(dto.getNombre());
        entity.setNumNivel(dto.getNumNivel());
        entity.setTipo(dto.getTipo());
        entity.setIdTipoAte(dto.getIdTipoAte());
        entity.setCentroCosto(dto.getCentroCosto());
        entity.setCentCostLimite(dto.getCentCostLimite());
        entity.setEstado(dto.getEstado() != null ? dto.getEstado() : EstadoGeneral.ACTIVO);
        entity.setIdNivelPadre(dto.getIdNivelPadre());
        entity.setOrden(dto.getOrden() != null ? dto.getOrden() : 0);

        return entity;
    }

    public void updateEntity(NivelDto dto, Nivel entity) {
        if (dto.getNombre() != null)
            entity.setNombre(dto.getNombre());
        if (dto.getNumNivel() != null)
            entity.setNumNivel(dto.getNumNivel());
        if (dto.getTipo() != null)
            entity.setTipo(dto.getTipo());
        if (dto.getIdTipoAte() != null)
            entity.setIdTipoAte(dto.getIdTipoAte());
        if (dto.getCentroCosto() != null)
            entity.setCentroCosto(dto.getCentroCosto());
        if (dto.getCentCostLimite() != null)
            entity.setCentCostLimite(dto.getCentCostLimite());
        if (dto.getEstado() != null)
            entity.setEstado(dto.getEstado());
        if (dto.getOrden() != null)
            entity.setOrden(dto.getOrden());
    }

    public NivelTreeDto toTreeDto(Nivel entity) {
        if (entity == null)
            return null;

        NivelTreeDto dto = new NivelTreeDto();
        dto.setIdNivel(entity.getIdNivel());
        dto.setNombre(entity.getNombre());
        dto.setNumNivel(entity.getNumNivel());
        dto.setTipo(entity.getTipo());
        dto.setEstado(entity.getEstado());
        dto.setNivelJerarquia(entity.getNivelJerarquia());
        dto.setOrden(entity.getOrden());
        dto.setIdNivelPadre(entity.getIdNivelPadre());

        int totalHijos = entity.getNivelesHijos() != null ? entity.getNivelesHijos().size() : 0;
        dto.setTieneHijos(totalHijos > 0);

        // Iconos según tipo
        dto.setIcono(obtenerIconoPorTipo(entity.getTipo()));
        dto.setColor(obtenerColorPorJerarquia(entity.getNivelJerarquia()));

        return dto;
    }

    private String obtenerIconoPorTipo(String tipo) {
        if (tipo == null)
            return "folder";
        return switch (tipo.toUpperCase()) {
            case "SERVICIO" -> "building";
            case "ESPECIALIDAD" -> "medical";
            case "CONSULTORIO" -> "door";
            case "AREA" -> "lab";
            case "AMBIENTE" -> "room";
            default -> "folder";
        };
    }

    private String obtenerColorPorJerarquia(Integer nivel) {
        if (nivel == null)
            return "blue";
        return switch (nivel) {
            case 1 -> "blue";
            case 2 -> "green";
            case 3 -> "purple";
            case 4 -> "orange";
            default -> "gray";
        };
    }
}

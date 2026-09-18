package com.pe.articulos.modules.documentos.services;

import org.springframework.data.domain.Pageable;

import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.documentos.dto.PlantillaAsignacionDTO;
import com.pe.articulos.modules.documentos.entities.PlantillaAsignacion;

public interface PlantillaAsignacionService {
        PageResponse<PlantillaAsignacionDTO> listarPorModulo(
                        String modulo, Pageable pageable);

        PlantillaAsignacionDTO guardar(
                        PlantillaAsignacion asignacion);

        void eliminar(Long id);

        PlantillaAsignacionDTO obtenerPorId(
                        Long id);
}

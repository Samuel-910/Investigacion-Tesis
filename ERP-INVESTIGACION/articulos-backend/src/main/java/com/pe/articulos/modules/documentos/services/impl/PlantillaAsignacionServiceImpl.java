package com.pe.articulos.modules.documentos.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pe.articulos.core.exception.ResourceNotFoundException;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.documentos.dto.PlantillaAsignacionDTO;
import com.pe.articulos.modules.documentos.entities.PlantillaAsignacion;
import com.pe.articulos.modules.documentos.mappers.PlantillaAsignacionMapper;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class PlantillaAsignacionServiceImpl
        implements com.pe.articulos.modules.documentos.services.PlantillaAsignacionService {

    private final com.pe.articulos.modules.documentos.repositories.PlantillaAsignacionRepository repository;
    private final PlantillaAsignacionMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PlantillaAsignacionDTO> listarPorModulo(String modulo, Pageable pageable) {
        log.info("Listando asignaciones paginadas para módulo: {}", modulo);
        Page<PlantillaAsignacion> page = repository.findByModulo(modulo, pageable);
        return PageResponse.fromPage(page.map(mapper::toDTO));
    }

    @Override
    @Transactional
    public PlantillaAsignacionDTO guardar(PlantillaAsignacion asignacion) {
        log.info("Guardando asignación de plantilla");
        if (asignacion == null) {
            throw new IllegalArgumentException("Asignación no puede ser nula");
        }
        return mapper.toDTO(repository.save(asignacion));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando (Soft Delete) asignación ID: {}", id);
        PlantillaAsignacion entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada"));
        entity.setEstado(3); // Soft delete
        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public PlantillaAsignacionDTO obtenerPorId(Long id) {
        return repository.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada"));
    }
}
// Implementación del servicio de asignación de plantillas

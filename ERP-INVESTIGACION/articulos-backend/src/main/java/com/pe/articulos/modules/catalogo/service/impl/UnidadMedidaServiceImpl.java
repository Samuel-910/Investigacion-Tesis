package com.pe.articulos.modules.catalogo.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pe.articulos.core.exception.ResourceNotFoundException;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.catalogo.dto.UnidadMedidaRequest;
import com.pe.articulos.modules.catalogo.dto.UnidadMedidaResponse;
import com.pe.articulos.modules.catalogo.entity.UnidadMedida;
import com.pe.articulos.modules.catalogo.mapper.UnidadMedidaMapper;
import com.pe.articulos.modules.catalogo.repository.UnidadMedidaRepository;
import com.pe.articulos.modules.catalogo.service.UnidadMedidaService;
import com.pe.articulos.core.enums.EstadoGeneral;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class UnidadMedidaServiceImpl implements UnidadMedidaService {

    private final UnidadMedidaRepository repository;
    private final UnidadMedidaMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UnidadMedidaResponse> listarActivas(Pageable pageable) {
        Page<UnidadMedida> page = repository.findByEstado(EstadoGeneral.ACTIVO, pageable);
        List<UnidadMedidaResponse> content = page.getContent().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UnidadMedidaResponse> listarTodos(Pageable pageable) {
        Page<UnidadMedida> page = repository.findAll(pageable);
        List<UnidadMedidaResponse> content = page.getContent().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber());
    }

    @Override
    @Transactional
    public UnidadMedidaResponse crear(UnidadMedidaRequest request) {
        UnidadMedida entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public UnidadMedidaResponse actualizar(Long id, UnidadMedidaRequest request) {
        UnidadMedida entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidad de Medida no encontrada"));

        mapper.updateEntity(entity, request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        UnidadMedida entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidad de Medida no encontrada"));

        entity.setEstado(EstadoGeneral.ELIMINADO);
        repository.save(entity);
    }
}

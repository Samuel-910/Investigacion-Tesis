package com.pe.articulos.modules.documentos.services;

import com.pe.articulos.core.exception.ResourceNotFoundException;
import com.pe.articulos.modules.documentos.dto.DocumentoFormatoDTO;
import com.pe.articulos.modules.documentos.entities.DocumentoFormato;
import com.pe.articulos.modules.documentos.mappers.DocumentoFormatoMapper;
import com.pe.articulos.modules.documentos.repositories.DocumentoFormatoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.pe.articulos.core.shared.dto.PageResponse;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DocumentoFormatoService {

    private final DocumentoFormatoRepository repository;
    private final DocumentoFormatoMapper mapper;

    @Transactional(readOnly = true)
    public PageResponse<DocumentoFormatoDTO> listarTodos(Pageable pageable) {
        log.info("Listando formatos de documentos paginado");
        Page<DocumentoFormato> page = repository.findAll(pageable);
        return PageResponse.fromPage(page.map(mapper::toDTO));
    }

    @Transactional(readOnly = true)
    public DocumentoFormatoDTO obtenerPorId(Long id) {
        return repository.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Formato no encontrado"));
    }

    @Transactional
    public DocumentoFormatoDTO guardar(DocumentoFormato formato) {
        log.info("Guardando formato de documento");
        return mapper.toDTO(repository.save(formato));
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando formato ID: {}", id);
        DocumentoFormato formato = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formato no encontrado"));
        formato.setEstado(3); // Soft delete
        repository.save(formato);
    }
}

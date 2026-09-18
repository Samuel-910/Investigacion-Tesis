package com.pe.articulos.modules.proveedores.service;

import com.pe.articulos.core.exception.ConflictException;
import com.pe.articulos.core.exception.ResourceNotFoundException;
import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.proveedores.dto.ProveedorRequest;
import com.pe.articulos.modules.proveedores.dto.ProveedorResponse;
import com.pe.articulos.modules.proveedores.entity.Proveedor;
import com.pe.articulos.modules.proveedores.mapper.ProveedorMapper;
import com.pe.articulos.modules.proveedores.repository.ProveedorRepository;
import com.pe.articulos.modules.proveedores.repository.CuentaProveedorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ProveedorService {

    private final ProveedorRepository repository;
    private final ProveedorMapper mapper;
    private final CuentaProveedorRepository cuentaRepository;

    @Transactional
    public ProveedorResponse crear(ProveedorRequest request) {
        log.info("Creando nuevo proveedor: {}", request.getRazonSocial());

        if (repository.existsByNumDocIdent(request.getNumDocIdent())) {
            throw new ConflictException(
                    "Ya existe un proveedor con el número de documento: " + request.getNumDocIdent());
        }

        Proveedor entity = mapper.toEntity(request);

        Proveedor saved = repository.save(entity);

        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProveedorResponse obtenerPorId(Long id) {
        Proveedor entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", id));
        ProveedorResponse response = mapper.toResponse(entity);
        asignarSaldo(response);
        return response;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProveedorResponse> listarTodos(Pageable pageable) {
        Page<Proveedor> page = repository.findAll(pageable);
        Page<ProveedorResponse> responsePage = page.map(mapper::toResponse);
        responsePage.getContent().forEach(this::asignarSaldo);
        return PageResponse.fromPage(responsePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProveedorResponse> buscar(String searchTerm, Integer estado, Pageable pageable) {
        EstadoGeneral estadoEnum = estado != null ? EstadoGeneral.fromInt(estado) : null;
        Page<Proveedor> page = repository.search(searchTerm, estadoEnum, pageable);
        Page<ProveedorResponse> responsePage = page.map(mapper::toResponse);
        responsePage.getContent().forEach(this::asignarSaldo);
        return PageResponse.fromPage(responsePage);
    }

    private void asignarSaldo(ProveedorResponse response) {
        cuentaRepository.findByProveedorId(response.getId())
                .ifPresent(cuenta -> response.setSaldo(cuenta.getSaldoTotal()));
    }

    @Transactional
    public ProveedorResponse actualizar(Long id, ProveedorRequest request) {
        log.info("Actualizando proveedor con ID: {}", id);

        Proveedor entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", id));

        if (!entity.getNumDocIdent().equals(request.getNumDocIdent()) &&
                repository.existsByNumDocIdent(request.getNumDocIdent())) {
            throw new ConflictException(
                    "Ya existe otro proveedor con el número de documento: " + request.getNumDocIdent());
        }

        mapper.updateEntityFromRequest(request, entity);
        Proveedor updated = repository.save(entity);
        return mapper.toResponse(updated);
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando proveedor con ID: {}", id);
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Proveedor", "id", id);
        }
        repository.deleteById(id);
    }
}

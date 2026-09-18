package com.pe.articulos.modules.catalogo.service.impl;

import com.pe.articulos.modules.catalogo.dto.CatalogoRequest;
import com.pe.articulos.modules.catalogo.dto.CatalogoResponse;
import com.pe.articulos.core.exception.ConflictException;
import com.pe.articulos.core.exception.ResourceNotFoundException;
import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.catalogo.entity.Catalogo;
import com.pe.articulos.modules.catalogo.mapper.CatalogoMapper;
import com.pe.articulos.modules.catalogo.repository.CatalogoRepository;
import com.pe.articulos.modules.catalogo.service.CatalogoService;

import com.pe.articulos.core.security.SecurityUtils;
import com.pe.articulos.modules.almacen.repository.AlmacenRepository;
import com.pe.articulos.modules.kardex.dto.KardexDTO;
import com.pe.articulos.modules.kardex.service.KardexService;
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
public class CatalogoServiceImpl implements CatalogoService {

    private final CatalogoRepository repository;
    private final CatalogoMapper mapper;
    private final KardexService kardexService;
    private final AlmacenRepository almacenRepository;
    private final com.pe.articulos.modules.niveles.repository.NivelRepository nivelRepository;

    @Override
    @Transactional
    @SuppressWarnings("null")
    public CatalogoResponse crear(CatalogoRequest request) {
        log.info("Creando nuevo producto/servicio: {}", request.getNombre());

        if (request.getCodigo() != null && repository.existsByCodigo(request.getCodigo())) {
            throw new ConflictException(
                    "Ya existe un producto/servicio con el código: " + request.getCodigo());
        }

        Catalogo entity = mapper.toEntity(request);
        
        // Asignar el nivel FARMACIA por defecto siempre
        com.pe.articulos.modules.niveles.entity.Nivel farmaciaNivel = nivelRepository.findByNombreIgnoreCase("FARMACIA")
                .orElseThrow(() -> new IllegalStateException("Nivel FARMACIA no está configurado en el sistema"));
        entity.setNivel(farmaciaNivel);

        if (entity.getTipoCatalogo() == null) {
            entity.setTipoCatalogo("ARTICULO");
        }
        if (entity.getNombreCatalogo() == null || entity.getNombreCatalogo().trim().isEmpty()) {
            entity.setNombreCatalogo(entity.getNombre());
        }

        Catalogo saved = repository.save(entity);
        log.info("Producto/servicio creado con ID: {}", saved.getId());

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogoResponse obtenerPorId(Long id) {
        log.info("Obteniendo producto/servicio por ID: {}", id);
        Catalogo entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catalogo", "id", id));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public PageResponse<CatalogoResponse> listarTodos(Pageable pageable, String tipo, Long idCategoria,
            Boolean esGenerico, Boolean manejaLotes, String estadoStr, String tipoAfectacion) {
        log.info("Listando todos los productos/servicios - Página: {}, Tamaño: {}, Tipo: {}",
                pageable.getPageNumber(), pageable.getPageSize(), tipo);

        Catalogo.Tipo tipoEnum = parseTipo(tipo);
        EstadoGeneral estadoEnum = parseEstado(estadoStr);
        Page<Catalogo> page = repository.listarCatalogoGenerico(tipoEnum, idCategoria, esGenerico, manejaLotes, estadoEnum, tipoAfectacion, pageable);
        return buildPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CatalogoResponse> buscar(String searchTerm, String searchType, String tipo, Long idCategoria,
            Boolean esGenerico, Boolean manejaLotes, String estadoStr, String tipoAfectacion, Pageable pageable) {
        log.info("Buscando productos/servicios - Termino: {}, SearchType: {}, Tipo: {}", searchTerm, searchType, tipo);

        Catalogo.Tipo tipoEnum = parseTipo(tipo);
        EstadoGeneral estadoEnum = parseEstado(estadoStr);
        Page<Catalogo> page = repository.searchCatalogoGenerico(searchTerm, searchType, tipoEnum, idCategoria,
                esGenerico, manejaLotes, estadoEnum, tipoAfectacion, pageable);
        return buildPageResponse(page);
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public CatalogoResponse actualizar(Long id, CatalogoRequest request) {
        log.info("Actualizando producto/servicio con ID: {}", id);

        Catalogo entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catalogo", "id", id));

        if (request.getCodigo() != null && !request.getCodigo().equals(entity.getCodigo())) {
            if (repository.existsByCodigo(request.getCodigo())) {
                throw new ConflictException(
                        "Ya existe un producto/servicio con el código: " + request.getCodigo());
            }
        }

        mapper.updateEntityFromRequest(request, entity);
        Catalogo updated = repository.save(entity);

        log.info("Producto/servicio actualizado: {}", id);
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public void eliminar(Long id) {
        log.info("Eliminando producto/servicio con ID: {}", id);

        Catalogo entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductoServicio", "id", id));

        entity.setEstado(EstadoGeneral.ELIMINADO);
        repository.save(entity);

        log.info("Producto/servicio eliminado: {}", id);
    }

    private Catalogo.Tipo parseTipo(String tipo) {
        if (tipo == null || tipo.trim().isEmpty() || tipo.equalsIgnoreCase("ALL")) {
            return null;
        }
        try {
            return Catalogo.Tipo.valueOf(tipo.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Tipo de catálogo no válido: {}", tipo);
            return null;
        }
    }

    private EstadoGeneral parseEstado(String estado) {
        if (estado == null || estado.trim().isEmpty() || estado.equalsIgnoreCase("TODOS")) {
            return null;
        }
        try {
            return EstadoGeneral.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Estado no válido: {}", estado);
            return null;
        }
    }

    private PageResponse<CatalogoResponse> buildPageResponse(Page<Catalogo> page) {
        return PageResponse.fromPage(page.map(mapper::toResponse));
    }
}

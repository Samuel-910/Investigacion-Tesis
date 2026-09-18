package com.pe.articulos.modules.catalogo.service;

import org.springframework.data.domain.Pageable;

import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.catalogo.dto.UnidadMedidaRequest;
import com.pe.articulos.modules.catalogo.dto.UnidadMedidaResponse;

public interface UnidadMedidaService {

    PageResponse<UnidadMedidaResponse> listarActivas(Pageable pageable);

    PageResponse<UnidadMedidaResponse> listarTodos(Pageable pageable);

    UnidadMedidaResponse crear(UnidadMedidaRequest request);

    UnidadMedidaResponse actualizar(Long id, UnidadMedidaRequest request);

    void eliminar(Long id);
}

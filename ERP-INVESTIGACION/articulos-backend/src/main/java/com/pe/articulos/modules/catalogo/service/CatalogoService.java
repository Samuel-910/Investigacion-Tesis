package com.pe.articulos.modules.catalogo.service;

import com.pe.articulos.modules.catalogo.dto.CatalogoRequest;
import com.pe.articulos.modules.catalogo.dto.CatalogoResponse;
import com.pe.articulos.core.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CatalogoService {

        CatalogoResponse crear(CatalogoRequest request);

        CatalogoResponse obtenerPorId(Long id);

        PageResponse<CatalogoResponse> listarTodos(Pageable pageable, String tipo, Long idCategoria, Boolean esGenerico, Boolean manejaLotes, String estado, String tipoAfectacion);

        PageResponse<CatalogoResponse> buscar(String searchTerm, String searchType, String tipo, Long idCategoria, Boolean esGenerico, Boolean manejaLotes, String estado, String tipoAfectacion, Pageable pageable);

        CatalogoResponse actualizar(Long id, CatalogoRequest request);

        void eliminar(Long id);

}

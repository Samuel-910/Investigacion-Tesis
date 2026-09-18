package com.pe.articulos.modules.compras.service;

import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.compras.dto.DetalleCompraResponse;
import org.springframework.data.domain.Pageable;

public interface DetalleCompraService {

    PageResponse<DetalleCompraResponse> listarPorCompra(Long idCompra, Pageable pageable);

    DetalleCompraResponse obtener(Long id);
}

package com.pe.articulos.modules.compras.service;

import com.pe.articulos.modules.compras.dto.IntercambioRequest;
import com.pe.articulos.modules.proveedores.dto.ProveedorResponse;
import org.springframework.data.domain.Pageable;

public interface IntercambioService {
    ProveedorResponse obtenerProveedorPorProductoYLote(Long idProducto, String lote, Pageable pageable);
    void procesarIntercambio(IntercambioRequest request);
}

package com.pe.articulos.modules.caja_general.service;

import com.pe.articulos.modules.caja_general.dto.CajaGeneralMovimientoRequest;
import com.pe.articulos.modules.caja_general.dto.CajaGeneralMovimientoResponse;
import com.pe.articulos.modules.caja_general.dto.CajaGeneralResponse;
import com.pe.articulos.core.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CajaGeneralService {
    CajaGeneralResponse obtenerPorSucursal(Long idSucursal);

    CajaGeneralMovimientoResponse registrarMovimiento(CajaGeneralMovimientoRequest request);

    PageResponse<CajaGeneralMovimientoResponse> listarMovimientos(Long idSucursal, String metodoPago, String query, String searchType, Pageable pageable);
    
    CajaGeneralMovimientoResponse actualizarMovimiento(Long id, CajaGeneralMovimientoRequest request);

    void eliminarMovimiento(Long id);
}

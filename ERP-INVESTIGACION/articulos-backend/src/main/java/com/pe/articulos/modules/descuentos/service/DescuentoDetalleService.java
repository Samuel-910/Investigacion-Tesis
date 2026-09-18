package com.pe.articulos.modules.descuentos.service;

import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.descuentos.dto.DescuentoDetalleDTO;
import org.springframework.data.domain.Pageable;

public interface DescuentoDetalleService {
    PageResponse<DescuentoDetalleDTO> listarPorDescuento(Long idDescuento, Pageable pageable);

    DescuentoDetalleDTO obtenerPorId(Long id);

    DescuentoDetalleDTO guardar(Long idDescuento, DescuentoDetalleDTO dto);

    void eliminar(Long id);
}

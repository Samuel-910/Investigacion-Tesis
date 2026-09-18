package com.pe.articulos.modules.descuentos.service;

import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.descuentos.dto.VentaBeneficioDTO;
import org.springframework.data.domain.Pageable;

public interface VentaBeneficioService {
    PageResponse<VentaBeneficioDTO> listarPorVenta(Long idVenta, Pageable pageable);

    VentaBeneficioDTO obtenerPorId(Long id);

    VentaBeneficioDTO guardar(VentaBeneficioDTO dto);

    void eliminar(Long id);
}

package com.pe.articulos.modules.compras.service;

import java.util.Map;

import com.pe.articulos.core.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.pe.articulos.modules.compras.dto.CronogramaPagoResponseDTO;
import com.pe.articulos.modules.compras.dto.SolicitudPagoCompraRequest;

public interface CronogramaPagosService {
    PageResponse<CronogramaPagoResponseDTO> listarDeudas(Map<String, Object> params, Pageable pageable);

    CronogramaPagoResponseDTO registrarPagoCuota(Long idCronograma, String numeroOperacion, MultipartFile voucher);

    CronogramaPagoResponseDTO solicitarPago(Long idCronograma);

    CronogramaPagoResponseDTO generarCronograma(Long idCompra, SolicitudPagoCompraRequest request);
}

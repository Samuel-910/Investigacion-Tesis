package com.pe.articulos.modules.aprobaciones.service;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.modules.aprobaciones.dto.SolicitudAnulacionDTO;
import com.pe.articulos.modules.aprobaciones.entity.SolicitudesAnulacion;

import java.util.List;

public interface SolicitudAnulacionService {
    ApiResponse<SolicitudAnulacionDTO> solicitar(
            SolicitudesAnulacion.TipoSolicitud tipo,
            Long referenciaId,
            String motivo);

    ApiResponse<List<SolicitudAnulacionDTO>> listarPendientes(String q, String type, Integer page, Integer size, Long idSucursal, Long idPuntoVenta);

    ApiResponse<Void> atender(Long id, boolean aprobada, String observacion);

    ApiResponse<List<SolicitudAnulacionDTO>> listarHistorial(String q, String type, Integer page, Integer size, Long idSucursal, Long idPuntoVenta);
}

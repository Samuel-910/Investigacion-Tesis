package com.pe.articulos.modules.reportes.service;

import com.pe.articulos.modules.reportes.dto.DashboardSaveRequestDTO;
import com.pe.articulos.modules.reportes.entity.DashboardConfig;
import java.util.List;

public interface DashboardConfigService {
    DashboardConfig guardarConfiguracion(DashboardSaveRequestDTO request, Long idUsuario, Long idSucursal,
            String categoria);

    DashboardConfig obtenerConfiguracionActual(Long idUsuario, Long idSucursal, String categoria);

    DashboardConfig obtenerPorId(Long id);

    List<String> obtenerCategorias(Long idUsuario, Long idSucursal);

    void inicializarParaSucursal(Long idSucursal);
}

package com.pe.articulos.modules.reportes.service;

import com.pe.articulos.modules.reportes.dto.DataPuntoDTO;
import com.pe.articulos.modules.reportes.dto.MultiseriesDataDTO;
import com.pe.articulos.modules.reportes.entity.DashboardWidget.*;
import java.util.List;

public interface DynamicReportService {
    List<DataPuntoDTO> obtenerDatos(MetricaReporte metrica, DimensionReporte dimension, String idSucursal);

    MultiseriesDataDTO obtenerDatosMultiseries(MetricaReporte metrica, DimensionReporte dimension,
            String idSucursal);
}

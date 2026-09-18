package com.pe.articulos.modules.reportes.service.strategy;

import com.pe.articulos.modules.reportes.entity.DashboardWidget.MetricaReporte;
import com.pe.articulos.modules.reportes.entity.DashboardWidget.DimensionReporte;

import java.util.List;

public interface ReporteStrategy {
    
    /**
     * Devuelve true si esta estrategia maneja la metrica especificada.
     */
    boolean supports(MetricaReporte metrica);

    /**
     * Obtiene los datos agrupados segun la dimension.
     */
    List<Object[]> obtenerDatosRaw(MetricaReporte metrica, DimensionReporte dimension, Long idSucursal);
}

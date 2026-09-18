package com.pe.articulos.modules.reportes.dto;

import lombok.*;
import com.pe.articulos.modules.reportes.entity.DashboardWidget.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WidgetConfigDTO {
    private String titulo;
    private TipoGrafico tipoGrafico;
    private MetricaReporte metrica;
    private DimensionReporte dimension;
    private Integer orden;
    private Integer columns;
    private Integer height;
}

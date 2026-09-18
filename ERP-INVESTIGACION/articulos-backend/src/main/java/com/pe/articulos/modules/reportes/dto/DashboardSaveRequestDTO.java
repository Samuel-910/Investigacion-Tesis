package com.pe.articulos.modules.reportes.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSaveRequestDTO {
    private String nombre;
    private String categoria;
    private List<WidgetConfigDTO> widgets;
}

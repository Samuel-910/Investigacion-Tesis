package com.pe.articulos.modules.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReporteResumenDTO {
    // KPIs
    private BigDecimal totalVentas;
    private BigDecimal totalCompras;
    private BigDecimal totalVencido;
    private BigDecimal totalPorPagar;

    // Data para gráficos
    private List<DataPuntoDTO> ventasMensuales;
    private List<DataPuntoDTO> comprasMensuales;
    private List<DataPuntoDTO> deudasPorProveedor;
    private List<DataPuntoDTO> topProductosVendidos;
}

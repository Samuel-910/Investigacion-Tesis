package com.pe.articulos.modules.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteDescuentoDetalleDTO {
    private BigDecimal totalDescuentos;
    private BigDecimal porcPromedio;
    private Long cantidadVentas;
    private List<DescuentoItemDTO> detalle;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DescuentoItemDTO {
        private String fecha;
        private String comprobante;
        private String cliente;
        private String motivo;
        private BigDecimal base;
        private BigDecimal descuento;
        private BigDecimal total;
    }
}

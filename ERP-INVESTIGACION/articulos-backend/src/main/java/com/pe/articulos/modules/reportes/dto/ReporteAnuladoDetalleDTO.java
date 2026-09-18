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
public class ReporteAnuladoDetalleDTO {
    private Long cantidadAnulados;
    private BigDecimal montoTotal;
    private List<AnuladoItemDTO> detalle;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnuladoItemDTO {
        private Long idVenta;
        private String fecha;
        private String tipo;
        private String serie;
        private String numero;
        private String doc;
        private String cliente;
        private String motivo;
        private BigDecimal monto;
        private String usuario;
    }
}

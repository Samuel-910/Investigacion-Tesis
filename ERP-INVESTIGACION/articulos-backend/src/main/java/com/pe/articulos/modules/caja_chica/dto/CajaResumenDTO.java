package com.pe.articulos.modules.caja_chica.dto;

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
public class CajaResumenDTO {
    private Long cajaId;
    private String nombre;
    private BigDecimal saldoInicial;
    private List<MetodoPagoResumenDTO> ingresos;
    private List<MetodoPagoResumenDTO> egresos;
    private BigDecimal totalIngresos;
    private BigDecimal totalEgresos;
    private BigDecimal saldoFinalTeorico;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetodoPagoResumenDTO {
        private String metodoPago;
        private BigDecimal monto;
    }
}

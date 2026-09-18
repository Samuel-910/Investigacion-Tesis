package com.pe.articulos.modules.compras.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudPagoCompraRequest {
    private boolean usarSaldoFavor;
    private BigDecimal montoUsarSaldo;
}

package com.pe.articulos.modules.caja_general.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CajaGeneralSaldoResponse {
    private String metodoPago;
    private BigDecimal saldo;
}

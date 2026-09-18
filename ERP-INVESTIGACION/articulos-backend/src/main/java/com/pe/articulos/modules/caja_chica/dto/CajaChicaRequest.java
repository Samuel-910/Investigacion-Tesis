package com.pe.articulos.modules.caja_chica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CajaChicaRequest {
    private String nombre;
    private Long idSucursal;
    private Long idPuntoVenta;
    private BigDecimal saldoInicial;
}

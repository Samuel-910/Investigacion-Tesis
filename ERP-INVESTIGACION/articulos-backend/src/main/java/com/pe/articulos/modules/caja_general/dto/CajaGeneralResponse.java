package com.pe.articulos.modules.caja_general.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CajaGeneralResponse {
    private Long id;
    private String nombre;
    private Long idSucursal;
    private BigDecimal saldoActual;
    private List<CajaGeneralSaldoResponse> saldosPorMetodo;
    private LocalDateTime fechaActualizacion;
}

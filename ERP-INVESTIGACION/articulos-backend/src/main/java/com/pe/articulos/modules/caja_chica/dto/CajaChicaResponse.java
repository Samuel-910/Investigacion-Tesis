package com.pe.articulos.modules.caja_chica.dto;

import com.pe.articulos.modules.caja_chica.entity.CajaChica.EstadoCaja;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CajaChicaResponse {
    private Long id;
    private String nombre;
    private BigDecimal saldoActual;
    private EstadoCaja estado;
    private BigDecimal saldoInicial;
    private BigDecimal saldoCierreReal;
    private Long idPuntoVenta;
    private String idUsuarioCajero;
    private LocalDateTime fechaCierre;
    private LocalDateTime createdAt;
}

package com.pe.articulos.modules.caja_general.dto;

import com.pe.articulos.modules.caja_general.entity.CajaGeneralMovimiento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CajaGeneralMovimientoRequest {
    private Long idSucursal;
    private CajaGeneralMovimiento.TipoMovimiento tipo;
    private BigDecimal monto;
    private String descripcion;
    private String referencia;
    private String metodoPago;
    private String metodoPagoOrigen;
    private String metodoPagoDestino;
    private String usuario;
}

package com.pe.articulos.modules.caja_chica.dto;

import com.pe.articulos.modules.caja_chica.entity.CajaChicaMovimiento.TipoMovimiento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoRequest {
    private Long cajaChicaId;
    private TipoMovimiento tipo;
    private BigDecimal monto;
    private String descripcion;
    private String referencia;
    private String metodoPago;
}

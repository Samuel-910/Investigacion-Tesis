package com.pe.articulos.modules.caja_chica.dto;

import com.pe.articulos.modules.caja_chica.entity.CajaChicaMovimiento.TipoMovimiento;
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
public class MovimientoResponse {
    private Long id;
    private TipoMovimiento tipo;
    private BigDecimal monto;
    private String descripcion;
    private String referencia;
    private String metodoPago;
    private LocalDateTime fecha;
    private String usuario;
}

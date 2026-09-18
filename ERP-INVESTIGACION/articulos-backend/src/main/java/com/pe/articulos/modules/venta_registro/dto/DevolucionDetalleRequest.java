package com.pe.articulos.modules.venta_registro.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class DevolucionDetalleRequest {
    private Long idDetalle;
    private BigDecimal cantidad;
}


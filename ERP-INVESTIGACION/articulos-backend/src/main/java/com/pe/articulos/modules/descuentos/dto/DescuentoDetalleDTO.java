package com.pe.articulos.modules.descuentos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DescuentoDetalleDTO {
    private Long id;
    private Long idCatalogo;
    private String nombreCatalogo;
    private String tipoDescuento;
    private BigDecimal valorDescuento;
    private Integer cantidad;
}

package com.pe.articulos.modules.productos.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AjusteInventarioDetalleResponse {
    private Long id;
    private Long idCatalogo;
    private String productoNombre;
    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
}

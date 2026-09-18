package com.pe.articulos.modules.productos.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AjusteInventarioDetalleRequest {
    private Long idCatalogo;
    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
}

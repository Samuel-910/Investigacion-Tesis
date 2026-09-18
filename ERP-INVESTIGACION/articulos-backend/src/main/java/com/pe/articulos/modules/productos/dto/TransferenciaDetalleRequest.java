package com.pe.articulos.modules.productos.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferenciaDetalleRequest {
    private Long idCatalogo;
    private BigDecimal cantidad;
    // Para envío/recepción se agregan lote y fecha
    private String nroLote;
    private java.time.LocalDate fechaVenc;
}

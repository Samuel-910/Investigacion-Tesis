package com.pe.articulos.modules.productos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaDetalleResponse {
    private Long id;
    private Long idCatalogo;
    private String productoNombre;
    private String productoCodigo;
    private BigDecimal cantidadSolicitada;
    private BigDecimal cantidadEnviada;
    private BigDecimal cantidadRecibida;
    private String nroLote;
    private LocalDate fechaVenc;
    private BigDecimal costoUnitario;
}

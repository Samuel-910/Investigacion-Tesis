package com.pe.articulos.modules.productos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockValorizadoDTO {
    private Long idProducto;
    private String nombre;
    private String laboratorio;
    private String almacen;
    private BigDecimal stock;
    private BigDecimal precioCompra;
    private BigDecimal valorTotal;
}

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
public class MovimientoDiversoDetalleResponse {
    private Long id;
    private Long idCatalogo;
    private String productoNombre;
    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
    private String tipo;
    private String nroLote;
    private LocalDate fechaVencimiento;
    private String observacion;
    private String clasificacionNombre;
    private Long idClasificacion;
    private Long idAlmacen;
}

package com.pe.articulos.modules.productos.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MovimientoDiversoDetalleRequest {
    private String tipo; // INGRESO, SALIDA
    private Long idCatalogo;
    private Long idProducto; // Opcional para salidas
    private Long idAlmacen;
    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
    private String nroLote;
    private LocalDate fechaVenc;
    private Long idClasificacion;
    private String observacion;
}

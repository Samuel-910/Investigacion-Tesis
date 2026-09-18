
package com.pe.articulos.modules.compras.dto;

import com.pe.articulos.modules.catalogo.dto.CatalogoResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleCompraResponse {

    private Long id;
    private CatalogoResponse producto;
    private BigDecimal cantidad;
    private String unidad;
    private Long idUnidadMedida;
    private String descripcion;
    private String lote;
    private LocalDate fechaVencimiento;
    private String presentacion;
    private Integer factorConversion;
    
    // Parent Compra Info
    private Long idCompra;
    private Long idProveedor;
    private String proveedorRazonSocial;
    private String serie;
    private String correlativo;
    private LocalDate fechaEmision;

    private BigDecimal precioUnitario;
    private BigDecimal porcentajeDescuento;
    private BigDecimal porcentajeDescuento2;
    private Boolean esBonificacion;
    private String tipoAfectacion;

    private BigDecimal valorVenta;
    private BigDecimal baseImp;
    private BigDecimal igv;
    private BigDecimal valorExo;
    private BigDecimal valorInaf;
    private BigDecimal igvDescuento;
}

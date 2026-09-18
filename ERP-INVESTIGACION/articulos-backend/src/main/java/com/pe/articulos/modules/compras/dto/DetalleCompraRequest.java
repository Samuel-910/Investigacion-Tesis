
package com.pe.articulos.modules.compras.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class DetalleCompraRequest {

    @NotNull(message = "El producto es obligatorio")
    private Long idProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "La cantidad debe ser mayor a 0")
    private BigDecimal cantidad;

    private String unidad; // Puede venir del front o del producto
    private String descripcion; // Opcional override

    private String lote;
    private String presentacion;
    private LocalDate fechaVencimiento;

    @NotNull(message = "El precio unitario es obligatorio")
    private BigDecimal precioUnitario;

    private BigDecimal porcentajeDescuento;
    private BigDecimal porcentajeDescuento2;
    private Boolean esBonificacion;

    private Long idUnidadMedida;
    private Integer factorConversion;

    private String tipoAfectacion; // GRAVADO_ONEROSO, INAFECTO_ONEROSO

    private BigDecimal baseImp;
    private BigDecimal igv;
    private BigDecimal valorExo;
    private BigDecimal valorInaf;
    private BigDecimal igvDescuento;
}

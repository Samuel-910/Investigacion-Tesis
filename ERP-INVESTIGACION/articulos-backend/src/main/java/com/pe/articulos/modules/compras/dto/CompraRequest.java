
package com.pe.articulos.modules.compras.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompraRequest {

    @NotNull(message = "El proveedor es obligatorio")
    private Long idProveedor;

    @NotNull(message = "La sucursal es obligatoria")
    private Long idSucursal;

    @NotBlank(message = "El tipo de comprobante es obligatorio")
    private String tipoComprobante;

    @NotBlank(message = "La serie es obligatoria")
    private String serie;

    @NotBlank(message = "El correlativo es obligatorio")
    private String correlativo;

    @NotNull(message = "La fecha de emisión es obligatoria")
    private LocalDate fechaEmision;

    private LocalDate fechaVencimiento;
    private String condicionPago;
    private BigDecimal montoPagado; // Nuevo campo para pagos iniciales

    @NotBlank(message = "La moneda es obligatoria")
    private String moneda;

    // Los montos globales se pueden calcular en el backend,
    // pero a veces se envían para validar cuadrar con el FE.
    // Dejaremos opcionales en el request, el servicio los verificará/recalculará.

    private BigDecimal percepcion;
    private BigDecimal ajusteRedondeo;

    private BigDecimal valorVentaGravado;
    private BigDecimal valorVentaExonerado;
    private BigDecimal valorVentaInafecto;
    private BigDecimal subtotal;
    private BigDecimal baseImp;
    private BigDecimal igv;
    private BigDecimal igvDescuento;
    private BigDecimal total;
    private Boolean isAjusteManual;
    private Boolean solicitarFondo; // Flag para petición a Caja General

    @NotEmpty(message = "La compra debe tener al menos un detalle")
    @Valid
    private List<DetalleCompraRequest> detalles;
}

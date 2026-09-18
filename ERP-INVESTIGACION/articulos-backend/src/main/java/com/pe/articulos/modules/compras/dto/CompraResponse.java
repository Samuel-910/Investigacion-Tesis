
package com.pe.articulos.modules.compras.dto;

import com.pe.articulos.modules.proveedores.dto.ProveedorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.pe.articulos.core.enums.EstadoGeneral;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompraResponse {

    private Long id;
    private ProveedorResponse proveedor;
    private String tipoComprobante;
    private String serie;
    private String correlativo;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private String condicionPago;
    private String moneda;

    private BigDecimal valorVentaGravado;
    private BigDecimal valorVentaExonerado;
    private BigDecimal valorVentaInafecto;
    private BigDecimal subtotal;
    private BigDecimal baseImp;
    private BigDecimal igv;
    private BigDecimal igvDescuento;
    private BigDecimal total;
    private BigDecimal percepcion;
    private BigDecimal ajusteRedondeo;
    private BigDecimal totalPagar;

    private EstadoGeneral estado;
    private LocalDateTime fechaRegistro;
    private String nombreGrupo;
    private Boolean solicitarFondo;

    private List<DetalleCompraResponse> detalles;
}

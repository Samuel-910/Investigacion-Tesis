package com.pe.articulos.modules.proveedores.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeudaProveedorResponse {
    private Long id;
    private Long idCuentaProveedor;
    private String proveedorRazonSocial;
    private BigDecimal montoOriginal;
    private BigDecimal saldoPendiente;
    private LocalDateTime fechaEmision;
    private LocalDate fechaVencimiento;
    private String estado;
    private String referenciaCargo;
}

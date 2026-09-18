package com.pe.articulos.modules.proveedores.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaProveedorResponse {
    private Long id;
    private Long idProveedor;
    private String proveedorRazonSocial;
    private BigDecimal saldoTotal;
}

package com.pe.articulos.modules.proveedores.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaProveedorMovimientoResponse {
    private Long id;
    private Long idCuenta;
    private String tipo;
    private BigDecimal monto;
    private String descripcion;
    private LocalDateTime fechaRegistro;
    private String idUser;
}

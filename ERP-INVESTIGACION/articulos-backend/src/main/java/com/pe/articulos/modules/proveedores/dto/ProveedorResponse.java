package com.pe.articulos.modules.proveedores.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProveedorResponse {

    private Long id;
    private String tipoDocIdent;
    private String numDocIdent;
    private String razonSocial;
    private String nombreComercial;
    private String direccion;
    private String email;
    private String telefono;
    private String departamento;
    private String provincia;
    private String distrito;
    private Integer estado;
    private java.math.BigDecimal saldo;
    private Integer plazoDias;
    private LocalDateTime fechaRegistro;
}

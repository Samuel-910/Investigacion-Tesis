package com.pe.articulos.modules.compras.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCreateRequest {

    @NotNull(message = "El proveedor es obligatorio")
    private Long idProveedor;
    @NotNull(message = "La sucursal es obligatoria")
    private Long idSucursal;
    private String nombreGrupo;
    private BigDecimal valorVentaGravado;
    private BigDecimal valorVentaExonerado;
    private BigDecimal valorVentaInafecto;
    private BigDecimal igv;
    private BigDecimal total;

    @NotEmpty(message = "La orden debe tener al menos un detalle")
    @Valid
    private List<DetalleCompraRequest> detalles;
}

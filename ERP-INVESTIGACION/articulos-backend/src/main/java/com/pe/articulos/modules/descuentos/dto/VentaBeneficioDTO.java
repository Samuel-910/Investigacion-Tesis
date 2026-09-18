package com.pe.articulos.modules.descuentos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaBeneficioDTO {
    private Long idVentaBeneficio;
    private String parentesco;
    private String nombrePaciente;
    private Long idVenta;
    private Long idDescuentoDetalle;
    private String nombreBeneficio;
}

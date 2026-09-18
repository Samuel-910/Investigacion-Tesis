package com.pe.articulos.modules.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteCajaDetalleDTO {
    private Long id;
    private String nombre;
    private String usuario;
    private String fechaApertura;
    private String fechaCierre;
    private BigDecimal saldoInicial;
    private BigDecimal saldoTeorico;
    private BigDecimal saldoReal;
    private BigDecimal diferencia;
}

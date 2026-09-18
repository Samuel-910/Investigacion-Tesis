package com.pe.articulos.modules.compras.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.pe.articulos.core.enums.EstadoGeneral;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CronogramaPagoResponseDTO {
    private Long idCronograma;
    private Long idCompra;
    private String proveedorRazonSocial;
    private Integer numeroCuota;
    private LocalDate fechaVencimiento;
    private BigDecimal montoCuota;
    private EstadoGeneral estado;
    private LocalDateTime fechaPagoReal;
    private String comprobantePago;
    private String usuarioCreacion;
    private String usuarioModificacion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}

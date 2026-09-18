package com.pe.articulos.modules.compania.dto;

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
public class CompaniaPersonaVinculoResponseDTO {
    private Long id;
    private Long idCompania;
    private String companiaNombre;
    private Long idPaciente;
    private String pacienteNombreCompleto;
    private String pacienteDni;
    private String nroPoliza;
    private String tipoAfiliacion;
    private String parentesco;
    private LocalDate fechaInicio;
    private LocalDate fechaVencimiento;
    private EstadoGeneral estado;
    private String usuarioCreacion;
    private String usuarioModificacion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}

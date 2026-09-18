package com.pe.articulos.modules.compania.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VinculoPacienteManualDTO {
    private Long idPaciente;
    private String nroPoliza;
    private String tipoAfiliacion;
    private String parentesco;
    private LocalDate fechaInicio;
    private LocalDate fechaVencimiento;
    private Boolean activo;
}

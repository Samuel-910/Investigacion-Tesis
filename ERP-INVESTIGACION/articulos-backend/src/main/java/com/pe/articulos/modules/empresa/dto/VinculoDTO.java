package com.pe.articulos.modules.empresa.dto;

import java.time.LocalDate;
import com.pe.articulos.core.enums.EstadoGeneral;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VinculoDTO {

    @NotNull(message = "El ID de la empresa es obligatorio")
    private Long idEmpresa;

    @NotNull(message = "El ID del personal es obligatorio")
    private Long idPersonal;

    private String cargo;

    private LocalDate fechaInicio;

    private EstadoGeneral estado;
}

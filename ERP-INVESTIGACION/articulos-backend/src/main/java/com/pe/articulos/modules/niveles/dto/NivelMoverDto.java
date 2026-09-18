package com.pe.articulos.modules.niveles.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NivelMoverDto {

    @NotNull(message = "El ID del nivel es obligatorio")
    private Long idNivel;

    private Long idNivelPadreNuevo;

    private Integer ordenNuevo;
}

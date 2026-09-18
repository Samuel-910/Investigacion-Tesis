package com.pe.articulos.modules.empresa.dto;

import java.time.LocalDate;
import com.pe.articulos.core.enums.EstadoGeneral;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaPersonaVinculoDTO {
    private Long id;
    private Long idEmpresa; // Si solo devolvemos IDs, o podríamos anidar DTOs si es necesario.
    private Long idPersonal;
    private String cargo;
    private LocalDate fechaInicio;
    private EstadoGeneral estado;
}

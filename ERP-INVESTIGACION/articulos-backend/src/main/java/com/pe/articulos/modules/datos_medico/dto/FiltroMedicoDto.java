package com.pe.articulos.modules.datos_medico.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.pe.articulos.core.enums.EstadoGeneral;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltroMedicoDto {
    private String nombreMed;
    private String nroCmp;
    private EstadoGeneral estado;
    private String tipo;
    private Long idArea;
    private Long idSucursal;
    private String vacaciones;
    private String tipoMedico;
}
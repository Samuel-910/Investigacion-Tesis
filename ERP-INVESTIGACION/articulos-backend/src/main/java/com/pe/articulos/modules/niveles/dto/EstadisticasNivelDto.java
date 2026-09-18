package com.pe.articulos.modules.niveles.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasNivelDto {

    private Long totalNiveles;
    private Long nivelesActivos;
    private Long nivelesInactivos;
    private Integer nivelMaximoJerarquia;
    private Long totalNivelesRaiz;
    private Long totalNivelesConHijos;
    private Long totalNivelesSinHijos;
}

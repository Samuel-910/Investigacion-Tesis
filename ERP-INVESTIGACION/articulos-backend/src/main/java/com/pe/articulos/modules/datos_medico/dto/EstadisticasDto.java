package com.pe.articulos.modules.datos_medico.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasDto {
    private Long totalActivos;
    private Long totalInactivos;
    private Long totalSuspendidos;
    private Long totalMedicos;
    private Long totalEnfermeras;
    private Long totalTecnicos;
    private Long totalEnVacaciones;
    private Long totalPorCapacitar;
}
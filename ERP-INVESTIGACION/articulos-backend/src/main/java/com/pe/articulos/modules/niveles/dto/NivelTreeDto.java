package com.pe.articulos.modules.niveles.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.pe.articulos.core.enums.EstadoGeneral;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NivelTreeDto {

    private Long idNivel;
    private String nombre;
    private String numNivel;
    private String tipo;
    private EstadoGeneral estado;
    private Integer nivelJerarquia;
    private Integer orden;

    private Long idNivelPadre;
    private boolean tieneHijos;
    private boolean expandido = false;

    // Lista de hijos (estructura recursiva)
    private List<NivelTreeDto> hijos = new ArrayList<>();

    // Campos adicionales para UI
    private String icono;
    private String color;
}
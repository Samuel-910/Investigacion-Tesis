package com.pe.articulos.modules.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.pe.articulos.core.enums.EstadoGeneral;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnidadMedidaResponse {
    private Long id;
    private String nombre;
    private String simbolo;
    private String codigoSunat;
    private EstadoGeneral estado;
    private Boolean esAgrupador;
}

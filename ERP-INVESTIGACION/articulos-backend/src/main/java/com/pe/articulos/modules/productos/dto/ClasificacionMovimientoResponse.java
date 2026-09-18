package com.pe.articulos.modules.productos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClasificacionMovimientoResponse {
    private Long id;
    private String nombre;
    private String tipo;
    private String estado;
}

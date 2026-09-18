package com.pe.articulos.modules.documentos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoFormatoDTO {
    private Long id;
    private String nombre;
    private Integer anchoPx;
    private Integer altoPx;
    private String descripcion;
    private Integer estado;
}

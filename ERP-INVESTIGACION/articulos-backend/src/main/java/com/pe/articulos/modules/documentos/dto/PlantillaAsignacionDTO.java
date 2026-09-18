package com.pe.articulos.modules.documentos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaAsignacionDTO {
    private Long id;
    private String modulo;
    private PlantillaDTO plantilla;
    private PuntoDocumentoDTO puntoDocumento;
    private TipoDocumentoDTO tipoDocumento;
    private String descripcion;
    private Integer estado;
}

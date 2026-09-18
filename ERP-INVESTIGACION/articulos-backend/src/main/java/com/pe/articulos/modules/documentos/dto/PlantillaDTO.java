package com.pe.articulos.modules.documentos.dto;

import com.pe.articulos.modules.documentos.entities.Modulo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaDTO {
    private Long id;
    private String nombre;
    private String htmlContenido;
    private String htmlTraducido;
    private String cssEstilo;
    private DocumentoFormatoDTO formato;
    private String orientacion;
    private Modulo modulo;
    private TipoDocumentoDTO tipoDocumento;
    private boolean isDefault;
    private Integer estado;
}

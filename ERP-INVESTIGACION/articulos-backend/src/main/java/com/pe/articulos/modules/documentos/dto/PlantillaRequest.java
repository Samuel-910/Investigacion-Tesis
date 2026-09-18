package com.pe.articulos.modules.documentos.dto;

import com.pe.articulos.modules.documentos.entities.Modulo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    private String htmlContenido;
    private String htmlTraducido;
    private String cssEstilo;
    private Long formatoId;
    private String orientacion;
    private Modulo modulo;
    private String tipoDocumento;
    private boolean isDefault;
}

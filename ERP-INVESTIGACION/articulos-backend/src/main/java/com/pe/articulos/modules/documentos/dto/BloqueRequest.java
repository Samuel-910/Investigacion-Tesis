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
public class BloqueRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    private String htmlContenido;
    private String cssEstilo;
    private String categoria;
    private java.util.List<Modulo> modulos;
}

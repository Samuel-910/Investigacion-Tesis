package com.pe.articulos.modules.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnidadMedidaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String nombre;

    @NotBlank(message = "El símbolo es obligatorio")
    @Size(max = 10, message = "El símbolo no puede exceder 10 caracteres")
    private String simbolo;

    @Size(max = 10, message = "El código SUNAT no puede exceder 10 caracteres")
    private String codigoSunat;

    @Builder.Default
    private Boolean esAgrupador = false;
}

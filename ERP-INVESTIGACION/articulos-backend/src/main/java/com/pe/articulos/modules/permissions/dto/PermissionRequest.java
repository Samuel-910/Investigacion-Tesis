package com.pe.articulos.modules.permissions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequest {

    @NotBlank(message = "El nombre del permiso es requerido")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String name;

    @NotBlank(message = "La descripción es requerida")
    @Size(max = 255, message = "La descripción no puede exceder los 255 caracteres")
    private String description;

    @NotBlank(message = "El módulo es requerido")
    @Size(max = 50, message = "El módulo no puede exceder los 50 caracteres")
    private String module;

    private Boolean active = true;
}
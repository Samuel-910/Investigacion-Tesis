package com.pe.articulos.modules.auth.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectSucursalRequest {

    @NotNull(message = "El ID de usuario es obligatorio")
    private Long userId;

    @NotNull(message = "El ID de sucursal es obligatorio")
    private Long sucursalId;

    @NotBlank(message = "La contraseña es requerida")
    private String password;
}

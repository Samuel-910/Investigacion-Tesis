package com.pe.articulos.modules.auth.service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectPuntoRequest {
    @NotNull(message = "El ID de usuario es obligatorio")
    private Long userId;

    @NotNull(message = "El ID del punto de venta es obligatorio")
    private Long puntoId;

    @NotNull(message = "La contraseña es obligatoria")
    private String password;
}

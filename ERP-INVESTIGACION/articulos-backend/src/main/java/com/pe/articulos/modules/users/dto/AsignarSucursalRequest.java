package com.pe.articulos.modules.users.dto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignarSucursalRequest {
    @NotNull(message = "El ID de sucursal es obligatorio")
    private Long sucursalId;
    private String motivo; 
}


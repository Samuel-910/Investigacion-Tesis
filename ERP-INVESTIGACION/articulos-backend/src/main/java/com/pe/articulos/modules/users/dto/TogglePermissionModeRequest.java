package com.pe.articulos.modules.users.dto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TogglePermissionModeRequest {
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long userId; 
    @NotNull(message = "El modo de permisos es obligatorio")
    private Boolean useDirectPermissions;
}

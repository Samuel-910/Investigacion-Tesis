package com.pe.articulos.modules.users.dto;
import java.time.LocalDateTime;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrantDirectPermissionRequest {
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long userId; 
    @NotNull(message = "El ID del permiso es obligatorio")
    private Long permissionId;
    @Size(max = 500, message = "La razón no puede tener más de 500 caracteres")
    private String reason;
    @Future(message = "La fecha de expiración debe estar en el futuro")
    private LocalDateTime expiresAt;
}

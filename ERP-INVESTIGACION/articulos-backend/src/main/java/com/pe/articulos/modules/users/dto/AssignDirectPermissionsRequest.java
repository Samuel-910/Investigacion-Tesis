package com.pe.articulos.modules.users.dto;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignDirectPermissionsRequest {
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long userId; 
    @NotEmpty(message = "Debe proporcionar al menos un permiso")
    private List<Long> permissionIds;
    @Size(max = 500, message = "La razón no puede tener más de 500 caracteres")
    private String reason;
    @Future(message = "La fecha de expiración debe estar en el futuro")
    private LocalDateTime expiresAt;
}

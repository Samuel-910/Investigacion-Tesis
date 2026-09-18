package com.pe.articulos.modules.users.dto;
import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignarMultiplesSucursalesRequest {
    @NotNull(message = "La lista de sucursales no puede ser nula")
    @NotEmpty(message = "Debe asignar al menos una sucursal")
    private List<Long> sucursalIds;
    private String motivo;
}


package com.pe.articulos.modules.almacen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.pe.articulos.core.enums.EstadoGeneral;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlmacenRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    private String codigo;
    private String ubicacion;
    private Boolean esPrincipal;
    private String responsable;
    private EstadoGeneral estado;
    
    @NotNull(message = "El ID de la sucursal es obligatorio")
    private Long idSucursal;
}

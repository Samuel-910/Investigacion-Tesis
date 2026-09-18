package com.pe.articulos.modules.sucursal.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.pe.articulos.core.enums.EstadoGeneral;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SucursalDto {

    private Long idSucursal;

    @NotBlank(message = "El nombre de la sucursal es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombreSucursal;

    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    private String direccion;

    @Pattern(regexp = "^[0-9]{1,9}$", message = "Teléfono inválido, máximo 9 dígitos")
    private String telefono;

    @Pattern(regexp = "^[0-9]{1,9}$", message = "Celular inválido, máximo 9 dígitos")
    private String celular;

    @NotNull(message = "El estado es obligatorio")
    private EstadoGeneral estado;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Datos adicionales
    private Integer cantidadPersonal;
}
package com.pe.articulos.modules.proveedores.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProveedorRequest {

    @NotBlank(message = "El tipo de documento es obligatorio")
    private String tipoDocIdent;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 20, message = "El número de documento no puede exceder los 20 caracteres")
    private String numDocIdent;

    @NotBlank(message = "La razón social es obligatoria")
    private String razonSocial;

    @Size(max = 255, message = "El nombre comercial no puede exceder los 255 caracteres")
    private String nombreComercial;

    private String direccion;

    @Email(message = "El formato del email no es válido")
    private String email;

    private String telefono;

    private String departamento;

    private String provincia;

    private String distrito;

    private Integer plazoDias;

    private Integer estado;
}

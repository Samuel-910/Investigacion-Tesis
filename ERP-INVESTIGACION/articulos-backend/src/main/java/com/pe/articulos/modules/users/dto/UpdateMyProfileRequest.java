package com.pe.articulos.modules.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMyProfileRequest {
    
    @NotBlank(message = "El primer nombre es obligatorio")
    private String firstName;
    
    @NotBlank(message = "El apellido paterno es obligatorio")
    private String lastName;

    private String apemat;

    @Email(message = "Debe proporcionar un email válido")
    private String email;

    private String phone;
}

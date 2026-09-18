package com.pe.articulos.modules.auth.service.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private String type;
    private Long userId;
    private Long idPersonal;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String sexo;
    private Set<String> roles;
    private Set<String> permissions;
    private Long sessionTimeoutMs;

    private Boolean requiresSucursalSelection;
    private java.util.List<com.pe.articulos.modules.sucursal.dto.SucursalDto> sucursalesDisponibles;

    private Long sucursalId;
    private String sucursalNombre;

    private Long puntoId;
    private String puntoNombre;

    private java.util.List<AccesoDto> accesos;
}
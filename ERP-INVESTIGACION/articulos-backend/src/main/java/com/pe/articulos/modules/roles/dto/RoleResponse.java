package com.pe.articulos.modules.roles.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.pe.articulos.modules.permissions.dto.PermissionResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {

    private Long id;
    private String name;
    private String description;
    private Boolean active;
    private long userCount;
    private Set<PermissionResponse> permissions;
    private Long idAcceso;
    private String nombreAcceso;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
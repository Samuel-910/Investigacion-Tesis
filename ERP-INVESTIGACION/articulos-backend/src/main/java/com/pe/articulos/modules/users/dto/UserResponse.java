package com.pe.articulos.modules.users.dto;
import java.time.LocalDateTime;
import java.util.List;
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
public class UserResponse {
    private Long id;
    private Long idPersonal;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String nombreCompleto;
    private String numdoc;
    private String ruc;
    private String phone;
    private Boolean active;
    private Set<String> roles;
    private Set<String> permissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
    private Long roleId;
    private String roleName;
    private Boolean useDirectPermissions;
    private List<PermissionResponse> rolePermissions;
    private List<UserDirectPermissionResponse> directPermissions;
    private List<PermissionResponse> effectivePermissions;
    private Boolean isCompania;
}

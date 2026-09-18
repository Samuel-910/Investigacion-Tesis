package com.pe.articulos.modules.users.dto;
import com.pe.articulos.modules.permissions.dto.PermissionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWithPermissionsResponse {
    
    private Long id;
    private String login;
    private String nombre;
    private String apepat;
    private String apemat;
    private String fullName;
    private String email;
    private Boolean active;
    
    private Set<String> roles;
    private List<PermissionResponse> rolePermissions;
    
    private Boolean useDirectPermissions;
    private String permissionMode; 
    
    private List<UserDirectPermissionResponse> directPermissions;
    private Long activeDirectPermissionsCount;
    
    private List<PermissionResponse> effectivePermissions;
    private Long effectivePermissionsCount;
    
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}

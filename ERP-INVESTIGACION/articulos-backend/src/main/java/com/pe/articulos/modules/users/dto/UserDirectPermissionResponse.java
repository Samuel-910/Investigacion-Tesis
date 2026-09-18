package com.pe.articulos.modules.users.dto;
import java.time.LocalDateTime;
import com.pe.articulos.modules.permissions.dto.PermissionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDirectPermissionResponse {
    private Long id;
    private PermissionResponse permission;
    
    private Long grantedBy;
    private String grantedByLogin;
    private String grantedByFullName;
    
    private LocalDateTime grantedAt;
    private String reason;
    private LocalDateTime expiresAt;
    private Boolean active;
    private Boolean isExpired;
    
    private Long daysUntilExpiration;
}

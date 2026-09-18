package com.pe.articulos.modules.users.dto;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryResponse {
    private Long id;
    private String login;
    private String fullName;
    private String email;
    private Boolean active;
    private Set<String> roleNames;
    private Boolean useDirectPermissions;
    private Long effectivePermissionsCount;
}

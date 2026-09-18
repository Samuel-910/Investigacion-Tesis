package com.pe.articulos.modules.users.service;
import com.pe.articulos.modules.users.dto.AssignDirectPermissionsRequest;
import com.pe.articulos.modules.users.dto.GrantDirectPermissionRequest;
import com.pe.articulos.modules.users.dto.UserDirectPermissionResponse;
import com.pe.articulos.modules.users.dto.UserWithPermissionsResponse;
import com.pe.articulos.modules.permissions.dto.PermissionResponse;
import java.util.List;
public interface UserPermissionService {
        
        
        UserWithPermissionsResponse assignDirectPermissions(
                        AssignDirectPermissionsRequest request,
                        Long currentUserid);
        
        UserDirectPermissionResponse grantDirectPermission(
                        GrantDirectPermissionRequest request,
                        Long currentUserid);
        
        UserWithPermissionsResponse removeDirectPermissions(
                        Long id,
                        List<Long> permissionIds);
        
        void revokeDirectPermission(Long id, Long permissionId);
        
        void clearAllDirectPermissions(Long id);
        
        
        List<UserDirectPermissionResponse> getDirectPermissions(Long id);
        
        List<PermissionResponse> getEffectivePermissions(Long id);
        
        UserWithPermissionsResponse getUserWithPermissions(Long id);
        
        
        UserWithPermissionsResponse togglePermissionMode(
                        Long id,
                        Boolean useDirectPermissions);
        
        boolean hasPermission(Long id, String permissionName);
        
        
        int deactivateExpiredPermissions();
        
        List<UserDirectPermissionResponse> getExpiringPermissions(int days);
}

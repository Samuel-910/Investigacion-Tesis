package com.pe.articulos.modules.users.controller;
import com.pe.articulos.core.exception.BadRequestException;
import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.modules.users.dto.AssignDirectPermissionsRequest;
import com.pe.articulos.modules.users.dto.GrantDirectPermissionRequest;
import com.pe.articulos.modules.users.dto.TogglePermissionModeRequest;
import com.pe.articulos.modules.users.dto.UserDirectPermissionResponse;
import com.pe.articulos.modules.users.dto.UserWithPermissionsResponse;
import com.pe.articulos.modules.users.entity.DatosPersonales;
import com.pe.articulos.modules.users.service.UserPermissionService;
import com.pe.articulos.modules.permissions.dto.PermissionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class UserPermissionController {
        private final UserPermissionService userPermissionService;
        
        
        
        @PostMapping("/permissions/assign")
        @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
        public ResponseEntity<ApiResponse<UserWithPermissionsResponse>> assignDirectPermissions(
                        @Valid @RequestBody AssignDirectPermissionsRequest request,
                        Authentication authentication) {
                Long currentUserId = getCurrentUserId(authentication);
                UserWithPermissionsResponse user = userPermissionService.assignDirectPermissions(
                                request, currentUserId);
                return ResponseEntity.ok(ApiResponse.success(user, "Permisos directos asignados exitosamente"));
        }
        
        @PostMapping("/permissions/grant")
        @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
        public ResponseEntity<ApiResponse<UserDirectPermissionResponse>> grantDirectPermission(
                        @Valid @RequestBody GrantDirectPermissionRequest request,
                        Authentication authentication) {
                Long currentUserId = getCurrentUserId(authentication);
                UserDirectPermissionResponse permission = userPermissionService
                                .grantDirectPermission(request, currentUserId);
                return ResponseEntity.ok(ApiResponse.success(permission, "Permiso directo otorgado exitosamente"));
        }
        
        @DeleteMapping("/{id}/permissions")
        @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
        public ResponseEntity<ApiResponse<UserWithPermissionsResponse>> removeDirectPermissions(
                        @PathVariable Long id,
                        @RequestBody List<Long> permissionIds) {
                
                if (id == null || id <= 0) {
                        throw new BadRequestException("El ID del usuario debe ser un número positivo");
                }
                if (permissionIds == null || permissionIds.isEmpty()) {
                        throw new BadRequestException("Debe proporcionar al menos un ID de permiso");
                }
                UserWithPermissionsResponse user = userPermissionService.removeDirectPermissions(
                                id, permissionIds);
                return ResponseEntity.ok(ApiResponse.success(user, "Permisos removidos exitosamente"));
        }
        
        @DeleteMapping("/{id}/permissions/{permissionId}")
        @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
        public ResponseEntity<ApiResponse<Void>> revokeDirectPermission(
                        @PathVariable Long id,
                        @PathVariable Long permissionId) {
                
                if (id == null || id <= 0) {
                        throw new BadRequestException("El ID del usuario debe ser un número positivo");
                }
                if (permissionId == null || permissionId <= 0) {
                        throw new BadRequestException("El ID del permiso debe ser un número positivo");
                }
                userPermissionService.revokeDirectPermission(id, permissionId);
                return ResponseEntity.ok(ApiResponse.success("Permiso revocado exitosamente"));
        }
        
        @DeleteMapping("/{id}/permissions/all")
        @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
        public ResponseEntity<ApiResponse<Void>> clearAllDirectPermissions(
                        @PathVariable Long id) {
                
                if (id == null || id <= 0) {
                        throw new BadRequestException("El ID del usuario debe ser un número positivo");
                }
                userPermissionService.clearAllDirectPermissions(id);
                return ResponseEntity.ok(ApiResponse.success("Todos los permisos directos eliminados exitosamente"));
        }
        
        
        
        @GetMapping("/{id}/permissions/direct")
        @PreAuthorize("hasAuthority('USUARIO_LEER')")
        public ResponseEntity<ApiResponse<List<UserDirectPermissionResponse>>> getDirectPermissions(
                        @PathVariable Long id) {
                
                if (id == null || id <= 0) {
                        throw new BadRequestException("El ID del usuario debe ser un número positivo");
                }
                List<UserDirectPermissionResponse> permissions = userPermissionService
                                .getDirectPermissions(id);
                return ResponseEntity.ok(ApiResponse.success(permissions, "Permisos directos obtenidos exitosamente"));
        }
        
        @GetMapping("/{id}/permissions/effective")
        @PreAuthorize("hasAuthority('USUARIO_LEER')")
        public ResponseEntity<ApiResponse<List<PermissionResponse>>> getEffectivePermissions(
                        @PathVariable Long id) {
                
                if (id == null || id <= 0) {
                        throw new BadRequestException("El ID del usuario debe ser un número positivo");
                }
                List<PermissionResponse> permissions = userPermissionService
                                .getEffectivePermissions(id);
                return ResponseEntity.ok(ApiResponse.success(permissions, "Permisos efectivos obtenidos exitosamente"));
        }
        
        @GetMapping("/{id}/with-permissions")
        @PreAuthorize("hasAuthority('USUARIO_LEER')")
        public ResponseEntity<ApiResponse<UserWithPermissionsResponse>> getUserWithPermissions(
                        @PathVariable Long id) {
                
                if (id == null || id <= 0) {
                        throw new BadRequestException("El ID del usuario debe ser un número positivo");
                }
                UserWithPermissionsResponse user = userPermissionService.getUserWithPermissions(id);
                return ResponseEntity.ok(ApiResponse.success(user, "Usuario con permisos obtenido exitosamente"));
        }
        
        
        
        @PutMapping("/permissions/toggle-mode")
        @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
        public ResponseEntity<ApiResponse<UserWithPermissionsResponse>> togglePermissionMode(
                        @Valid @RequestBody TogglePermissionModeRequest request) {
                
                if (request.getUserId() == null || request.getUserId() <= 0) {
                        throw new BadRequestException("El ID del usuario debe ser un número positivo");
                }
                UserWithPermissionsResponse user = userPermissionService.togglePermissionMode(
                                request.getUserId(),
                                request.getUseDirectPermissions());
                String mode = request.getUseDirectPermissions() ? "DIRECTO" : "ROL";
                return ResponseEntity.ok(ApiResponse.success(user, "Modo de permisos actualizado a: " + mode));
        }
        
        @GetMapping("/{id}/has-permission/{permissionName}")
        @PreAuthorize("hasAuthority('USUARIO_LEER')")
        public ResponseEntity<ApiResponse<Map<String, Object>>> hasPermission(
                        @PathVariable Long id,
                        @PathVariable String permissionName) {
                
                if (id == null || id <= 0) {
                        throw new BadRequestException("El ID del usuario debe ser un número positivo");
                }
                if (permissionName == null || permissionName.trim().isEmpty()) {
                        throw new BadRequestException("El nombre del permiso no puede estar vacío");
                }
                boolean hasPermission = userPermissionService.hasPermission(id, permissionName);
                return ResponseEntity.ok(ApiResponse.success(Map.of(
                                                "hasPermission", hasPermission,
                                                "permissionName", permissionName,
                                                "userId", id), "Verificación realizada exitosamente"));
        }
        
        
        
        @PostMapping("/permissions/deactivate-expired")
        @PreAuthorize("hasAuthority('PERMISO_ACTUALIZAR')")
        public ResponseEntity<ApiResponse<Map<String, Object>>> deactivateExpiredPermissions() {
                int count = userPermissionService.deactivateExpiredPermissions();
                return ResponseEntity.ok(ApiResponse.success(Map.of(
                                                "deactivatedCount", count,
                                                "timestamp", java.time.LocalDateTime.now()), count + " permisos expirados desactivados"));
        }
        
        @GetMapping("/permissions/expiring")
        @PreAuthorize("hasAuthority('USUARIO_LEER')")
        public ResponseEntity<ApiResponse<List<UserDirectPermissionResponse>>> getExpiringPermissions(
                        @RequestParam(defaultValue = "7") int days) {
                
                if (days <= 0) {
                        throw new BadRequestException("Los días deben ser un número positivo");
                }
                if (days > 365) {
                        throw new BadRequestException("El rango máximo es de 365 días");
                }
                List<UserDirectPermissionResponse> permissions = userPermissionService
                                .getExpiringPermissions(days);
                return ResponseEntity.ok(ApiResponse.success(permissions, "Permisos que expiran en los próximos " + days + " días"));
        }
        
        
        
        @GetMapping("/{id}/permissions/stats")
        @PreAuthorize("hasAuthority('USUARIO_LEER')")
        public ResponseEntity<ApiResponse<Map<String, Object>>> getPermissionStats(
                        @PathVariable Long id) {
                
                if (id == null || id <= 0) {
                        throw new BadRequestException("El ID del usuario debe ser un número positivo");
                }
                UserWithPermissionsResponse user = userPermissionService.getUserWithPermissions(id);
                Map<String, Object> stats = Map.of(
                                "id", user.getId(),
                                "login", user.getLogin(),
                                "fullName", user.getFullName(),
                                "permissionMode", user.getPermissionMode(),
                                "useDirectPermissions",
                                user.getUseDirectPermissions() != null ? user.getUseDirectPermissions() : false,
                                "rolesCount", user.getRoles().size(),
                                "rolePermissionsCount", user.getRolePermissions().size(),
                                "directPermissionsCount", user.getDirectPermissions().size(),
                                "activeDirectPermissionsCount", user.getActiveDirectPermissionsCount(),
                                "effectivePermissionsCount", user.getEffectivePermissionsCount());
                return ResponseEntity.ok(ApiResponse.success(stats, "Estadísticas obtenidas exitosamente"));
        }
        
        @PostMapping("/{sourceId}/permissions/copy-to/{targetId}")
        @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
        public ResponseEntity<ApiResponse<UserWithPermissionsResponse>> copyPermissions(
                        @PathVariable Long sourceId,
                        @PathVariable Long targetId,
                        @RequestParam(required = false) String reason,
                        Authentication authentication) {
                
                if (sourceId == null || sourceId <= 0) {
                        throw new BadRequestException("El ID del usuario origen debe ser un número positivo");
                }
                if (targetId == null || targetId <= 0) {
                        throw new BadRequestException("El ID del usuario destino debe ser un número positivo");
                }
                if (sourceId.equals(targetId)) {
                        throw new BadRequestException("El usuario origen y destino no pueden ser el mismo");
                }
                
                List<UserDirectPermissionResponse> sourcePermissions = userPermissionService
                                .getDirectPermissions(sourceId);
                
                if (sourcePermissions.isEmpty()) {
                        throw new BadRequestException("El usuario origen no tiene permisos directos para copiar");
                }
                
                List<Long> permissionIds = sourcePermissions.stream()
                                .map(p -> p.getPermission().getId())
                                .toList();
                
                AssignDirectPermissionsRequest request = new AssignDirectPermissionsRequest();
                request.setUserId(targetId);
                request.setPermissionIds(permissionIds);
                request.setReason(reason != null ? reason : "Copiado de usuario " + sourceId);
                
                Long currentUserId = getCurrentUserId(authentication);
                UserWithPermissionsResponse targetUser = userPermissionService
                                .assignDirectPermissions(request, currentUserId);
                return ResponseEntity.ok(ApiResponse.success(targetUser, "Permisos copiados exitosamente de usuario " + sourceId + " a " + targetId));
        }
        
        
        
        private Long getCurrentUserId(Authentication authentication) {
                if (authentication == null) {
                        throw new BadRequestException("Usuario no autenticado");
                }
                if (!(authentication.getPrincipal() instanceof DatosPersonales)) {
                        throw new BadRequestException("Tipo de autenticación inválido");
                }
                DatosPersonales user = (DatosPersonales) authentication.getPrincipal();
                if (user.getId() == null) {
                        throw new BadRequestException("ID de usuario no disponible");
                }
                return user.getId();
        }
        
        @GetMapping("/{id}/permissions")
        @PreAuthorize("hasAuthority('USUARIO_LEER')")
        public ResponseEntity<ApiResponse<List<String>>> getUserPermissionNames(
                        @PathVariable Long id) {
                if (id == null || id <= 0) {
                        throw new BadRequestException("El ID del usuario debe ser un número positivo");
                }
                List<PermissionResponse> permissions = userPermissionService.getEffectivePermissions(id);
                List<String> permissionNames = permissions.stream()
                                .map(PermissionResponse::getName)
                                .toList();
                return ResponseEntity.ok(ApiResponse.success(permissionNames, "Permisos del usuario obtenidos exitosamente"));
        }
        
        @GetMapping("/{id}/modules")
        @PreAuthorize("hasAuthority('USUARIO_LEER')")
        public ResponseEntity<ApiResponse<List<String>>> getUserModules(
                        @PathVariable Long id) {
                if (id == null || id <= 0) {
                        throw new BadRequestException("El ID del usuario debe ser un número positivo");
                }
                List<PermissionResponse> permissions = userPermissionService.getEffectivePermissions(id);
                List<String> modules = permissions.stream()
                                .map(PermissionResponse::getModule)
                                .filter(module -> module != null && !module.isEmpty())
                                .distinct()
                                .sorted()
                                .toList();
                return ResponseEntity.ok(ApiResponse.success(modules, "Módulos del usuario obtenidos exitosamente"));
        }
        
        @GetMapping("/{id}/crud")
        @PreAuthorize("hasAuthority('USUARIO_LEER')")
        public ResponseEntity<ApiResponse<Map<String, Boolean>>> getModuleCRUDPermissions(
                        @PathVariable Long id,
                        @RequestParam String modulo) {
                if (id == null || id <= 0) {
                        throw new BadRequestException("El ID del usuario debe ser un número positivo");
                }
                if (modulo == null || modulo.trim().isEmpty()) {
                        throw new BadRequestException("El nombre del módulo no puede estar vacío");
                }
                List<PermissionResponse> permissions = userPermissionService.getEffectivePermissions(id);
                java.util.Set<String> permissionNames = permissions.stream()
                                .map(PermissionResponse::getName)
                                .collect(java.util.stream.Collectors.toSet());
                Map<String, Boolean> crudPermissions = new java.util.HashMap<>();
                crudPermissions.put("canCreate", permissionNames.contains(modulo + "_CREAR"));
                crudPermissions.put("canRead", permissionNames.contains(modulo + "_LEER"));
                crudPermissions.put("canUpdate", permissionNames.contains(modulo + "_ACTUALIZAR"));
                crudPermissions.put("canDelete", permissionNames.contains(modulo + "_ELIMINAR"));
                return ResponseEntity.ok(ApiResponse.success(crudPermissions, "Permisos CRUD del módulo obtenidos exitosamente"));
        }
}

package com.pe.articulos.modules.users.controller;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.pe.articulos.core.exception.BadRequestException;
import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.users.dto.ChangePasswordRequest;
import com.pe.articulos.modules.users.dto.CreateUserRequest;
import com.pe.articulos.modules.users.dto.ResetPasswordRequest;
import com.pe.articulos.modules.users.dto.UpdateUserRequest;
import com.pe.articulos.modules.users.dto.UpdateMyProfileRequest;
import com.pe.articulos.modules.users.dto.UserResponse;
import com.pe.articulos.modules.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;
    
    @PostMapping
    @PreAuthorize("hasAuthority('USUARIO_CREAR')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "Usuario creado exitosamente"));
    }
    
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile() {
        UserResponse user = userService.getMe();
        return ResponseEntity.ok(ApiResponse.success(user, "Perfil obtenido exitosamente"));
    }
    
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @Valid @RequestBody UpdateMyProfileRequest request) {
        UserResponse user = userService.updateMe(request);
        return ResponseEntity.ok(ApiResponse.success(user, "Perfil actualizado exitosamente"));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(user, "Usuario actualizado exitosamente"));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "Usuario obtenido exitosamente"));
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @PageableDefault(page = 0, size = 10, sort = "nombre") Pageable pageable) {
        PageResponse<UserResponse> response = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Usuarios obtenidos exitosamente"));
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getActiveUsers(
            @PageableDefault(page = 0, size = 10, sort = "nombre") Pageable pageable) {
        PageResponse<UserResponse> response = userService.getActiveUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Usuarios activos obtenidos exitosamente"));
    }
    
    @GetMapping("/role/{roleName}")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsersByRole(
            @PathVariable String roleName,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new BadRequestException("El nombre del rol no puede estar vacío");
        }
        PageResponse<UserResponse> response = userService.getUsersByRole(roleName, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Usuarios con rol " + roleName + " obtenidos exitosamente"));
    }

    @GetMapping("/modulo/{moduloNombre}")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsersByModulo(
            @PathVariable String moduloNombre,
            @PageableDefault(page = 0, size = 10, sort = "nombre") Pageable pageable) {
        
        if (moduloNombre == null || moduloNombre.trim().isEmpty()) {
            throw new BadRequestException("El nombre del módulo no puede estar vacío");
        }
        PageResponse<UserResponse> response = userService.getUsersByModulo(moduloNombre, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Usuarios del módulo " + moduloNombre + " obtenidos exitosamente"));
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> searchUsers(
            @RequestParam(required = false, defaultValue = "") String q,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        
        if (q == null || q.trim().isEmpty()) {
            return getActiveUsers(pageable);
        }
        
        if (q.trim().length() < 1) {
            return getActiveUsers(pageable);
        }
        PageResponse<UserResponse> response = userService.searchByName(q.trim(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Búsqueda realizada exitosamente"));
    }
    
    @GetMapping("/filter")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> searchByFilter(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false, defaultValue = "ALL") String type,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        PageResponse<UserResponse> response = userService.searchByFilter(query, type, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Búsqueda realizada exitosamente"));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIO_ELIMINAR')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Usuario eliminado exitosamente"));
    }
    
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('USUARIO_BLOQUEAR')")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        userService.toggleUserStatus(id, active);
        String message = active ? "Usuario activado exitosamente" : "Usuario desactivado exitosamente";
        return ResponseEntity.ok(ApiResponse.success(message));
    }
    
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<UserResponse>> assignRoles(
            @PathVariable Long id,
            @RequestBody List<Long> roleIds) {
        
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BadRequestException("Debe proporcionar al menos un ID de rol");
        }
        
        for (Long roleId : roleIds) {
            if (roleId == null || roleId <= 0) {
                throw new BadRequestException("Los IDs de roles deben ser números positivos");
            }
        }
        UserResponse user = userService.assignRoles(id, roleIds);
        return ResponseEntity.ok(ApiResponse.success(user, "Roles asignados exitosamente"));
    }
    
    @DeleteMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<UserResponse>> removeRoles(
            @PathVariable Long id,
            @RequestBody List<Long> roleIds) {
        
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BadRequestException("Debe proporcionar al menos un ID de rol para remover");
        }
        
        for (Long roleId : roleIds) {
            if (roleId == null || roleId <= 0) {
                throw new BadRequestException("Los IDs de roles deben ser números positivos");
            }
        }
        UserResponse user = userService.removeRoles(id, roleIds);
        return ResponseEntity.ok(ApiResponse.success(user, "Roles removidos exitosamente"));
    }
    
    @PostMapping("/{id}/puntos")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<UserResponse>> assignPuntos(
            @PathVariable Long id,
            @RequestBody List<Long> puntoIds) {
        UserResponse user = userService.assignPuntos(id, puntoIds);
        return ResponseEntity.ok(ApiResponse.success(user, puntoIds.size() + " puntos de venta asignados exitosamente"));
    }

    @DeleteMapping("/{id}/puntos")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<UserResponse>> removePuntos(
            @PathVariable Long id,
            @RequestBody List<Long> puntoIds) {
        UserResponse user = userService.removePuntos(id, puntoIds);
        return ResponseEntity.ok(ApiResponse.success(user, "Puntos de venta removidos exitosamente"));
    }

    @GetMapping("/{id}/puntos")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<List<com.pe.articulos.modules.puntos.dto.PuntoResponseDTO>>> getPuntosAsignados(
            @PathVariable Long id) {
        List<com.pe.articulos.modules.puntos.dto.PuntoResponseDTO> puntos = userService.getPuntosAsignados(id);
        return ResponseEntity.ok(ApiResponse.success(puntos, "Puntos obtenidos exitosamente"));
    }
    
    @PostMapping("/{userId}/modulos")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<UserResponse>> assignModulos(
            @PathVariable Long userId,
            @RequestBody List<Long> moduloIds) {
        UserResponse response = userService.assignModulos(userId, moduloIds);
        return ResponseEntity.ok(ApiResponse.success(response, "Módulos asignados exitosamente"));
    }

    @PostMapping("/{userId}/modulos-by-name")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<UserResponse>> assignModulosByName(
            @PathVariable Long userId,
            @RequestBody List<String> moduloNames) {
        UserResponse response = userService.assignModulosByName(userId, moduloNames);
        return ResponseEntity.ok(ApiResponse.success(response, "Módulos asignados exitosamente por nombre"));
    }

    @DeleteMapping("/{userId}/modulos")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<UserResponse>> removeModulos(
            @PathVariable Long userId,
            @RequestBody List<Long> moduloIds) {
        UserResponse response = userService.removeModulos(userId, moduloIds);
        return ResponseEntity.ok(ApiResponse.success(response, "Módulos removidos exitosamente"));
    }

    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Las contraseñas nuevas no coinciden");
        }
        
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("La nueva contraseña debe ser diferente a la contraseña actual");
        }
        userService.changePassword(id, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Contraseña cambiada exitosamente"));
    }
    
    @PatchMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request) {
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Las contraseñas nuevas no coinciden");
        }
        userService.resetPassword(id, request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Contraseña reseteada exitosamente"));
    }
    
    @PostMapping("/{id}/sucursales")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<UserResponse>> assignSucursales(
            @PathVariable Long id,
            @RequestBody List<Long> sucursalIds) {
        UserResponse user = userService.assignSucursales(id, sucursalIds);
        return ResponseEntity.ok(ApiResponse.success(user, sucursalIds.size() + " sucursales asignadas exitosamente"));
    }
    @DeleteMapping("/{id}/sucursales")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<UserResponse>> removeSucursales(
            @PathVariable Long id,
            @RequestBody List<Long> sucursalIds) {
        UserResponse user = userService.removeSucursales(id, sucursalIds);
        return ResponseEntity.ok(ApiResponse.success(user, "Sucursales removidas exitosamente"));
    }
    @GetMapping("/{id}/sucursales")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<List<com.pe.articulos.modules.sucursal.dto.SucursalDto>>> getSucursalesAsignadas(
            @PathVariable Long id) {
        List<com.pe.articulos.modules.sucursal.dto.SucursalDto> sucursales = userService.getSucursalesAsignadas(id);
        return ResponseEntity.ok(ApiResponse.success(sucursales, "Sucursales obtenidas exitosamente"));
    }
}

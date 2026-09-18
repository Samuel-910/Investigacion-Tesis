package com.pe.articulos.modules.permissions.controller;

import com.pe.articulos.core.exception.BadRequestException;
import com.pe.articulos.modules.permissions.dto.PermissionRequest;
import com.pe.articulos.modules.permissions.dto.PermissionResponse;
import com.pe.articulos.modules.permissions.service.PermissionService;
import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.core.shared.dto.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/permissions")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISO_CREAR')")
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody PermissionRequest request) {

        PermissionResponse permission = permissionService.createPermission(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<PermissionResponse>builder()
                        .success(true)
                        .message("Permiso creado exitosamente")
                        .data(permission)
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody PermissionRequest request) {

        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del permiso debe ser un número positivo");
        }

        PermissionResponse permission = permissionService.updatePermission(id, request);

        return ResponseEntity.ok(ApiResponse.<PermissionResponse>builder()
                .success(true)
                .message("Permiso actualizado exitosamente")
                .data(permission)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISO_ELIMINAR')")
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable Long id) {

        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del permiso debe ser un número positivo");
        }

        permissionService.deletePermission(id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Permiso eliminado exitosamente")
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionById(@PathVariable Long id) {

        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del permiso debe ser un número positivo");
        }

        PermissionResponse permission = permissionService.getPermissionById(id);

        return ResponseEntity.ok(ApiResponse.<PermissionResponse>builder()
                .success(true)
                .message("Permiso obtenido exitosamente")
                .data(permission)
                .build());
    }

    @GetMapping("/name/{name}")
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionByName(@PathVariable String name) {

        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("El nombre del permiso no puede estar vacío");
        }

        if (name.trim().length() < 3) {
            throw new BadRequestException("El nombre del permiso debe tener al menos 3 caracteres");
        }

        PermissionResponse permission = permissionService.getPermissionByName(name);

        return ResponseEntity.ok(ApiResponse.<PermissionResponse>builder()
                .success(true)
                .message("Permiso obtenido exitosamente")
                .data(permission)
                .build());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponse>>> getAllPermissions(
            @PageableDefault(page = 0, size = 10, sort = "name") Pageable pageable) {

        PageResponse<PermissionResponse> response = permissionService.getAllPermissions(pageable);

        return ResponseEntity.ok(ApiResponse.<PageResponse<PermissionResponse>>builder()
                .success(true)
                .message("Permisos obtenidos exitosamente")
                .data(response)
                .build());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponse>>> getActivePermissions(
            @PageableDefault(page = 0, size = 20, sort = "name") Pageable pageable) {

        PageResponse<PermissionResponse> response = permissionService.getActivePermissions(pageable);

        return ResponseEntity.ok(ApiResponse.<PageResponse<PermissionResponse>>builder()
                .success(true)
                .message("Permisos activos obtenidos exitosamente")
                .data(response)
                .build());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponse>>> searchPermissions(
            @RequestParam String q,
            @RequestParam(required = false, defaultValue = "all") String by,
            @PageableDefault(page = 0, size = 10, sort = "name") Pageable pageable) {

        PageResponse<PermissionResponse> response = permissionService.searchPermissions(q, by, pageable);

        return ResponseEntity.ok(ApiResponse.<PageResponse<PermissionResponse>>builder()
                .success(true)
                .message("Búsqueda realizada exitosamente")
                .data(response)
                .build());
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponse>>> searchByFilter(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false, defaultValue = "ALL") String type,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        PageResponse<PermissionResponse> response = permissionService.searchByFilter(query, type, pageable);

        return ResponseEntity.ok(ApiResponse.<PageResponse<PermissionResponse>>builder()
                .success(true)
                .message("Búsqueda realizada exitosamente")
                .data(response)
                .build());
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissionsList() {

        List<PermissionResponse> permissions = permissionService.getAllPermissionsAsList();

        return ResponseEntity.ok(ApiResponse.<List<PermissionResponse>>builder()
                .success(true)
                .message("Permisos obtenidos exitosamente")
                .data(permissions)
                .build());
    }

    @GetMapping("/active/list")
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getActivePermissionsList() {

        List<PermissionResponse> permissions = permissionService.getActivePermissionsAsList();

        return ResponseEntity.ok(ApiResponse.<List<PermissionResponse>>builder()
                .success(true)
                .message("Permisos activos obtenidos exitosamente")
                .data(permissions)
                .build());
    }

    @GetMapping("/search/list")
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> searchPermissionsList(
            @RequestParam String q) {

        if (q == null || q.trim().isEmpty()) {
            throw new BadRequestException("El término de búsqueda no puede estar vacío");
        }

        if (q.trim().length() < 2) {
            throw new BadRequestException("El término de búsqueda debe tener al menos 2 caracteres");
        }

        List<PermissionResponse> permissions = permissionService.searchPermissionsAsList(q);

        return ResponseEntity.ok(ApiResponse.<List<PermissionResponse>>builder()
                .success(true)
                .message("Búsqueda realizada exitosamente")
                .data(permissions)
                .build());
    }

    @GetMapping("/module/{module}")
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponse>>> getPermissionsByModule(
            @PathVariable String module,
            @PageableDefault(page = 0, size = 10, sort = "name") Pageable pageable) {

        if (module == null || module.trim().isEmpty()) {
            throw new BadRequestException("El nombre del módulo no puede estar vacío");
        }

        if (module.trim().length() < 2) {
            throw new BadRequestException("El nombre del módulo debe tener al menos 2 caracteres");
        }

        PageResponse<PermissionResponse> response = permissionService.getPermissionsByModule(module, pageable);

        return ResponseEntity.ok(ApiResponse.<PageResponse<PermissionResponse>>builder()
                .success(true)
                .message("Permisos del módulo obtenidos exitosamente")
                .data(response)
                .build());
    }

    @GetMapping("/module/{module}/active")
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponse>>> getActivePermissionsByModule(
            @PathVariable String module,
            @PageableDefault(page = 0, size = 10, sort = "name") Pageable pageable) {

        if (module == null || module.trim().isEmpty()) {
            throw new BadRequestException("El nombre del módulo no puede estar vacío");
        }

        if (module.trim().length() < 2) {
            throw new BadRequestException("El nombre del módulo debe tener al menos 2 caracteres");
        }

        PageResponse<PermissionResponse> response = permissionService.getActivePermissionsByModule(module,
                pageable);

        return ResponseEntity.ok(ApiResponse.<PageResponse<PermissionResponse>>builder()
                .success(true)
                .message("Permisos activos del módulo obtenidos exitosamente")
                .data(response)
                .build());
    }

    @GetMapping("/module/{module}/list")
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getPermissionsByModuleList(
            @PathVariable String module) {

        if (module == null || module.trim().isEmpty()) {
            throw new BadRequestException("El nombre del módulo no puede estar vacío");
        }

        List<PermissionResponse> permissions = permissionService.getPermissionsByModuleAsList(module);

        return ResponseEntity.ok(ApiResponse.<List<PermissionResponse>>builder()
                .success(true)
                .message("Permisos del módulo obtenidos exitosamente")
                .data(permissions)
                .build());
    }

    @GetMapping("/module/{module}/active/list")
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getActivePermissionsByModuleList(
            @PathVariable String module) {

        if (module == null || module.trim().isEmpty()) {
            throw new BadRequestException("El nombre del módulo no puede estar vacío");
        }

        List<PermissionResponse> permissions = permissionService.getActivePermissionsByModuleAsList(module);

        return ResponseEntity.ok(ApiResponse.<List<PermissionResponse>>builder()
                .success(true)
                .message("Permisos activos del módulo obtenidos exitosamente")
                .data(permissions)
                .build());
    }

    @GetMapping("/modules")
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<List<String>>> getAllModules() {

        List<String> modules = permissionService.getAllModules();

        return ResponseEntity.ok(ApiResponse.<List<String>>builder()
                .success(true)
                .message("Módulos obtenidos exitosamente")
                .data(modules)
                .build());
    }

    @GetMapping("/exists/{name}")
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<Boolean>> checkPermissionExists(@PathVariable String name) {

        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("El nombre del permiso no puede estar vacío");
        }

        boolean exists = permissionService.existsByName(name);

        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .success(true)
                .message(exists ? "El permiso existe" : "El permiso no existe")
                .data(exists)
                .build());
    }
}
package com.pe.articulos.modules.roles.controller;

import com.pe.articulos.core.exception.BadRequestException;
import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.core.shared.dto.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.pe.articulos.modules.roles.dto.RoleRequest;
import com.pe.articulos.modules.roles.dto.RoleResponse;
import com.pe.articulos.modules.roles.service.RoleService;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RoleController {

        private final RoleService roleService;

        /**
         * Crear un nuevo rol
         * POST /api/roles
         */
        @PostMapping
        @PreAuthorize("hasAuthority('ROL_CREAR')")
        public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {

                RoleResponse role = roleService.createRole(request);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(role));
        }

        /**
         * Actualizar un rol existente
         * PUT /api/roles/{id}
         */
        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('ROL_ACTUALIZAR')")
        public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
                        @PathVariable Long id,
                        @Valid @RequestBody RoleRequest request) {

                // Validación
                if (id == null || id <= 0) {
                        throw new BadRequestException("El ID del rol debe ser un número positivo");
                }

                RoleResponse role = roleService.updateRole(id, request);

                return ResponseEntity.ok(ApiResponse.success(role));
        }

        /**
         * Obtener un rol por ID
         * GET /api/roles/{id}
         */
        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('ROL_LEER')")
        public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {

                // Validación
                if (id == null || id <= 0) {
                        throw new BadRequestException("El ID del rol debe ser un número positivo");
                }

                RoleResponse role = roleService.getRoleById(id);

                return ResponseEntity.ok(ApiResponse.success(role));
        }

        @GetMapping
        @PreAuthorize("hasAuthority('ROL_LEER')")
        public ResponseEntity<ApiResponse<PageResponse<RoleResponse>>> getAllRoles(
                        @PageableDefault(page = 0, size = 10, sort = "name") Pageable pageable) {

                PageResponse<RoleResponse> response = roleService.getAllRoles(pageable);

                return ResponseEntity.ok(ApiResponse.success(response));
        }

        /**
         * Listar roles activos con paginación
         * GET /api/roles/active?page=0&size=20&sort=name
         */
        @GetMapping("/active")
        @PreAuthorize("hasAuthority('ROL_LEER')")
        public ResponseEntity<ApiResponse<PageResponse<RoleResponse>>> getActiveRoles(
                        @PageableDefault(page = 0, size = 20, sort = "name") Pageable pageable) {

                PageResponse<RoleResponse> response = roleService.getActiveRoles(pageable);

                return ResponseEntity.ok(ApiResponse.success(response));
        }

        /**
         * Filtro avanzado de roles por tipo
         * GET /api/roles/filter?query=admin&type=NOMBRE
         */
        @GetMapping("/filter")
        @PreAuthorize("hasAuthority('ROL_LEER')")
        public ResponseEntity<ApiResponse<PageResponse<RoleResponse>>> searchByFilter(
                        @RequestParam(required = false, defaultValue = "") String query,
                        @RequestParam(required = false, defaultValue = "ALL") String type,
                        @PageableDefault(page = 0, size = 10) Pageable pageable) {

                PageResponse<RoleResponse> response = roleService.searchByFilter(query, type, pageable);

                return ResponseEntity.ok(ApiResponse.success(response));
        }

        /**
         * Eliminar un rol (soft delete)
         * DELETE /api/roles/{id}
         */
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('ROL_ELIMINAR')")
        public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {

                // Validación
                if (id == null || id <= 0) {
                        throw new BadRequestException("El ID del rol debe ser un número positivo");
                }

                roleService.deleteRole(id);

                return ResponseEntity.ok(ApiResponse.success(null));
        }

        /**
         * Asignar permisos a un rol
         * POST /api/roles/{roleId}/permissions
         */
        @PostMapping("/{roleId}/permissions")
        @PreAuthorize("hasAuthority('ROL_ACTUALIZAR')")
        public ResponseEntity<ApiResponse<RoleResponse>> assignPermissions(
                        @PathVariable Long roleId,
                        @RequestBody List<Long> permissionIds) {

                // Validaciones
                if (roleId == null || roleId <= 0) {
                        throw new BadRequestException("El ID del rol debe ser un número positivo");
                }

                if (permissionIds == null || permissionIds.isEmpty()) {
                        throw new BadRequestException("Debe proporcionar al menos un ID de permiso");
                }

                // Validar que la lista no sea demasiado grande
                if (permissionIds.size() > 100) {
                        throw new BadRequestException("No se pueden asignar más de 100 permisos a la vez");
                }

                RoleResponse role = roleService.assignPermissions(roleId, permissionIds);

                return ResponseEntity.ok(ApiResponse.success(role));
        }

        /**
         * Remover permisos de un rol
         * DELETE /api/roles/{roleId}/permissions
         */
        @DeleteMapping("/{roleId}/permissions")
        @PreAuthorize("hasAuthority('ROL_ACTUALIZAR')")
        public ResponseEntity<ApiResponse<RoleResponse>> removePermissions(
                        @PathVariable Long roleId,
                        @RequestBody List<Long> permissionIds) {

                // Validaciones
                if (roleId == null || roleId <= 0) {
                        throw new BadRequestException("El ID del rol debe ser un número positivo");
                }

                if (permissionIds == null || permissionIds.isEmpty()) {
                        throw new BadRequestException("Debe proporcionar al menos un ID de permiso");
                }

                // Validar que la lista no sea demasiado grande
                if (permissionIds.size() > 100) {
                        throw new BadRequestException("No se pueden remover más de 100 permisos a la vez");
                }

                RoleResponse role = roleService.removePermissions(roleId, permissionIds);

                return ResponseEntity.ok(ApiResponse.success(role));
        }
}
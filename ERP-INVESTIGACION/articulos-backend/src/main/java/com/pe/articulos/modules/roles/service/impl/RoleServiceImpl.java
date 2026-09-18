package com.pe.articulos.modules.roles.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pe.articulos.core.exception.BadRequestException;
import com.pe.articulos.core.exception.ResourceNotFoundException;
import com.pe.articulos.core.exception.ValidationException;
import com.pe.articulos.modules.permissions.entity.Permission;
import com.pe.articulos.modules.permissions.repository.PermissionRepository;
import com.pe.articulos.modules.roles.dto.RoleRequest;
import com.pe.articulos.modules.roles.dto.RoleResponse;
import com.pe.articulos.modules.roles.entity.Role;
import com.pe.articulos.modules.roles.repository.RoleRepository;
import com.pe.articulos.modules.roles.service.RoleService;
import com.pe.articulos.modules.roles.mapper.RoleMapper;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.core.reports.audit.service.AuditService;
import com.pe.articulos.core.security.util.HttpUtils;

import com.pe.articulos.modules.accesos.entity.AccesoMain;
import com.pe.articulos.modules.accesos.repository.AccesoMainRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@SuppressWarnings("null")
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AccesoMainRepository accesoMainRepository;
    private final AuditService auditService;
    private final jakarta.servlet.http.HttpServletRequest httpRequest;
    private final RoleMapper roleMapper;

    // ========================================
    // CONSTANTES
    // ========================================
    private static final int MIN_NAME_LENGTH = 3;
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final int MAX_PERMISSIONS_PER_ROLE = 100;

    // ========================================
    // CREAR ROL
    // ========================================

    @Override
    public RoleResponse createRole(RoleRequest request) {
        log.info("Creating role: {}", request.getName());

        validateRoleName(request.getName());
        String normalizedName = request.getName().trim().toUpperCase();

        if (roleRepository.existsByName(normalizedName)) {
            throw new ValidationException("Ya existe un rol con el nombre '" + request.getName() + "'");
        }

        // --- LÓGICA PARA EL ACCESO AUTOMÁTICO ---
        // Buscamos el acceso llamado "ARTICULOS" (o el nombre exacto que tengas en BD)
        String accesoTarget = "Articulos";
        AccesoMain accesoMain = accesoMainRepository.findByNombre(accesoTarget)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Error de configuración: El acceso '" + accesoTarget + "' no existe en la base de datos"));

        if (request.getDescription() != null) {
            String trimmedDescription = request.getDescription().trim();
            if (trimmedDescription.length() > MAX_DESCRIPTION_LENGTH) {
                throw new BadRequestException(
                        "La descripción no puede superar los " + MAX_DESCRIPTION_LENGTH + " caracteres");
            }
            if (trimmedDescription.isEmpty()) {
                request.setDescription(null);
            }
        }

        if (request.getActive() == null) {
            request.setActive(true);
        }

        Role role = Role.builder()
                .name(normalizedName)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .active(request.getActive())
                .acceso(accesoMain) // Asignamos el encontrado automáticamente
                .permissions(new HashSet<>())
                .build();

        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            validateAndAssignPermissions(role, request.getPermissionIds());
        }

        Role savedRole = roleRepository.save(role);

        // Auditoría y Log...
        logAuditAction("ROLE_CREATE", "Rol creado: " + savedRole.getName(), savedRole.getId(), "/api/roles", "POST");

        return roleMapper.toResponse(savedRole);
    }

    @Override
    public RoleResponse updateRole(Long id, RoleRequest request) {
        log.info("Updating role with id: {}", id);

        // Validar ID
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del rol debe ser un número positivo");
        }

        // Validar que existe
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        // Validar nombre
        validateRoleName(request.getName());

        // Validar nombre único (si cambió)
        String normalizedName = request.getName().trim().toUpperCase();
        if (!role.getName().equals(normalizedName) && roleRepository.existsByName(normalizedName)) {
            throw new ValidationException("Ya existe un rol con el nombre '" + request.getName() + "'");
        }

        // Validar descripción
        if (request.getDescription() != null) {
            String trimmedDescription = request.getDescription().trim();
            if (trimmedDescription.length() > MAX_DESCRIPTION_LENGTH) {
                throw new BadRequestException(
                        "La descripción no puede superar los " + MAX_DESCRIPTION_LENGTH + " caracteres");
            }
        }

        // No permitir desactivar roles con usuarios asignados
        if (request.getActive() != null && !request.getActive() && role.getActive()) {
            if (role.getUsers() != null && !role.getUsers().isEmpty()) {
                throw new ValidationException("No se puede desactivar un rol que tiene " +
                        role.getUsers().size() + " usuario(s) asignado(s)");
            }
        }

        // ========================================
        // ACTUALIZACIÓN
        // ========================================

        role.setName(normalizedName);
        role.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        if (request.getActive() != null) {
            role.setActive(request.getActive());
        }

        // Actualizar permisos si vienen en el request
        if (request.getPermissionIds() != null) {
            role.getPermissions().clear();
            if (!request.getPermissionIds().isEmpty()) {
                validateAndAssignPermissions(role, request.getPermissionIds());
            }
        }

        if (request.getIdAcceso() != null && request.getIdAcceso() > 0) {
            AccesoMain nuevoAcceso = accesoMainRepository.findById(request.getIdAcceso())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Acceso no encontrado con ID: " + request.getIdAcceso()));
            role.setAcceso(nuevoAcceso);
        }

        Role updatedRole = roleRepository.save(role);
        log.info("Role updated successfully: {} with {} permissions",
                updatedRole.getName(), updatedRole.getPermissions().size());

        // Auditoría
        logAuditAction(
                "ROLE_UPDATE",
                "Rol actualizado: " + updatedRole.getName() + " (Permisos: " + updatedRole.getPermissions().size()
                        + ")",
                updatedRole.getId(),
                "/api/roles/" + id,
                "PUT");

        return roleMapper.toResponse(updatedRole);
    }

    // ========================================
    // CONSULTAS
    // ========================================

    private final String MODULO_DESTINO = "Articulos";

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        log.info("Getting role by id: {} for module: {}", id, MODULO_DESTINO);

        // Buscamos por ID pero validamos que pertenezca al módulo
        Role role = roleRepository.findByIdAndAccesoNombre(id, MODULO_DESTINO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rol no encontrado o no pertenece al módulo " + MODULO_DESTINO));

        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoleResponse> getAllRoles(Pageable pageable) {
        log.info("Getting all roles for module: {}", MODULO_DESTINO);
        validatePageable(pageable);

        // Usamos un nuevo método en el repositorio
        Page<Role> rolePage = roleRepository.findByAccesoNombre(MODULO_DESTINO, pageable);
        return mapToPageResponse(rolePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoleResponse> getActiveRoles(Pageable pageable) {
        log.info("Getting active roles for module: {}", MODULO_DESTINO);
        validatePageable(pageable);

        // Filtramos por activos Y por nombre de acceso
        Page<Role> rolePage = roleRepository.findByActiveTrueAndAccesoNombre(MODULO_DESTINO, pageable);
        return mapToPageResponse(rolePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoleResponse> searchByFilter(String query, String type, Pageable pageable) {
        log.info("Searching roles by {} with query {} in module {}", type, query, MODULO_DESTINO);
        Page<Role> page;

        // Ajustamos la búsqueda para que siempre respete el filtro del módulo
        page = roleRepository.searchByNameAndAcceso(query, MODULO_DESTINO, pageable);

        return mapToPageResponse(page);
    }
    // ========================================
    // ELIMINAR (SOFT DELETE)
    // ========================================

    @Override
    public void deleteRole(Long id) {
        log.info("Deleting role with id: {}", id);

        // Validar ID
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del rol debe ser un número positivo");
        }

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        // Validar que no esté ya inactivo
        if (!role.getActive()) {
            throw new ValidationException("El rol ya está desactivado");
        }

        // Validar que no tenga usuarios asignados
        if (role.getUsers() != null && !role.getUsers().isEmpty()) {
            throw new ValidationException("No se puede eliminar un rol que tiene " +
                    role.getUsers().size() + " usuario(s) asignado(s). " +
                    "Primero reasigne los usuarios a otro rol.");
        }

        // Soft delete (o dejamos que el repositorio lo maneje con delete() si está con @SQLDelete)
        roleRepository.delete(role);

        log.info("Role soft deleted successfully: {}", role.getName());

        // Auditoría
        logAuditAction(
                "ROLE_DELETE",
                "Rol eliminado (soft delete): " + role.getName() + " (ID: " + id + ")",
                id,
                "/api/roles/" + id,
                "DELETE");
    }

    // ========================================
    // GESTIÓN DE PERMISOS
    // ========================================

    @Override
    public RoleResponse assignPermissions(Long roleId, List<Long> permissionIds) {
        log.info("Assigning {} permissions to role: {}",
                permissionIds != null ? permissionIds.size() : 0, roleId);

        // Validaciones
        if (roleId == null || roleId <= 0) {
            throw new BadRequestException("El ID del rol debe ser un número positivo");
        }

        if (permissionIds == null || permissionIds.isEmpty()) {
            throw new BadRequestException("Debe proporcionar al menos un ID de permiso");
        }

        // Validar IDs de permisos
        for (Long permissionId : permissionIds) {
            if (permissionId == null || permissionId <= 0) {
                throw new BadRequestException("Todos los IDs de permisos deben ser números positivos");
            }
        }

        // Validar que no haya duplicados
        Set<Long> uniqueIds = new HashSet<>(permissionIds);
        if (uniqueIds.size() != permissionIds.size()) {
            throw new BadRequestException("La lista de permisos contiene IDs duplicados");
        }

        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        // Validar que el rol esté activo
        if (!role.getActive()) {
            throw new ValidationException("No se pueden asignar permisos a un rol inactivo");
        }

        Set<Permission> permissions = permissionRepository.findByIdIn(new HashSet<>(permissionIds));

        if (permissions.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron permisos con los IDs proporcionados");
        }

        if (permissions.size() != permissionIds.size()) {
            throw new BadRequestException("Algunos IDs de permisos no existen en el sistema. " +
                    "Se encontraron " + permissions.size() + " de " + permissionIds.size() + " permisos solicitados");
        }

        // Validar permisos inactivos
        long inactivePermissions = permissions.stream()
                .filter(p -> !p.getActive())
                .count();
        if (inactivePermissions > 0) {
            throw new ValidationException("No se pueden asignar " + inactivePermissions +
                    " permiso(s) inactivo(s)");
        }

        // Validar límite de permisos
        int totalPermissions = role.getPermissions().size() + permissions.size();
        if (totalPermissions > MAX_PERMISSIONS_PER_ROLE) {
            throw new ValidationException("El rol no puede tener más de " + MAX_PERMISSIONS_PER_ROLE +
                    " permisos. Actualmente tiene " + role.getPermissions().size() +
                    " y está intentando agregar " + permissions.size() + " más");
        }

        // Asignar permisos (addAll evita duplicados automáticamente por ser Set)
        int sizeBefore = role.getPermissions().size();
        role.getPermissions().addAll(permissions);
        int sizeAfter = role.getPermissions().size();
        int addedCount = sizeAfter - sizeBefore;

        Role updatedRole = roleRepository.save(role);
        log.info("Permissions assigned successfully to role: {} - Added: {}, Already had: {}",
                updatedRole.getName(), addedCount, sizeBefore);

        // Auditoría
        logAuditAction(
                "ROLE_PERMISSIONS_ASSIGN",
                "Permisos asignados a rol " + updatedRole.getName() + ": " + permissionIds,
                updatedRole.getId(),
                "/api/roles/" + roleId + "/permissions",
                "POST");

        return roleMapper.toResponse(updatedRole);
    }

    @Override
    public RoleResponse removePermissions(Long roleId, List<Long> permissionIds) {
        log.info("Removing {} permissions from role: {}",
                permissionIds != null ? permissionIds.size() : 0, roleId);

        // Validaciones
        if (roleId == null || roleId <= 0) {
            throw new BadRequestException("El ID del rol debe ser un número positivo");
        }

        if (permissionIds == null || permissionIds.isEmpty()) {
            throw new BadRequestException("Debe proporcionar al menos un ID de permiso");
        }

        // Validar IDs de permisos
        for (Long permissionId : permissionIds) {
            if (permissionId == null || permissionId <= 0) {
                throw new BadRequestException("Todos los IDs de permisos deben ser números positivos");
            }
        }

        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        // Validar que el rol tenga permisos
        if (role.getPermissions().isEmpty()) {
            throw new ValidationException("El rol no tiene permisos asignados");
        }

        Set<Permission> permissionsToRemove = permissionRepository.findByIdIn(new HashSet<>(permissionIds));

        if (permissionsToRemove.isEmpty()) {
            log.warn("No se encontraron permisos válidos para remover del rol: {}", roleId);
        }

        int sizeBefore = role.getPermissions().size();
        role.getPermissions().removeAll(permissionsToRemove);
        int sizeAfter = role.getPermissions().size();
        int removedCount = sizeBefore - sizeAfter;

        Role updatedRole = roleRepository.save(role);
        log.info("Permissions removed successfully from role: {} - Removed: {}, Remaining: {}",
                updatedRole.getName(), removedCount, sizeAfter);

        // Auditoría
        logAuditAction(
                "ROLE_PERMISSIONS_REMOVE",
                "Permisos removidos de rol " + updatedRole.getName() + ": " + permissionIds,
                updatedRole.getId(),
                "/api/roles/" + roleId + "/permissions",
                "DELETE");

        return roleMapper.toResponse(updatedRole);
    }

    // ========================================
    // MÉTODOS HELPER PRIVADOS
    // ========================================

    private void logAuditAction(String action, String description, Long resourceId,
            String endpoint, String method) {
        try {
            auditService.logAction(
                    "ROLES",
                    action,
                    description,
                    HttpUtils.getCurrentUsername(),
                    resourceId,
                    HttpUtils.getClientIpAddress(httpRequest),
                    "Éxito",
                    HttpUtils.getUserAgent(httpRequest),
                    endpoint,
                    method);
        } catch (Exception e) {
            log.error("Error logging audit for action: {}", action, e);
        }
    }

    private void validateRoleName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("El nombre del rol es obligatorio");
        }

        String trimmedName = name.trim();

        if (trimmedName.length() < MIN_NAME_LENGTH) {
            throw new BadRequestException("El nombre del rol debe tener al menos " +
                    MIN_NAME_LENGTH + " caracteres");
        }

        if (trimmedName.length() > MAX_NAME_LENGTH) {
            throw new BadRequestException("El nombre del rol no puede superar los " +
                    MAX_NAME_LENGTH + " caracteres");
        }

        // Validar que solo contenga letras, números, espacios y guiones bajos
        if (!trimmedName.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9\\s_-]+$")) {
            throw new BadRequestException(
                    "El nombre del rol solo puede contener letras, números, espacios, guiones y guiones bajos");
        }
    }

    private void validateAndAssignPermissions(Role role, Set<Long> permissionIds) {
        // Validar cantidad
        if (permissionIds.size() > MAX_PERMISSIONS_PER_ROLE) {
            throw new ValidationException("No se pueden asignar más de " + MAX_PERMISSIONS_PER_ROLE +
                    " permisos a un rol. Intentó asignar: " + permissionIds.size());
        }

        // Validar IDs
        for (Long permissionId : permissionIds) {
            if (permissionId == null || permissionId <= 0) {
                throw new BadRequestException("Todos los IDs de permisos deben ser números positivos");
            }
        }

        // Buscar permisos
        Set<Permission> permissions = permissionRepository.findByIdIn(permissionIds);

        if (permissions.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron permisos con los IDs proporcionados");
        }

        if (permissions.size() != permissionIds.size()) {
            throw new BadRequestException("Algunos IDs de permisos no existen. " +
                    "Se encontraron " + permissions.size() + " de " + permissionIds.size() + " permisos");
        }

        // Validar que estén activos
        long inactiveCount = permissions.stream()
                .filter(p -> !p.getActive())
                .count();
        if (inactiveCount > 0) {
            throw new ValidationException("No se pueden asignar " + inactiveCount + " permiso(s) inactivo(s)");
        }

        role.getPermissions().addAll(permissions);
    }

    private void validatePageable(Pageable pageable) {
        if (pageable == null) {
            throw new BadRequestException("Los parámetros de paginación son requeridos");
        }

        if (pageable.getPageSize() > 100) {
            throw new BadRequestException("El tamaño de página no puede ser mayor a 100");
        }

        if (pageable.getPageSize() <= 0) {
            throw new BadRequestException("El tamaño de página debe ser mayor a 0");
        }
    }

    // ========================================
    // MAPEO
    // ========================================

    private PageResponse<RoleResponse> mapToPageResponse(Page<Role> page) {
        List<RoleResponse> content = page.getContent().stream()
                .map(roleMapper::toResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber());
    }
}
package com.pe.articulos.modules.permissions.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pe.articulos.core.exception.BadRequestException;
import com.pe.articulos.core.exception.ResourceNotFoundException;
import com.pe.articulos.core.exception.ValidationException;
import com.pe.articulos.modules.accesos.entity.AccesoMain;
import com.pe.articulos.modules.accesos.repository.AccesoMainRepository;
import com.pe.articulos.modules.permissions.dto.PermissionMapper;
import com.pe.articulos.modules.permissions.dto.PermissionRequest;
import com.pe.articulos.modules.permissions.dto.PermissionResponse;
import com.pe.articulos.modules.permissions.entity.Permission;
import com.pe.articulos.modules.permissions.repository.PermissionRepository;
import com.pe.articulos.modules.permissions.service.PermissionService;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.core.reports.audit.service.AuditService;
import com.pe.articulos.core.security.util.HttpUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final AccesoMainRepository accesoMainRepository;
    private final AuditService auditService;
    private final HttpServletRequest httpRequest;
    private final PermissionMapper permissionMapper;

    private static final String TARGET_ACCESO = "Articulos";
    private static final int MIN_NAME_LENGTH = 3;

    @Override
    @Transactional
    public PermissionResponse createPermission(PermissionRequest request) {
        validatePermissionName(request.getName());
        String normalizedName = request.getName().trim().toUpperCase();

        if (permissionRepository.existsByNameAndAccesoNombre(normalizedName, TARGET_ACCESO)) {
            throw new ValidationException("Ya existe el permiso '" + normalizedName + "' en " + TARGET_ACCESO);
        }

        AccesoMain acceso = getArticulosAcceso();

        Permission permission = Permission.builder()
                .name(normalizedName)
                .description(request.getDescription().trim())
                .module(request.getModule().trim().toUpperCase())
                .active(request.getActive() == null || request.getActive())
                .acceso(acceso)
                .build();

        Permission saved = permissionRepository.save(permission);
        logAuditAction("PERMISSION_CREATE", "Creado: " + saved.getName(), saved.getId(), "/api/permissions", "POST");
        return permissionMapper.toDto(saved);
    }

    @Override
    @Transactional
    public PermissionResponse updatePermission(Long id, PermissionRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));

        validatePermissionName(request.getName());
        String normalizedName = request.getName().trim().toUpperCase();

        permission.setName(normalizedName);
        permission.setDescription(request.getDescription().trim());
        permission.setModule(request.getModule().trim().toUpperCase());
        permission.setActive(request.getActive());

        Permission updated = permissionRepository.save(permission);
        logAuditAction("PERMISSION_UPDATE", "Actualizado: " + updated.getName(), id, "/api/permissions/" + id, "PUT");
        return permissionMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));
        
        permissionRepository.delete(permission);
        logAuditAction("PERMISSION_DELETE", "Inactivado: " + permission.getName(), id, "/api/permissions/" + id,
                "DELETE");
    }

    @Override
    public PermissionResponse getPermissionById(Long id) {
        return permissionRepository.findById(id)
                .map(permissionMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));
    }

    @Override
    public PermissionResponse getPermissionByName(String name) {
        return permissionRepository.findByName(name.trim().toUpperCase())
                .map(permissionMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado: " + name));
    }

    @Override
    public boolean existsByName(String name) {
        return permissionRepository.existsByNameAndAccesoNombre(name.trim().toUpperCase(), TARGET_ACCESO);
    }

    @Override
    public PageResponse<PermissionResponse> getAllPermissions(Pageable pageable) {
        return mapToPageResponse(permissionRepository.findByAccesoNombre(TARGET_ACCESO, pageable));
    }

    @Override
    public PageResponse<PermissionResponse> getActivePermissions(Pageable pageable) {
        return mapToPageResponse(permissionRepository.findByActiveTrueAndAccesoNombre(TARGET_ACCESO, pageable));
    }

    @Override
    public PageResponse<PermissionResponse> getPermissionsByModule(String module, Pageable pageable) {
        return mapToPageResponse(
                permissionRepository.findByModuleAndAccesoNombre(module.toUpperCase(), TARGET_ACCESO, pageable));
    }

    @Override
    public PageResponse<PermissionResponse> getActivePermissionsByModule(String module, Pageable pageable) {
        return mapToPageResponse(permissionRepository.findByModuleAndActiveTrueAndAccesoNombre(module.toUpperCase(),
                TARGET_ACCESO, pageable));
    }

    @Override
    public List<PermissionResponse> getAllPermissionsAsList() {
        return permissionRepository.findByAccesoNombre(TARGET_ACCESO, Pageable.unpaged()).getContent()
                .stream().map(permissionMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<PermissionResponse> getActivePermissionsAsList() {
        return permissionRepository.findByActiveTrueAndAccesoNombre(TARGET_ACCESO)
                .stream().map(permissionMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<PermissionResponse> getPermissionsByModuleAsList(String module) {
        return permissionRepository.findByModuleAndAccesoNombre(module.toUpperCase(), TARGET_ACCESO)
                .stream().map(permissionMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<PermissionResponse> getActivePermissionsByModuleAsList(String module) {
        return permissionRepository.findByModuleAndActiveTrueAndAccesoNombre(module.toUpperCase(), TARGET_ACCESO)
                .stream().map(permissionMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public PageResponse<PermissionResponse> searchPermissions(String search, String target, Pageable pageable) {
        return mapToPageResponse(permissionRepository.searchPermissionsInAcceso(search, TARGET_ACCESO, pageable));
    }

    @Override
    public PageResponse<PermissionResponse> searchByFilter(String query, String type, Pageable pageable) {
        return mapToPageResponse(permissionRepository.searchPermissionsInAcceso(query, TARGET_ACCESO, pageable));
    }

    @Override
    public List<PermissionResponse> searchPermissionsAsList(String search) {
        return permissionRepository.searchPermissionsInAcceso(search, TARGET_ACCESO, Pageable.unpaged()).getContent()
                .stream().map(permissionMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<String> getAllModules() {
        return permissionRepository.findDistinctModulesInAcceso(TARGET_ACCESO);
    }

    private AccesoMain getArticulosAcceso() {
        return accesoMainRepository.findByNombre(TARGET_ACCESO)
                .orElseThrow(() -> new ResourceNotFoundException("Acceso " + TARGET_ACCESO + " no configurado"));
    }

    private void logAuditAction(String action, String desc, Long resId, String endp, String meth) {
        try {
            auditService.logAction("PERMISOS", action, desc, HttpUtils.getCurrentUsername(),
                    resId, HttpUtils.getClientIpAddress(httpRequest), "Éxito",
                    HttpUtils.getUserAgent(httpRequest), endp, meth);
        } catch (Exception e) {
            log.error("Audit Error: {}", e.getMessage());
        }
    }

    private void validatePermissionName(String name) {
        if (name == null || name.trim().length() < MIN_NAME_LENGTH)
            throw new BadRequestException("Nombre de permiso inválido");
    }

    private PageResponse<PermissionResponse> mapToPageResponse(Page<Permission> page) {
        List<PermissionResponse> content = page.getContent().stream().map(permissionMapper::toDto)
                .collect(Collectors.toList());
        return new PageResponse<>(content, page.getTotalElements(), page.getTotalPages(), page.getSize(),
                page.getNumber());
    }
}
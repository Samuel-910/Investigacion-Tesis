package com.pe.articulos.modules.roles.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import com.pe.articulos.modules.roles.dto.RoleRequest;
import com.pe.articulos.modules.roles.dto.RoleResponse;
import com.pe.articulos.core.shared.dto.PageResponse;

public interface RoleService {

    RoleResponse createRole(RoleRequest request);

    RoleResponse updateRole(Long id, RoleRequest request);

    RoleResponse getRoleById(Long id);

    // --- CAMBIOS PARA PAGINACIÓN ---

    PageResponse<RoleResponse> getAllRoles(Pageable pageable);

    PageResponse<RoleResponse> getActiveRoles(Pageable pageable);

    PageResponse<RoleResponse> searchByFilter(String query, String type, Pageable pageable);

    void deleteRole(Long id);

    RoleResponse assignPermissions(Long roleId, List<Long> permissionIds);

    RoleResponse removePermissions(Long roleId, List<Long> permissionIds);
}
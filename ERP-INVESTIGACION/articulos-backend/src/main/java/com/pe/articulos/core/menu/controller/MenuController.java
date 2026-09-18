package com.pe.articulos.core.menu.controller;

import com.pe.articulos.core.exception.BadRequestException;
import com.pe.articulos.core.menu.dto.MenuItemDTO;
import com.pe.articulos.core.menu.service.MenuService;
import com.pe.articulos.core.shared.dto.ApiResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.pe.articulos.modules.users.entity.DatosPersonales;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * Obtener el menú dinámico según el acceso seleccionado y permisos del usuario autenticado
     * GET /api/menu/{accesoId}
     */
    @GetMapping("/{accesoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MenuItemDTO>>> obtenerMenu(@PathVariable Long accesoId) {

        if (accesoId == null || accesoId <= 0) {
            throw new BadRequestException("El ID del acceso debe ser un número positivo");
        }

        // Obtener ID del usuario autenticado desde el contexto de seguridad
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof DatosPersonales)) {
             throw new BadRequestException("No se pudo identificar al usuario autenticado");
        }
        
        Long usuarioId = ((DatosPersonales) auth.getPrincipal()).getId();

        List<MenuItemDTO> menu = menuService.obtenerMenuParaUsuario(usuarioId, accesoId);

        return ResponseEntity.ok(ApiResponse.<List<MenuItemDTO>>builder()
                .success(true)
                .message("Menú de acceso ID " + accesoId + " obtenido exitosamente")
                .data(menu)
                .build());
    }
}
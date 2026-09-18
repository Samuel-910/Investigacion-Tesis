package com.pe.articulos.modules.notificaciones.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import com.pe.articulos.modules.notificaciones.service.NotificacionService;
import com.pe.articulos.modules.notificaciones.dto.NotificacionDTO;
import com.pe.articulos.modules.users.entity.DatosPersonales;
import com.pe.articulos.core.shared.dto.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController("alertaNotificacionController")
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@Slf4j
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping("/no-leidas")
    public ResponseEntity<ApiResponse<List<NotificacionDTO>>> obtenerNoLeidas(Authentication auth) {
        Long idSucursal = 1L; // Fallback por defecto (Sede Principal)
        if (auth != null && auth.getPrincipal() instanceof DatosPersonales) {
            DatosPersonales userDetails = (DatosPersonales) auth.getPrincipal();
            if (userDetails.getSucursalActual() != null) {
                idSucursal = userDetails.getSucursalActual().getIdSucursal();
            }
        }
        
        List<NotificacionDTO> noLeidas = notificacionService.obtenerNoLeidasPorSucursal(idSucursal);
        return ResponseEntity.ok(ApiResponse.success(noLeidas, "Notificaciones no leídas"));
    }

    @PutMapping("/{id}/marcar-leida")
    public ResponseEntity<ApiResponse<Void>> marcarComoLeida(@PathVariable Long id) {
        notificacionService.marcarComoLeida(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Notificación marcada como leída"));
    }
}

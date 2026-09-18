package com.pe.articulos.core.shared.notificaciones;

import com.pe.articulos.core.shared.notificaciones.NotificacionPendiente;
import com.pe.articulos.core.shared.notificaciones.NotificacionPendienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionPendienteRepository notificacionPendienteRepository;

    @GetMapping("/pendientes/{sucursalId}")
    public ResponseEntity<List<NotificacionPendiente>> getPendientesPorSucursal(@PathVariable Long sucursalId) {
        List<NotificacionPendiente> pendientes = notificacionPendienteRepository
                .findBySucursalIdAndEstadoOrderByFechaCreacionDesc(sucursalId, "PENDIENTE");
        return ResponseEntity.ok(pendientes);
    }
}

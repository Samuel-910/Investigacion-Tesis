package com.pe.articulos.modules.aprobaciones.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.modules.aprobaciones.dto.SolicitudAnulacionDTO;
import com.pe.articulos.modules.aprobaciones.service.SolicitudAnulacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aprobaciones/anulaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SolicitudAnulacionController {

    private final SolicitudAnulacionService service;

    @GetMapping("/pendientes")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('APROBAR_ANULACION')")
    public ResponseEntity<ApiResponse<List<SolicitudAnulacionDTO>>> listarPendientes(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long idSucursal,
            @RequestParam(required = false) Long idPuntoVenta) {
        return ResponseEntity.ok(service.listarPendientes(q, type, page, size, idSucursal, idPuntoVenta));
    }

    @PostMapping("/{id}/atender")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('APROBAR_ANULACION')")
    public ResponseEntity<ApiResponse<Void>> atender(
            @PathVariable Long id,
            @RequestParam boolean aprobada,
            @RequestParam(required = false) String observacion) {
        return ResponseEntity.ok(service.atender(id, aprobada, observacion));
    }

    @GetMapping("/historial")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('APROBAR_ANULACION')")
    public ResponseEntity<ApiResponse<List<SolicitudAnulacionDTO>>> listarHistorial(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long idSucursal,
            @RequestParam(required = false) Long idPuntoVenta) {
        return ResponseEntity.ok(service.listarHistorial(q, type, page, size, idSucursal, idPuntoVenta));
    }
}

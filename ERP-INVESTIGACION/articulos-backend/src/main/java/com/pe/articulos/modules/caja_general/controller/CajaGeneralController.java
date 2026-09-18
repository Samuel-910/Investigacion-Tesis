package com.pe.articulos.modules.caja_general.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.modules.caja_general.dto.CajaGeneralMovimientoRequest;
import com.pe.articulos.modules.caja_general.dto.CajaGeneralMovimientoResponse;
import com.pe.articulos.modules.caja_general.dto.CajaGeneralResponse;
import com.pe.articulos.modules.caja_general.service.CajaGeneralService;
import lombok.RequiredArgsConstructor;
import com.pe.articulos.core.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/caja-general")
@RequiredArgsConstructor
public class CajaGeneralController {

    private final CajaGeneralService cajaGeneralService;

    @GetMapping("/sucursal/{idSucursal}")
    public ResponseEntity<ApiResponse<CajaGeneralResponse>> obtenerPorSucursal(@PathVariable Long idSucursal) {
        return ResponseEntity.ok(ApiResponse.success(cajaGeneralService.obtenerPorSucursal(idSucursal)));
    }

    @GetMapping("/sucursal/{idSucursal}/movimientos")
    public ResponseEntity<ApiResponse<PageResponse<CajaGeneralMovimientoResponse>>> listarMovimientos(
            @PathVariable Long idSucursal,
            @RequestParam(required = false) String metodoPago,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String searchType,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(cajaGeneralService.listarMovimientos(idSucursal, metodoPago, query, searchType, pageable)));
    }

    @PostMapping("/movimiento")
    public ResponseEntity<ApiResponse<CajaGeneralMovimientoResponse>> registrarMovimiento(
            @RequestBody CajaGeneralMovimientoRequest request) {
        return ResponseEntity.ok(ApiResponse.success(cajaGeneralService.registrarMovimiento(request)));
    }

    @PutMapping("/movimiento/{id}")
    public ResponseEntity<ApiResponse<CajaGeneralMovimientoResponse>> actualizarMovimiento(
            @PathVariable Long id,
            @RequestBody CajaGeneralMovimientoRequest request) {
        return ResponseEntity.ok(ApiResponse.success(cajaGeneralService.actualizarMovimiento(id, request)));
    }

    @DeleteMapping("/movimiento/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarMovimiento(@PathVariable Long id) {
        cajaGeneralService.eliminarMovimiento(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

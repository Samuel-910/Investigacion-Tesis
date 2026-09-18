package com.pe.articulos.modules.caja_chica.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.modules.auth.service.JwtService;
import com.pe.articulos.modules.caja_chica.dto.*;
import com.pe.articulos.modules.caja_chica.services.CajaChicaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/caja-chica")
@RequiredArgsConstructor
public class CajaChicaController {

    private final CajaChicaService cajaChicaService;
    private final JwtService jwtService;

    @GetMapping("/cajas")
    public ApiResponse<List<CajaChicaResponse>> listarCajas() {
        return ApiResponse.<List<CajaChicaResponse>>builder()
                .success(true)
                .data(cajaChicaService.listarCajas())
                .message("Cajas listadas correctamente")
                .build();
    }

    @GetMapping("/abierta")
    public ApiResponse<CajaChicaResponse> obtenerCajaAbierta(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        Long puntoId = null;
        String userId = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            puntoId = jwtService.extractPuntoId(token);
            userId = String.valueOf(jwtService.extractUserId(token));
        }

        return ApiResponse.<CajaChicaResponse>builder()
                .success(true)
                .data(cajaChicaService.obtenerCajaAbierta(puntoId, userId).orElse(null))
                .message("Caja abierta obtenida")
                .build();
    }

    @PostMapping("/cajas")
    public ApiResponse<CajaChicaResponse> crearCaja(
            @RequestBody CajaChicaRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String userId = String.valueOf(jwtService.extractUserId(authHeader.substring(7)));
        return ApiResponse.<CajaChicaResponse>builder()
                .success(true)
                .data(cajaChicaService.crearCaja(request, userId))
                .message("Caja creada correctamente")
                .build();
    }

    @PostMapping("/movimientos")
    public ApiResponse<MovimientoResponse> registrarMovimiento(
            @RequestBody MovimientoRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String userId = String.valueOf(jwtService.extractUserId(authHeader.substring(7)));
        return ApiResponse.<MovimientoResponse>builder()
                .success(true)
                .data(cajaChicaService.registrarMovimiento(request, userId))
                .message("Movimiento registrado correctamente")
                .build();
    }

    @GetMapping("/movimientos/{cajaId}")
    public ApiResponse<Page<MovimientoResponse>> listarMovimientos(@PathVariable Long cajaId, Pageable pageable) {
        return ApiResponse.<Page<MovimientoResponse>>builder()
                .success(true)
                .data(cajaChicaService.listarMovimientos(cajaId, pageable))
                .message("Movimientos listados correctamente")
                .build();
    }

    @GetMapping("/resumen/{id}")
    public ApiResponse<CajaResumenDTO> obtenerResumen(@PathVariable Long id) {
        return ApiResponse.<CajaResumenDTO>builder()
                .success(true)
                .data(cajaChicaService.obtenerResumen(id))
                .message("Resumen de caja obtenido")
                .build();
    }

    @PutMapping("/cajas/{id}/cerrar")
    public ApiResponse<CajaChicaResponse> cerrarCaja(
            @PathVariable Long id,
            @RequestParam BigDecimal saldoCierreReal,
            @RequestParam(defaultValue = "false") boolean transferirACajaGeneral,
            @RequestHeader("Authorization") String authHeader) {
        String userId = String.valueOf(jwtService.extractUserId(authHeader.substring(7)));
        return ApiResponse.<CajaChicaResponse>builder()
                .success(true)
                .data(cajaChicaService.cerrarCaja(id, saldoCierreReal, transferirACajaGeneral, userId))
                .message("Caja cerrada correctamente")
                .build();
    }

    @PutMapping("/cajas/{id}/cerrar-detallado")
    public ApiResponse<CajaChicaResponse> cerrarCajaDetallado(
            @PathVariable Long id,
            @RequestBody CierreCajaRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String userId = String.valueOf(jwtService.extractUserId(authHeader.substring(7)));
        return ApiResponse.<CajaChicaResponse>builder()
                .success(true)
                .data(cajaChicaService.cerrarCajaDetallado(id, request, userId))
                .message("Caja cerrada detalladamente correctamente")
                .build();
    }

    @DeleteMapping("/movimientos/{id}")
    public ApiResponse<Void> eliminarMovimiento(@PathVariable Long id) {
        cajaChicaService.eliminarMovimiento(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Movimiento anulado correctamente")
                .build();
    }
}

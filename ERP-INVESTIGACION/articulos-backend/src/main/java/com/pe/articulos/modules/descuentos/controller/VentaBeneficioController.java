package com.pe.articulos.modules.descuentos.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.modules.descuentos.dto.VentaBeneficioDTO;
import com.pe.articulos.modules.descuentos.service.VentaBeneficioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import com.pe.articulos.core.shared.dto.PageResponse;

@Tag(name = "Venta Beneficios", description = "API para gestionar la trazabilidad de beneficios aplicados a ventas")
@RestController
@RequestMapping("/api/venta-beneficios")
@RequiredArgsConstructor
public class VentaBeneficioController {

    private final VentaBeneficioService service;

    @Operation(summary = "Registrar un beneficio aplicado a una venta")
    @PostMapping
    public ResponseEntity<ApiResponse<VentaBeneficioDTO>> guardar(@RequestBody VentaBeneficioDTO dto) {
        VentaBeneficioDTO response = service.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<VentaBeneficioDTO>builder()
                        .success(true)
                        .message("Beneficio de venta registrado")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Listar beneficios aplicados por ID de venta")
    @GetMapping("/venta/{idVenta}")
    public ResponseEntity<ApiResponse<PageResponse<VentaBeneficioDTO>>> listarPorVenta(
            @PathVariable Long idVenta,
            Pageable pageable) {
        PageResponse<VentaBeneficioDTO> response = service.listarPorVenta(idVenta, pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<VentaBeneficioDTO>>builder()
                .success(true)
                .message("Beneficios de la venta obtenidos")
                .data(response)
                .build());
    }

    @Operation(summary = "Obtener beneficio por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VentaBeneficioDTO>> obtenerPorId(@PathVariable Long id) {
        VentaBeneficioDTO response = service.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.<VentaBeneficioDTO>builder()
                .success(true)
                .message("Beneficio encontrado")
                .data(response)
                .build());
    }

    @Operation(summary = "Eliminar un registro de beneficio de venta")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Registro eliminado")
                .build());
    }
}

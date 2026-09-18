package com.pe.articulos.modules.descuentos.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.modules.descuentos.dto.DescuentoDetalleDTO;
import com.pe.articulos.modules.descuentos.service.DescuentoDetalleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import com.pe.articulos.core.shared.dto.PageResponse;

@Tag(name = "Descuento Detalles", description = "API para gestionar los detalles individuales de los descuentos")
@RestController
@RequestMapping("/api/descuento-detalles")
@RequiredArgsConstructor
public class DescuentoDetalleController {

    private final DescuentoDetalleService service;

    @Operation(summary = "Agregar detalle a un descuento")
    @PostMapping("/descuento/{idDescuento}")
    public ResponseEntity<ApiResponse<DescuentoDetalleDTO>> guardar(
            @PathVariable Long idDescuento,
            @RequestBody DescuentoDetalleDTO dto) {
        DescuentoDetalleDTO response = service.guardar(idDescuento, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<DescuentoDetalleDTO>builder()
                        .success(true)
                        .message("Detalle agregado exitosamente")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Listar detalles por ID de descuento")
    @GetMapping("/descuento/{idDescuento}")
    public ResponseEntity<ApiResponse<PageResponse<DescuentoDetalleDTO>>> listarPorDescuento(
            @PathVariable Long idDescuento,
            Pageable pageable) {
        PageResponse<DescuentoDetalleDTO> response = service.listarPorDescuento(idDescuento, pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<DescuentoDetalleDTO>>builder()
                .success(true)
                .message("Detalles obtenidos con éxito")
                .data(response)
                .build());
    }

    @Operation(summary = "Obtener detalle por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DescuentoDetalleDTO>> obtenerPorId(@PathVariable Long id) {
        DescuentoDetalleDTO response = service.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.<DescuentoDetalleDTO>builder()
                .success(true)
                .message("Detalle encontrado")
                .data(response)
                .build());
    }

    @Operation(summary = "Eliminar detalle")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Detalle eliminado correctamente")
                .build());
    }
}

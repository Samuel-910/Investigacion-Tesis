package com.pe.articulos.modules.compras.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.modules.compras.dto.IntercambioRequest;
import com.pe.articulos.modules.compras.service.IntercambioService;
import com.pe.articulos.modules.proveedores.dto.ProveedorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compras/intercambio")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IntercambioController {

    private final IntercambioService intercambioService;

    @GetMapping("/proveedor")
    @PreAuthorize("hasAuthority('COMPRA_LEER')")
    public ResponseEntity<ApiResponse<ProveedorResponse>> obtenerProveedor(
            @RequestParam Long idProducto,
            @RequestParam String lote) {
        ProveedorResponse proveedor = intercambioService.obtenerProveedorPorProductoYLote(idProducto, lote, org.springframework.data.domain.PageRequest.of(0, 1));
        if (proveedor == null) {
            return ResponseEntity.ok(ApiResponse.<ProveedorResponse>builder()
                    .success(false)
                    .message("No se encontró el proveedor original para este producto y lote")
                    .build());
        }
        return ResponseEntity.ok(ApiResponse.<ProveedorResponse>builder()
                .success(true)
                .data(proveedor)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('COMPRA_CREAR')")
    public ResponseEntity<ApiResponse<Void>> procesarIntercambio(@RequestBody IntercambioRequest request) {
        try {
            intercambioService.procesarIntercambio(request);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Intercambio procesado correctamente")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                    .success(false)
                    .message("Error al procesar el intercambio: " + e.getMessage())
                    .build());
        }
    }
}

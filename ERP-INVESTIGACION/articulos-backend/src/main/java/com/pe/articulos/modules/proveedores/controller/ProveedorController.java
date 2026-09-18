package com.pe.articulos.modules.proveedores.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.proveedores.dto.ProveedorRequest;
import com.pe.articulos.modules.proveedores.dto.ProveedorResponse;
import com.pe.articulos.modules.proveedores.service.ProveedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Proveedores", description = "API para gestionar proveedores")
@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

        private final ProveedorService service;

        @Operation(summary = "Crear proveedor")
        @PostMapping
        public ResponseEntity<ApiResponse<ProveedorResponse>> crear(@Valid @RequestBody ProveedorRequest request) {
                ProveedorResponse response = service.crear(request);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<ProveedorResponse>builder()
                                                .success(true)
                                                .message("Proveedor creado exitosamente")
                                                .data(response)
                                                .build());
        }

        @Operation(summary = "Obtener proveedor por ID")
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<ProveedorResponse>> obtenerPorId(@PathVariable Long id) {
                ProveedorResponse response = service.obtenerPorId(id);
                return ResponseEntity.ok(ApiResponse.<ProveedorResponse>builder()
                                .success(true)
                                .message("Proveedor encontrado")
                                .data(response)
                                .build());
        }

        @Operation(summary = "Listar todos los proveedores paginados")
        @GetMapping
        public ResponseEntity<ApiResponse<PageResponse<ProveedorResponse>>> listarTodos(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "razonSocial") String sortBy,
                        @RequestParam(defaultValue = "ASC") String direction) {

                Sort.Direction sortDirection = Sort.Direction.fromString(direction != null ? direction : "ASC");
                Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
                PageResponse<ProveedorResponse> response = service.listarTodos(pageable);

                return ResponseEntity.ok(ApiResponse.<PageResponse<ProveedorResponse>>builder()
                                .success(true)
                                .message("Proveedores obtenidos exitosamente")
                                .data(response)
                                .build());
        }

        @Operation(summary = "Buscar proveedores")
        @GetMapping("/buscar")
        public ResponseEntity<ApiResponse<PageResponse<ProveedorResponse>>> buscar(
                        @RequestParam String q,
                        @RequestParam(required = false) Integer estado,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                Pageable pageable = PageRequest.of(page, size);
                PageResponse<ProveedorResponse> response = service.buscar(q, estado, pageable);

                return ResponseEntity.ok(ApiResponse.<PageResponse<ProveedorResponse>>builder()
                                .success(true)
                                .message("Búsqueda completada")
                                .data(response)
                                .build());
        }

        @Operation(summary = "Actualizar proveedor")
        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<ProveedorResponse>> actualizar(
                        @PathVariable Long id,
                        @Valid @RequestBody ProveedorRequest request) {

                ProveedorResponse response = service.actualizar(id, request);
                return ResponseEntity.ok(ApiResponse.<ProveedorResponse>builder()
                                .success(true)
                                .message("Proveedor actualizado exitosamente")
                                .data(response)
                                .build());
        }

        @Operation(summary = "Eliminar proveedor")
        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
                service.eliminar(id);
                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Proveedor eliminado exitosamente")
                                .build());
        }
}

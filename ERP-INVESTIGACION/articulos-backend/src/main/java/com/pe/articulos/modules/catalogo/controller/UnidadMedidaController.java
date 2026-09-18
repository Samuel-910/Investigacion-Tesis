package com.pe.articulos.modules.catalogo.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.catalogo.dto.UnidadMedidaRequest;
import com.pe.articulos.modules.catalogo.dto.UnidadMedidaResponse;
import com.pe.articulos.modules.catalogo.service.UnidadMedidaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Unidades de Medida", description = "API para gestionar las unidades de medida")
@RestController
@RequestMapping("/api/unidades-medida")
@RequiredArgsConstructor
public class UnidadMedidaController {

        private final UnidadMedidaService service;

        @Operation(summary = "Listar todas las unidades de medida activas paginadas")
        @GetMapping("/activas")
        public ResponseEntity<ApiResponse<PageResponse<UnidadMedidaResponse>>> listarActivas(Pageable pageable) {

                PageResponse<UnidadMedidaResponse> response = service.listarActivas(pageable);
                return ResponseEntity.ok(ApiResponse.<PageResponse<UnidadMedidaResponse>>builder()
                                .success(true)
                                .message("Unidades de medida obtenidas exitosamente")
                                .data(response)
                                .build());
        }

        @Operation(summary = "Listar todas las unidades de medida paginadas")
        @GetMapping
        public ResponseEntity<ApiResponse<PageResponse<UnidadMedidaResponse>>> listarTodos(Pageable pageable) {

                PageResponse<UnidadMedidaResponse> response = service.listarTodos(pageable);
                return ResponseEntity.ok(ApiResponse.<PageResponse<UnidadMedidaResponse>>builder()
                                .success(true)
                                .message("Unidades de medida obtenidas exitosamente")
                                .data(response)
                                .build());
        }

        @Operation(summary = "Crear unidad de medida")
        @PostMapping
        public ResponseEntity<ApiResponse<UnidadMedidaResponse>> crear(
                        @Valid @RequestBody UnidadMedidaRequest request) {
                UnidadMedidaResponse response = service.crear(request);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<UnidadMedidaResponse>builder()
                                                .success(true)
                                                .message("Unidad de medida creada exitosamente")
                                                .data(response)
                                                .build());
        }

        @Operation(summary = "Actualizar unidad de medida")
        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<UnidadMedidaResponse>> actualizar(
                        @PathVariable Long id,
                        @Valid @RequestBody UnidadMedidaRequest request) {
                UnidadMedidaResponse response = service.actualizar(id, request);
                return ResponseEntity.ok(ApiResponse.<UnidadMedidaResponse>builder()
                                .success(true)
                                .message("Unidad de medida actualizada exitosamente")
                                .data(response)
                                .build());
        }

        @Operation(summary = "Eliminar unidad de medida")
        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
                service.eliminar(id);
                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Unidad de medida eliminada exitosamente")
                                .build());
        }
}

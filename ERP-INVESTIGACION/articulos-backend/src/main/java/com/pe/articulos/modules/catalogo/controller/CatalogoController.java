package com.pe.articulos.modules.catalogo.controller;

import com.pe.articulos.modules.catalogo.dto.CatalogoRequest;
import com.pe.articulos.modules.catalogo.dto.CatalogoResponse;
import com.pe.articulos.modules.catalogo.service.CatalogoService;
import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.core.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Productos y Servicios", description = "API para gestionar el catálogo de productos y servicios")
@RestController
@RequestMapping("/api/catalogo")
@RequiredArgsConstructor
public class CatalogoController {

        private final CatalogoService service;

        @Operation(summary = "Crear producto/servicio", description = "Crea un nuevo producto o servicio en el catálogo")
        @PostMapping
        public ResponseEntity<ApiResponse<CatalogoResponse>> crear(
                        @Valid @RequestBody CatalogoRequest request) {

                CatalogoResponse response = service.crear(request);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<CatalogoResponse>builder()
                                                .success(true)
                                                .message("Producto/servicio creado exitosamente")
                                                .data(response)
                                                .build());
        }

        @Operation(summary = "Obtener producto/servicio por ID")
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<CatalogoResponse>> obtenerPorId(@PathVariable Long id) {
                CatalogoResponse response = service.obtenerPorId(id);
                return ResponseEntity.ok(new ApiResponse<>("Producto/servicio encontrado", response, 200, true));
        }

        @Operation(summary = "Listar todos los productos/servicios paginados")
        @GetMapping
        public ResponseEntity<ApiResponse<PageResponse<CatalogoResponse>>> listarTodos(
                        @RequestParam(required = false) String tipo,
                        @RequestParam(required = false) Long idCategoria,
                        @RequestParam(required = false) Boolean esGenerico,
                        @RequestParam(required = false) Boolean manejaLotes,
                        @RequestParam(required = false) String estado,
                        @RequestParam(required = false) String tipoAfectacion,
                        Pageable pageable) {

                PageResponse<CatalogoResponse> response = service.listarTodos(pageable, tipo, idCategoria, esGenerico, manejaLotes, estado, tipoAfectacion);

                return ResponseEntity.ok(ApiResponse.<PageResponse<CatalogoResponse>>builder()
                                .success(true)
                                .message("Productos/servicios obtenidos exitosamente")
                                .data(response)
                                .build());
        }

        @Operation(summary = "Buscar productos/servicios")
        @GetMapping("/buscar")
        public ResponseEntity<ApiResponse<PageResponse<CatalogoResponse>>> buscar(
                        @RequestParam String q,
                        @RequestParam(required = false) String searchType,
                        @RequestParam(required = false) String tipo,
                        @RequestParam(required = false) Long idCategoria,
                        @RequestParam(required = false) Boolean esGenerico,
                        @RequestParam(required = false) Boolean manejaLotes,
                        @RequestParam(required = false) String estado,
                        @RequestParam(required = false) String tipoAfectacion,
                        Pageable pageable) {
                PageResponse<CatalogoResponse> response = service.buscar(q, searchType, tipo, idCategoria, esGenerico, manejaLotes, estado, tipoAfectacion, pageable);

                return ResponseEntity.ok(ApiResponse.<PageResponse<CatalogoResponse>>builder()
                                .success(true)
                                .message("Búsqueda completada")
                                .data(response)
                                .build());
        }

        @Operation(summary = "Actualizar producto/servicio")
        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<CatalogoResponse>> actualizar(
                        @PathVariable Long id,
                        @Valid @RequestBody CatalogoRequest request) {

                CatalogoResponse response = service.actualizar(id, request);

                return ResponseEntity.ok(ApiResponse.<CatalogoResponse>builder()
                                .success(true)
                                .message("Producto/servicio actualizado exitosamente")
                                .data(response)
                                .build());
        }

        @Operation(summary = "Eliminar producto/servicio")
        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {

                service.eliminar(id);

                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Producto/servicio eliminado exitosamente")
                                .data(null)
                                .build());
        }
}

package com.pe.articulos.modules.atributos.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.atributos.entity.BaseAtributo;
import com.pe.articulos.modules.atributos.service.BaseAtributoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public abstract class BaseAtributoController<T extends BaseAtributo> {

        protected final BaseAtributoService<T> service;
        protected final String nombreEntidad;

        protected BaseAtributoController(BaseAtributoService<T> service, String nombreEntidad) {
                this.service = service;
                this.nombreEntidad = nombreEntidad;
        }

        @GetMapping
        public ResponseEntity<ApiResponse<PageResponse<T>>> buscar(
                        @RequestParam(required = false, defaultValue = "") String q,
                        @PageableDefault(size = 10, sort = "descripcion") Pageable pageable) {

                Page<T> page = service.buscar(q, pageable);
                List<T> content = page.getContent();

                PageResponse<T> response = new PageResponse<>(
                                content,
                                page.getTotalElements(),
                                page.getTotalPages(),
                                page.getSize(),
                                page.getNumber());

                return ResponseEntity.ok(ApiResponse.<PageResponse<T>>builder()
                                .success(true)
                                .message("Listado de " + nombreEntidad + " obtenido exitosamente")
                                .data(response)
                                .build());
        }

        @GetMapping("/activos")
        public ResponseEntity<ApiResponse<PageResponse<T>>> buscarActivos(
                        @RequestParam(required = false, defaultValue = "") String q,
                        @PageableDefault(size = 10, sort = "descripcion") Pageable pageable) {

                Page<T> page = service.buscarActivos(q, pageable);
                List<T> content = page.getContent();

                PageResponse<T> response = new PageResponse<>(
                                content,
                                page.getTotalElements(),
                                page.getTotalPages(),
                                page.getSize(),
                                page.getNumber());

                return ResponseEntity.ok(ApiResponse.<PageResponse<T>>builder()
                                .success(true)
                                .message("Listado de " + nombreEntidad + " activos obtenido exitosamente")
                                .data(response)
                                .build());
        }

        @PostMapping
        public ResponseEntity<ApiResponse<T>> crear(@RequestBody T entidad) {
                T created = service.guardar(entidad);
                return new ResponseEntity<>(ApiResponse.<T>builder()
                                .success(true)
                                .message(nombreEntidad + " creado exitosamente")
                                .data(created)
                                .build(), HttpStatus.CREATED);
        }

        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<T>> actualizar(@PathVariable Long id, @RequestBody T entidad) {
                // Asegurar ID
                entidad.setId(id);
                T updated = service.guardar(entidad);
                return ResponseEntity.ok(ApiResponse.<T>builder()
                                .success(true)
                                .message(nombreEntidad + " actualizado exitosamente")
                                .data(updated)
                                .build());
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
                service.eliminar(id);
                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message(nombreEntidad + " eliminado exitosamente")
                                .build());
        }
}

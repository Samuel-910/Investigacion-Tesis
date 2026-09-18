package com.pe.articulos.modules.documentos.controllers;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.documentos.dto.PlantillaAsignacionDTO;
import com.pe.articulos.modules.documentos.entities.PlantillaAsignacion;
import com.pe.articulos.modules.documentos.services.PlantillaAsignacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Slf4j
@RestController
@RequestMapping("/api/documentos/plantilla-asignacion")
@RequiredArgsConstructor
public class PlantillaAsignacionController {

    private final PlantillaAsignacionService service;

    @GetMapping("/modulo/{modulo}")
    public ResponseEntity<ApiResponse<PageResponse<PlantillaAsignacionDTO>>> listarPorModulo(
            @PathVariable String modulo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/documentos/plantilla-asignacion/modulo/{} (paginated)", modulo);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(service.listarPorModulo(modulo, pageable)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PlantillaAsignacionDTO>> guardar(
            @Valid @RequestBody PlantillaAsignacion asignacion) {
        log.info("POST /api/documentos/plantilla-asignacion");
        return ResponseEntity.ok(ApiResponse.success(service.guardar(asignacion)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlantillaAsignacionDTO>> obtenerPorId(
            @PathVariable Long id) {
        log.info("GET /api/documentos/plantilla-asignacion/{}", id);
        return ResponseEntity.ok(ApiResponse.success(service.obtenerPorId(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/documentos/plantilla-asignacion/{}", id);
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

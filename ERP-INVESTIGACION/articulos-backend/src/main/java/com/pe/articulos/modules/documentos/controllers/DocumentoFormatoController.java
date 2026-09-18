package com.pe.articulos.modules.documentos.controllers;

import com.pe.articulos.modules.documentos.dto.DocumentoFormatoDTO;
import com.pe.articulos.modules.documentos.entities.DocumentoFormato;
import com.pe.articulos.modules.documentos.services.DocumentoFormatoService;
import com.pe.articulos.core.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.pe.articulos.core.shared.dto.PageResponse;

@Slf4j
@RestController
@RequestMapping("/api/documentos/formatos")
@RequiredArgsConstructor
public class DocumentoFormatoController {

    private final DocumentoFormatoService service;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DocumentoFormatoDTO>>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/documentos/formatos");
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(service.listarTodos(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentoFormatoDTO>> obtener(@PathVariable Long id) {
        log.info("GET /api/documentos/formatos/{}", id);
        return ResponseEntity.ok(ApiResponse.success(service.obtenerPorId(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DocumentoFormatoDTO>> guardar(@Valid @RequestBody DocumentoFormato formato) {
        log.info("POST /api/documentos/formatos");
        return ResponseEntity.ok(ApiResponse.success(service.guardar(formato)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/documentos/formatos/{}", id);
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

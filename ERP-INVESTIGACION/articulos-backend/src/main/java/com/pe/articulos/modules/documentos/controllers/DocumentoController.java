package com.pe.articulos.modules.documentos.controllers;

import com.pe.articulos.modules.documentos.dto.BloqueDTO;
import com.pe.articulos.modules.documentos.dto.BloqueRequest;
import com.pe.articulos.modules.documentos.dto.PlantillaDTO;
import com.pe.articulos.modules.documentos.dto.PlantillaRequest;
import com.pe.articulos.modules.documentos.entities.Modulo;
import com.pe.articulos.modules.documentos.services.DocumentoService;
import com.pe.articulos.core.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.pe.articulos.core.shared.dto.PageResponse;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService service;

    // --- BLOQUES ---

    @GetMapping("/bloques")
    public ResponseEntity<ApiResponse<PageResponse<BloqueDTO>>> listarBloques(
            @RequestParam(required = false) Modulo modulo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/documentos/bloques - modulo: {}", modulo);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(service.listarBloquesPorContexto(modulo, pageable)));
    }

    @GetMapping("/bloques/{id}")
    public ResponseEntity<ApiResponse<BloqueDTO>> obtenerBloque(@PathVariable Long id) {
        log.info("GET /api/documentos/bloques/{}", id);
        return ResponseEntity.ok(ApiResponse.success(service.obtenerBloque(id)));
    }

    @PostMapping("/bloques")
    public ResponseEntity<ApiResponse<BloqueDTO>> guardarBloque(@Valid @RequestBody BloqueRequest request) {
        log.info("POST /api/documentos/bloques");
        return ResponseEntity.ok(ApiResponse.success(service.guardarBloque(request)));
    }

    @PutMapping("/bloques/{id}")
    public ResponseEntity<ApiResponse<BloqueDTO>> actualizarBloque(@PathVariable Long id,
            @Valid @RequestBody BloqueRequest request) {
        log.info("PUT /api/documentos/bloques/{}", id);
        return ResponseEntity.ok(ApiResponse.success(service.actualizarBloque(id, request)));
    }

    @DeleteMapping("/bloques/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarBloque(@PathVariable Long id) {
        log.info("DELETE /api/documentos/bloques/{}", id);
        service.eliminarBloque(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // --- PLANTILLAS ---

    @GetMapping("/plantillas")
    public ResponseEntity<ApiResponse<PageResponse<PlantillaDTO>>> listarPlantillas(
            @RequestParam(required = false) Modulo modulo,
            @RequestParam(required = false) String tipo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/documentos/plantillas - modulo: {}, tipo: {}", modulo, tipo);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(service.listarPlantillasPorContexto(modulo, tipo, pageable)));
    }

    @GetMapping("/plantillas/{id}")
    public ResponseEntity<ApiResponse<PlantillaDTO>> obtenerPlantilla(@PathVariable Long id) {
        log.info("GET /api/documentos/plantillas/{}", id);
        return ResponseEntity.ok(ApiResponse.success(service.obtenerPlantilla(id)));
    }

    @PostMapping("/plantillas")
    public ResponseEntity<ApiResponse<PlantillaDTO>> guardarPlantilla(@Valid @RequestBody PlantillaRequest request) {
        log.info("POST /api/documentos/plantillas");
        return ResponseEntity.ok(ApiResponse.success(service.guardarPlantilla(request)));
    }

    @PutMapping("/plantillas/{id}")
    public ResponseEntity<ApiResponse<PlantillaDTO>> actualizarPlantilla(@PathVariable Long id,
            @Valid @RequestBody PlantillaRequest request) {
        log.info("PUT /api/documentos/plantillas/{}", id);
        return ResponseEntity.ok(ApiResponse.success(service.actualizarPlantilla(id, request)));
    }

    @DeleteMapping("/plantillas/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarPlantilla(@PathVariable Long id) {
        log.info("DELETE /api/documentos/plantillas/{}", id);
        service.eliminarPlantilla(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // --- GENERACIÓN ---

    @PostMapping("/plantillas/{id}/generar-pdf")
    public ResponseEntity<byte[]> generarPdf(@PathVariable Long id, @RequestBody Map<String, Object> datos) {
        byte[] pdf = service.generarPdf(id, datos);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "documento.pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}

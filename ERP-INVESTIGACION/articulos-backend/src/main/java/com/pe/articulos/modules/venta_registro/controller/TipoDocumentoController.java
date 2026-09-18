package com.pe.articulos.modules.venta_registro.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.modules.venta_registro.entity.TipoDocumento;
import com.pe.articulos.modules.venta_registro.service.TipoDocumentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-documento")
@RequiredArgsConstructor
public class TipoDocumentoController {

    private final TipoDocumentoService tipoDocumentoService;

    @GetMapping
    public ApiResponse<List<TipoDocumento>> listarTodos() {
        return ApiResponse.success(tipoDocumentoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoDocumento> obtenerPorId(@PathVariable String id) {
        return tipoDocumentoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ApiResponse<TipoDocumento> guardar(@RequestBody TipoDocumento tipoDocumento) {
        return ApiResponse.success(tipoDocumentoService.save(tipoDocumento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        if (tipoDocumentoService.findById(id).isPresent()) {
            tipoDocumentoService.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}


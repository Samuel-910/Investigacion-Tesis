package com.pe.articulos.modules.empresa.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.modules.empresa.dto.EmpresaDTO;
import com.pe.articulos.modules.empresa.dto.EmpresaPersonaVinculoDTO;
import com.pe.articulos.modules.empresa.dto.VinculoDTO;
import com.pe.articulos.modules.empresa.service.EmpresaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @PostMapping
    public ResponseEntity<ApiResponse<EmpresaDTO>> createEmpresa(@Valid @RequestBody EmpresaDTO empresaDTO) {
        log.info("REST request para crear una Empresa");
        EmpresaDTO saved = empresaService.create(empresaDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<EmpresaDTO>builder()
                        .success(true)
                        .message("Empresa creada exitosamente")
                        .data(saved)
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getAll(
            Pageable pageable,
            @RequestParam(required = false) boolean all) {
        log.info("REST request para obtener Empresas (all={})", all);
        if (all) {
            return ResponseEntity.ok(ApiResponse.<Object>builder()
                    .success(true)
                    .message("Empresas obtenidas exitosamente")
                    .data(empresaService.getAll())
                    .build());
        }
        return ResponseEntity.ok(ApiResponse.<Object>builder()
                .success(true)
                .message("Empresas obtenidas exitosamente")
                .data(empresaService.getAll(pageable))
                .build());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<EmpresaDTO>>> search(
            @RequestParam String q,
            Pageable pageable) {
        log.info("REST request para buscar Empresas con query: {}", q);
        return ResponseEntity.ok(ApiResponse.<Page<EmpresaDTO>>builder()
                .success(true)
                .message("Resultados de búsqueda")
                .data(empresaService.search(q, pageable))
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmpresaDTO>> getById(@PathVariable Long id) {
        log.info("REST request para obtener Empresa con ID: {}", id);
        return ResponseEntity.ok(ApiResponse.<EmpresaDTO>builder()
                .success(true)
                .data(empresaService.getById(id))
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmpresaDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody EmpresaDTO empresaDTO) {
        log.info("REST request para actualizar Empresa con ID: {}", id);
        return ResponseEntity.ok(ApiResponse.<EmpresaDTO>builder()
                .success(true)
                .message("Empresa actualizada exitosamente")
                .data(empresaService.update(id, empresaDTO))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        log.info("REST request para eliminar Empresa con ID: {}", id);
        empresaService.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Empresa eliminada exitosamente")
                .build());
    }

    @PostMapping("/vinculo")
    public ResponseEntity<ApiResponse<EmpresaPersonaVinculoDTO>> createVinculo(
            @Valid @RequestBody VinculoDTO vinculoDTO) {
        log.info("REST request para crear Vínculo Empresa-Personal");
        EmpresaPersonaVinculoDTO created = empresaService.crearVinculo(vinculoDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<EmpresaPersonaVinculoDTO>builder()
                        .success(true)
                        .message("Vínculo Empresa-Personal creado exitosamente")
                        .data(created)
                        .build());
    }

    @GetMapping("/{id}/vinculos")
    public ResponseEntity<ApiResponse<List<EmpresaPersonaVinculoDTO>>> getVinculosByEmpresa(
            @PathVariable Long id) {
        log.info("REST request para obtener Vínculos de Empresa con ID: {}", id);
        return ResponseEntity.ok(ApiResponse.<List<EmpresaPersonaVinculoDTO>>builder()
                .success(true)
                .message("Vínculos obtenidos exitosamente")
                .data(empresaService.getVinculosByEmpresa(id))
                .build());
    }
}

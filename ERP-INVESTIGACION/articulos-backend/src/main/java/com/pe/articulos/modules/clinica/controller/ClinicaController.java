package com.pe.articulos.modules.clinica.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.modules.clinica.entity.Clinica;
import com.pe.articulos.modules.clinica.entity.ClinicaAuditoria;
import com.pe.articulos.modules.clinica.service.ClinicaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.pe.articulos.core.shared.dto.PageResponse;

@RestController
@RequestMapping("/api/clinica")
@RequiredArgsConstructor
public class ClinicaController {

    private final ClinicaService clinicaService;

    @GetMapping
    public ResponseEntity<ApiResponse<Clinica>> getMain() {
        return ResponseEntity.ok(ApiResponse.success(clinicaService.obtenerPrincipal()));
    }

    @GetMapping("/historial/{id}")
    public ResponseEntity<ApiResponse<PageResponse<ClinicaAuditoria>>> getHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaCambio") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction != null ? direction : "DESC");
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        return ResponseEntity.ok(ApiResponse.success(clinicaService.listarHistorial(id, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Clinica>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(clinicaService.obtenerPorId(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Clinica>> create(@RequestBody Clinica clinica) {
        return ResponseEntity.ok(ApiResponse.<Clinica>builder()
                .success(true)
                .message("Datos de la clínica guardados")
                .data(clinicaService.crear(clinica))
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Clinica>> update(@PathVariable Long id, @RequestBody Clinica clinica) {
        return ResponseEntity.ok(ApiResponse.<Clinica>builder()
                .success(true)
                .message("Datos de la clínica actualizados")
                .data(clinicaService.actualizar(id, clinica))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        clinicaService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Datos eliminados")
                .build());
    }
}

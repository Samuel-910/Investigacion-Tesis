package com.pe.articulos.modules.almacen.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.modules.almacen.dto.AlmacenRequest;
import com.pe.articulos.modules.almacen.dto.AlmacenResponse;
import com.pe.articulos.modules.almacen.service.AlmacenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import com.pe.articulos.core.shared.dto.PageResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/almacenes")
@RequiredArgsConstructor
@Slf4j
public class AlmacenController {

    private final AlmacenService almacenService;

    @GetMapping("/sucursal/{idSucursal}")
    public ResponseEntity<ApiResponse<PageResponse<AlmacenResponse>>> getBySucursal(@PathVariable Long idSucursal,
            Pageable pageable) {
        log.info("REST request to get Almacenes by sucursal: {}", idSucursal);
        return ResponseEntity.ok(ApiResponse.success(almacenService.getAlmacenesBySucursal(idSucursal, pageable)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AlmacenResponse>> create(@Valid @RequestBody AlmacenRequest request) {
        log.info("REST request to create Almacen: {}", request.getNombre());
        return ResponseEntity.ok(ApiResponse.success(almacenService.createAlmacen(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AlmacenResponse>> update(@PathVariable Long id,
            @Valid @RequestBody AlmacenRequest request) {
        log.info("REST request to update Almacen: {}", id);
        return ResponseEntity.ok(ApiResponse.success(almacenService.updateAlmacen(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        log.info("REST request to delete Almacen: {}", id);
        almacenService.deleteAlmacen(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlmacenResponse>> getById(@PathVariable Long id) {
        log.info("REST request to get Almacen: {}", id);
        return ResponseEntity.ok(ApiResponse.success(almacenService.getAlmacenById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AlmacenResponse>>> getAll(Pageable pageable) {
        log.info("REST request to get all Almacenes");
        return ResponseEntity.ok(ApiResponse.success(almacenService.getAllAlmacenes(pageable)));
    }
}

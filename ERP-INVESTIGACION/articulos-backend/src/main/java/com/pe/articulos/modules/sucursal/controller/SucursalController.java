package com.pe.articulos.modules.sucursal.controller;

import com.pe.articulos.core.exception.BadRequestException;
import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.sucursal.dto.SucursalDto;
import com.pe.articulos.modules.sucursal.service.SucursalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sucursales")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SucursalController {

    private final SucursalService sucursalService;

    // ========================================
    // CRUD BÁSICO
    // ========================================

    @PostMapping
    @PreAuthorize("hasAuthority('SUCURSAL_CREAR')")
    public ResponseEntity<ApiResponse<SucursalDto>> crear(
            @Valid @RequestBody SucursalDto dto) {

        SucursalDto creada = sucursalService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(creada));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SUCURSAL_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<SucursalDto>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SucursalDto dto) {

        if (id == null || id <= 0) {
            throw new BadRequestException("El ID debe ser un número positivo");
        }

        SucursalDto actualizada = sucursalService.actualizar(id, dto);
        return ResponseEntity.ok(ApiResponse.success(actualizada));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUCURSAL_LEER')")
    public ResponseEntity<ApiResponse<SucursalDto>> obtenerPorId(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID debe ser un número positivo");
        }

        return sucursalService.obtenerPorId(id)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto)))
                .orElseThrow(() -> new com.pe.articulos.core.exception.ResourceNotFoundException(
                        "Sucursal", "id", id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUCURSAL_ELIMINAR')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID debe ser un número positivo");
        }

        sucursalService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ========================================
    // LISTADOS CON PAGINACIÓN
    // ========================================

    @GetMapping
    @PreAuthorize("hasAuthority('SUCURSAL_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<SucursalDto>>> obtenerTodas(
            @PageableDefault(size = 20, sort = "nombreSucursal") Pageable pageable) {

        PageResponse<SucursalDto> response = sucursalService.obtenerTodas(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAuthority('SUCURSAL_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<SucursalDto>>> obtenerPorEstado(
            @PathVariable String estado,
            @PageableDefault(size = 20) Pageable pageable) {

        if (estado == null || estado.trim().isEmpty()) {
            throw new BadRequestException("El estado no puede estar vacío");
        }

        PageResponse<SucursalDto> response = sucursalService.obtenerPorEstado(estado, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAuthority('SUCURSAL_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<SucursalDto>>> buscar(
            @RequestParam String q,
            @RequestParam(defaultValue = "ALL") String type,
            @PageableDefault(size = 20) Pageable pageable) {

        if (q == null || q.trim().isEmpty()) {
            throw new BadRequestException("El término de búsqueda no puede estar vacío");
        }

        PageResponse<SucursalDto> response = sucursalService.buscar(q, type, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/con-personal")
    @PreAuthorize("hasAuthority('SUCURSAL_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<SucursalDto>>> obtenerConPersonal(
            @PageableDefault(size = 20, sort = "cantidadPersonal") Pageable pageable) {

        PageResponse<SucursalDto> response = sucursalService.obtenerConPersonal(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========================================
    // LISTADOS SIN PAGINACIÓN
    // ========================================

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('SUCURSAL_LEER')")
    public ResponseEntity<ApiResponse<List<SucursalDto>>> obtenerTodasList() {
        List<SucursalDto> sucursales = sucursalService.obtenerTodasAsList();
        return ResponseEntity.ok(ApiResponse.success(sucursales));
    }

    @GetMapping("/activas/list")
    @PreAuthorize("hasAuthority('SUCURSAL_LEER')")
    public ResponseEntity<ApiResponse<List<SucursalDto>>> obtenerActivasList() {
        List<SucursalDto> sucursales = sucursalService.obtenerActivasAsList();
        return ResponseEntity.ok(ApiResponse.success(sucursales));
    }

    @GetMapping("/estado/{estado}/list")
    @PreAuthorize("hasAuthority('SUCURSAL_LEER')")
    public ResponseEntity<ApiResponse<List<SucursalDto>>> obtenerPorEstadoList(
            @PathVariable String estado) {

        if (estado == null || estado.trim().isEmpty()) {
            throw new BadRequestException("El estado no puede estar vacío");
        }

        List<SucursalDto> sucursales = sucursalService.obtenerPorEstadoAsList(estado);
        return ResponseEntity.ok(ApiResponse.success(sucursales));
    }

    @GetMapping("/sin-personal/list")
    @PreAuthorize("hasAuthority('SUCURSAL_LEER')")
    public ResponseEntity<ApiResponse<List<SucursalDto>>> obtenerSinPersonalList() {
        List<SucursalDto> sucursales = sucursalService.obtenerSinPersonalAsList();
        return ResponseEntity.ok(ApiResponse.success(sucursales));
    }

    // ========================================
    // BÚSQUEDAS ESPECIALIZADAS
    // ========================================

    @GetMapping("/nombre/{nombre}")
    @PreAuthorize("hasAuthority('SUCURSAL_LEER')")
    public ResponseEntity<ApiResponse<SucursalDto>> obtenerPorNombre(@PathVariable String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new BadRequestException("El nombre no puede estar vacío");
        }

        return sucursalService.obtenerPorNombre(nombre)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto)))
                .orElseThrow(() -> new com.pe.articulos.core.exception.ResourceNotFoundException(
                        "Sucursal", "nombre", nombre));
    }

    @GetMapping("/con-personal/detalle")
    @PreAuthorize("hasAuthority('SUCURSAL_LEER')")
    public ResponseEntity<ApiResponse<List<SucursalDto>>> obtenerConPersonalDetalle() {
        List<SucursalDto> sucursales = sucursalService.obtenerConCantidadPersonal();
        return ResponseEntity.ok(ApiResponse.success(sucursales));
    }

    @GetMapping("/sin-personal")
    @PreAuthorize("hasAuthority('SUCURSAL_LEER')")
    public ResponseEntity<ApiResponse<List<SucursalDto>>> obtenerSinPersonal() {
        List<SucursalDto> sucursales = sucursalService.obtenerSinPersonal();
        return ResponseEntity.ok(ApiResponse.success(sucursales));
    }

    // ========================================
    // UTILIDADES
    // ========================================

    @GetMapping("/contar/estado/{estado}")
    @PreAuthorize("hasAuthority('SUCURSAL_LEER')")
    public ResponseEntity<ApiResponse<Long>> contarPorEstado(@PathVariable String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            throw new BadRequestException("El estado no puede estar vacío");
        }

        Long count = sucursalService.contarPorEstado(estado);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/contar/activas")
    @PreAuthorize("hasAuthority('SUCURSAL_LEER')")
    public ResponseEntity<ApiResponse<Long>> contarActivas() {
        Long count = sucursalService.contarActivas();
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/contar/total")
    @PreAuthorize("hasAuthority('SUCURSAL_LEER')")
    public ResponseEntity<ApiResponse<Long>> contarTotal() {
        Long count = sucursalService.contarTotal();
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/existe/{nombre}")
    @PreAuthorize("hasAuthority('SUCURSAL_LEER')")
    public ResponseEntity<ApiResponse<Boolean>> existePorNombre(@PathVariable String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new BadRequestException("El nombre no puede estar vacío");
        }

        boolean existe = sucursalService.existePorNombre(nombre);
        return ResponseEntity.ok(ApiResponse.success(existe));
    }

    // ========================================
    // OPERACIONES ESPECIALES
    // ========================================

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('SUCURSAL_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<Void>> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado) {

        if (id == null || id <= 0) {
            throw new BadRequestException("El ID debe ser un número positivo");
        }

        if (estado == null || estado.trim().isEmpty()) {
            throw new BadRequestException("El estado no puede estar vacío");
        }

        sucursalService.cambiarEstado(id, estado);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasAuthority('SUCURSAL_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<Void>> activar(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID debe ser un número positivo");
        }

        sucursalService.activar(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/{id}/inactivar")
    @PreAuthorize("hasAuthority('SUCURSAL_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<Void>> inactivar(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID debe ser un número positivo");
        }

        sucursalService.inactivar(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
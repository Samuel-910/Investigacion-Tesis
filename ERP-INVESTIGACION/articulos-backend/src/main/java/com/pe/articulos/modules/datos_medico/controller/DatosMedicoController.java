package com.pe.articulos.modules.datos_medico.controller;

import com.pe.articulos.core.exception.BadRequestException;
import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.datos_medico.dto.*;
import com.pe.articulos.modules.datos_medico.service.DatosMedicoService;
import com.pe.articulos.core.enums.EstadoGeneral;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/personal-medico")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DatosMedicoController {

    private final DatosMedicoService medicoService;

    // ========================================
    // CRUD BÁSICO
    // ========================================

    @PostMapping
    @PreAuthorize("hasAuthority('PERSONAL_CREAR')")
    public ResponseEntity<ApiResponse<DatosMedicoDto>> crear(
            @Valid @RequestBody DatosMedicoCreateDto dto) {

        DatosMedicoDto creado = medicoService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<DatosMedicoDto>builder()
                        .success(true)
                        .message("Personal médico creado exitosamente")
                        .data(creado)
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERSONAL_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<DatosMedicoDto>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DatosMedicoUpdateDto dto) {

        if (id == null || id <= 0) {
            throw new BadRequestException("El ID debe ser un número positivo");
        }

        DatosMedicoDto actualizado = medicoService.actualizar(id, dto);
        return ResponseEntity.ok(ApiResponse.<DatosMedicoDto>builder()
                .success(true)
                .message("Personal actualizado exitosamente")
                .data(actualizado)
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERSONAL_LEER')")
    public ResponseEntity<ApiResponse<DatosMedicoDto>> obtenerPorId(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID debe ser un número positivo");
        }

        return medicoService.obtenerPorId(id)
                .map(dto -> ResponseEntity.ok(
                        ApiResponse.<DatosMedicoDto>builder()
                                .success(true)
                                .message("Personal encontrado")
                                .data(dto)
                                .build()))
                .orElseThrow(() -> new com.pe.articulos.core.exception.ResourceNotFoundException(
                        "DatosMedico", "id", id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERSONAL_ELIMINAR')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID debe ser un número positivo");
        }

        medicoService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Personal marcado como inactivo")
                .build());
    }

    // ========================================
    // LISTADOS CON PAGINACIÓN
    // ========================================

    @GetMapping
    @PreAuthorize("hasAuthority('PERSONAL_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<DatosMedicoDto>>> obtenerTodos(
            @PageableDefault(size = 20, sort = "nombreMed") Pageable pageable) {

        PageResponse<DatosMedicoDto> response = medicoService.obtenerTodos(pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<DatosMedicoDto>>builder()
                .success(true)
                .message("Personal obtenido exitosamente")
                .data(response)
                .build());
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAuthority('PERSONAL_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<DatosMedicoDto>>> obtenerPorEstado(
            @PathVariable String estado,
            @PageableDefault(size = 20) Pageable pageable) {

        if (estado == null || estado.trim().isEmpty()) {
            throw new BadRequestException("El estado no puede estar vacío");
        }

        PageResponse<DatosMedicoDto> response = medicoService.obtenerPorEstado(estado, pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<DatosMedicoDto>>builder()
                .success(true)
                .message("Personal por estado obtenido")
                .data(response)
                .build());
    }

    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasAuthority('PERSONAL_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<DatosMedicoDto>>> obtenerPorTipo(
            @PathVariable String tipo,
            @PageableDefault(size = 20) Pageable pageable) {

        if (tipo == null || tipo.trim().isEmpty()) {
            throw new BadRequestException("El tipo no puede estar vacío");
        }

        PageResponse<DatosMedicoDto> response = medicoService.obtenerPorTipo(tipo, pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<DatosMedicoDto>>builder()
                .success(true)
                .message("Personal por tipo obtenido")
                .data(response)
                .build());
    }

    @GetMapping("/area/{idArea}")
    @PreAuthorize("hasAuthority('PERSONAL_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<DatosMedicoDto>>> obtenerPorArea(
            @PathVariable Long idArea,
            @PageableDefault(size = 20) Pageable pageable) {

        if (idArea == null || idArea <= 0) {
            throw new BadRequestException("El ID del área debe ser positivo");
        }

        PageResponse<DatosMedicoDto> response = medicoService.obtenerPorArea(idArea, pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<DatosMedicoDto>>builder()
                .success(true)
                .message("Personal del área obtenido")
                .data(response)
                .build());
    }

    @GetMapping("/sucursal/{idSucursal}")
    @PreAuthorize("hasAuthority('PERSONAL_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<DatosMedicoDto>>> obtenerPorSucursal(
            @PathVariable Long idSucursal,
            @PageableDefault(size = 20) Pageable pageable) {

        if (idSucursal == null || idSucursal <= 0) {
            throw new BadRequestException("El ID de la sucursal debe ser positivo");
        }

        PageResponse<DatosMedicoDto> response = medicoService.obtenerPorSucursal(idSucursal, pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<DatosMedicoDto>>builder()
                .success(true)
                .message("Personal de la sucursal obtenido")
                .data(response)
                .build());
    }

    @GetMapping("/tipo-medico/{tipoMedico}")
    @PreAuthorize("hasAuthority('PERSONAL_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<DatosMedicoDto>>> obtenerPorTipoMedico(
            @PathVariable String tipoMedico,
            @PageableDefault(size = 20) Pageable pageable) {

        if (tipoMedico == null || tipoMedico.trim().isEmpty()) {
            throw new BadRequestException("El tipo de médico no puede estar vacío");
        }

        PageResponse<DatosMedicoDto> response = medicoService.obtenerPorTipoMedico(tipoMedico, pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<DatosMedicoDto>>builder()
                .success(true)
                .message("Médicos por tipo obtenidos")
                .data(response)
                .build());
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAuthority('PERSONAL_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<DatosMedicoDto>>> buscarConFiltros(
            @RequestParam(required = false) String nombreMed,
            @RequestParam(required = false) String nroCmp,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Long idArea,
            @RequestParam(required = false) Long idSucursal,
            @RequestParam(required = false) String vacaciones,
            @RequestParam(required = false) String tipoMedico,
            @PageableDefault(size = 20, sort = "nombreMed") Pageable pageable) {

        FiltroMedicoDto filtro = new FiltroMedicoDto();
        filtro.setNombreMed(nombreMed);
        filtro.setNroCmp(nroCmp);
        filtro.setEstado(EstadoGeneral.fromCodigo(estado));
        filtro.setTipo(tipo);
        filtro.setIdArea(idArea);
        filtro.setIdSucursal(idSucursal);
        filtro.setVacaciones(vacaciones);
        filtro.setTipoMedico(tipoMedico);

        PageResponse<DatosMedicoDto> response = medicoService.buscarConFiltros(filtro, pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<DatosMedicoDto>>builder()
                .success(true)
                .message("Búsqueda completada exitosamente")
                .data(response)
                .build());
    }

    @GetMapping("/activos")
    @PreAuthorize("hasAuthority('PERSONAL_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<DatosMedicoDto>>> obtenerActivos(
            @PageableDefault(size = 20) Pageable pageable) {

        PageResponse<DatosMedicoDto> response = medicoService.obtenerActivos(pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<DatosMedicoDto>>builder()
                .success(true)
                .message("Personal activo obtenido")
                .data(response)
                .build());
    }

    // ========================================
    // BÚSQUEDAS ESPECIALIZADAS
    // ========================================

    @GetMapping("/cmp/{nroCmp}")
    @PreAuthorize("hasAuthority('PERSONAL_LEER')")
    public ResponseEntity<ApiResponse<DatosMedicoDto>> obtenerPorCmp(@PathVariable String nroCmp) {
        if (nroCmp == null || nroCmp.trim().isEmpty()) {
            throw new BadRequestException("El CMP no puede estar vacío");
        }

        return medicoService.obtenerPorCmp(nroCmp)
                .map(dto -> ResponseEntity.ok(
                        ApiResponse.<DatosMedicoDto>builder()
                                .success(true)
                                .message("Médico encontrado")
                                .data(dto)
                                .build()))
                .orElseThrow(() -> new com.pe.articulos.core.exception.ResourceNotFoundException(
                        "DatosMedico", "nroCmp", nroCmp));
    }

    @GetMapping("/vacaciones")
    @PreAuthorize("hasAuthority('PERSONAL_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<DatosMedicoDto>>> obtenerEnVacaciones(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @PageableDefault(size = 20) Pageable pageable) {

        LocalDate fechaBusqueda = fecha != null ? fecha : LocalDate.now();
        PageResponse<DatosMedicoDto> enVacaciones = medicoService.obtenerEnVacaciones(fechaBusqueda, pageable);

        return ResponseEntity.ok(ApiResponse.<PageResponse<DatosMedicoDto>>builder()
                .success(true)
                .message("Personal en vacaciones obtenido")
                .data(enVacaciones)
                .build());
    }

    @GetMapping("/capacitacion/pendientes")
    @PreAuthorize("hasAuthority('PERSONAL_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<DatosMedicoDto>>> obtenerPendientesCapacitacion(
            @RequestParam(defaultValue = "12") int meses,
            @PageableDefault(size = 20) Pageable pageable) {

        if (meses <= 0 || meses > 120) {
            throw new BadRequestException("Los meses deben estar entre 1 y 120");
        }

        PageResponse<DatosMedicoDto> pendientes = medicoService.obtenerPendientesCapacitacion(meses, pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<DatosMedicoDto>>builder()
                .success(true)
                .message("Personal pendiente de capacitación obtenido")
                .data(pendientes)
                .build());
    }

    @GetMapping("/emergencia")
    @PreAuthorize("hasAuthority('PERSONAL_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<DatosMedicoDto>>> obtenerMedicosEmergencia(
            @PageableDefault(size = 20) Pageable pageable) {

        PageResponse<DatosMedicoDto> medicosEmergencia = medicoService.obtenerMedicosEmergencia(pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<DatosMedicoDto>>builder()
                .success(true)
                .message("Médicos de emergencia obtenidos")
                .data(medicosEmergencia)
                .build());
    }

    // ========================================
    // ESTADÍSTICAS Y UTILIDADES
    // ========================================

    @GetMapping("/estadisticas")
    @PreAuthorize("hasAuthority('PERSONAL_LEER')")
    public ResponseEntity<ApiResponse<EstadisticasDto>> obtenerEstadisticas() {
        EstadisticasDto stats = medicoService.obtenerEstadisticas();
        return ResponseEntity.ok(ApiResponse.<EstadisticasDto>builder()
                .success(true)
                .message("Estadísticas obtenidas exitosamente")
                .data(stats)
                .build());
    }

    @GetMapping("/contar/estado/{estado}")
    @PreAuthorize("hasAuthority('PERSONAL_LEER')")
    public ResponseEntity<ApiResponse<Long>> contarPorEstado(@PathVariable String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            throw new BadRequestException("El estado no puede estar vacío");
        }

        Long count = medicoService.contarPorEstado(estado);
        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .success(true)
                .message("Conteo obtenido")
                .data(count)
                .build());
    }

    @GetMapping("/contar/tipo/{tipo}")
    @PreAuthorize("hasAuthority('PERSONAL_LEER')")
    public ResponseEntity<ApiResponse<Long>> contarPorTipo(@PathVariable String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new BadRequestException("El tipo no puede estar vacío");
        }

        Long count = medicoService.contarPorTipo(tipo);
        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .success(true)
                .message("Conteo obtenido")
                .data(count)
                .build());
    }

    @GetMapping("/existe/cmp/{nroCmp}")
    @PreAuthorize("hasAuthority('PERSONAL_LEER')")
    public ResponseEntity<ApiResponse<Boolean>> existePorCmp(@PathVariable String nroCmp) {
        if (nroCmp == null || nroCmp.trim().isEmpty()) {
            throw new BadRequestException("El CMP no puede estar vacío");
        }

        boolean existe = medicoService.existePorCmp(nroCmp);
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .success(true)
                .message(existe ? "CMP ya registrado" : "CMP disponible")
                .data(existe)
                .build());
    }

    // ========================================
    // OPERACIONES ESPECIALES (PATCH)
    // ========================================

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('PERSONAL_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<Void>> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado) {

        if (id == null || id <= 0) {
            throw new BadRequestException("El ID debe ser un número positivo");
        }

        if (estado == null || estado.trim().isEmpty()) {
            throw new BadRequestException("El estado no puede estar vacío");
        }

        medicoService.cambiarEstado(id, estado);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Estado actualizado exitosamente")
                .build());
    }

    @PatchMapping("/{id}/area")
    @PreAuthorize("hasAuthority('PERSONAL_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<Void>> asignarArea(
            @PathVariable Long id,
            @RequestParam Long idArea) {

        if (id == null || id <= 0) {
            throw new BadRequestException("El ID debe ser un número positivo");
        }

        if (idArea == null || idArea <= 0) {
            throw new BadRequestException("El ID del área debe ser positivo");
        }

        medicoService.asignarArea(id, idArea);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Área asignada exitosamente")
                .build());
    }

    @PatchMapping("/{id}/sucursal")
    @PreAuthorize("hasAuthority('PERSONAL_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<Void>> asignarSucursal(
            @PathVariable Long id,
            @RequestParam Long idSucursal) {

        if (id == null || id <= 0) {
            throw new BadRequestException("El ID debe ser un número positivo");
        }

        if (idSucursal == null || idSucursal <= 0) {
            throw new BadRequestException("El ID de la sucursal debe ser positivo");
        }

        medicoService.asignarSucursal(id, idSucursal);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Sucursal asignada exitosamente")
                .build());
    }

    @PostMapping("/{id}/vacaciones")
    @PreAuthorize("hasAuthority('PERSONAL_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<Void>> programarVacaciones(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            @RequestParam Integer dias) {

        if (id == null || id <= 0) {
            throw new BadRequestException("El ID debe ser un número positivo");
        }

        medicoService.programarVacaciones(id, inicio, fin, dias);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Vacaciones programadas exitosamente")
                .build());
    }

    @PatchMapping("/{id}/honorarios")
    @PreAuthorize("hasAuthority('PERSONAL_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<Void>> actualizarHonorarios(
            @PathVariable Long id,
            @RequestParam BigDecimal monto) {

        if (id == null || id <= 0) {
            throw new BadRequestException("El ID debe ser un número positivo");
        }

        if (monto == null || monto.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("El monto debe ser mayor o igual a 0");
        }

        medicoService.actualizarHonorarios(id, monto);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Honorarios actualizados exitosamente")
                .build());
    }
}
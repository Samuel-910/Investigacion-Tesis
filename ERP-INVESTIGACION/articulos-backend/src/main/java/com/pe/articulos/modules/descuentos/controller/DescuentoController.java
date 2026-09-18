package com.pe.articulos.modules.descuentos.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.descuentos.dto.DescuentoDTO;
import com.pe.articulos.modules.descuentos.service.DescuentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

@Tag(name = "Descuentos", description = "API para gestionar descuentos maestro-detalle")
@RestController
@RequestMapping("/api/descuentos")
@RequiredArgsConstructor
public class DescuentoController {

    private final DescuentoService service;

    @Operation(summary = "Crear o actualizar descuento")
    @PostMapping
    public ResponseEntity<ApiResponse<DescuentoDTO>> guardar(@RequestBody DescuentoDTO dto) {
        DescuentoDTO response = service.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<DescuentoDTO>builder()
                        .success(true)
                        .message("Descuento guardado exitosamente")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Obtener descuento por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DescuentoDTO>> obtenerPorId(@PathVariable Long id) {
        DescuentoDTO response = service.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.<DescuentoDTO>builder()
                .success(true)
                .message("Descuento encontrado")
                .data(response)
                .build());
    }

    @Operation(summary = "Listar descuentos con filtros")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DescuentoDTO>>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) Long idCompania) {

        PageResponse<DescuentoDTO> response = service.listar(page, size, nombre, activo, idCompania);
        return ResponseEntity.ok(ApiResponse.<PageResponse<DescuentoDTO>>builder()
                .success(true)
                .message("Descuentos listados con éxito")
                .data(response)
                .build());
    }

    @Operation(summary = "Listar descuentos vigentes")
    @GetMapping("/vigentes")
    public ResponseEntity<ApiResponse<PageResponse<DescuentoDTO>>> listarVigentes(Pageable pageable) {
        PageResponse<DescuentoDTO> response = service.listarVigentes(pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<DescuentoDTO>>builder()
                .success(true)
                .message("Descuentos vigentes obtenidos")
                .data(response)
                .build());
    }

    @Operation(summary = "Listar descuentos aplicables para un paciente")
    @GetMapping("/aplicables/{idPaciente}")
    public ResponseEntity<ApiResponse<java.util.List<DescuentoDTO>>> listarAplicables(@PathVariable Long idPaciente) {
        java.util.List<DescuentoDTO> response = service.listarAplicables(idPaciente);
        return ResponseEntity.ok(ApiResponse.<java.util.List<DescuentoDTO>>builder()
                .success(true)
                .message("Descuentos aplicables obtenidos")
                .data(response)
                .build());
    }

    @Operation(summary = "Eliminar descuento")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Descuento eliminado correctamente")
                .build());
    }
}

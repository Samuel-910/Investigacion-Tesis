package com.pe.articulos.modules.puntos.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.puntos.dto.PuntoRequestDTO;
import com.pe.articulos.modules.puntos.dto.PuntoResponseDTO;
import com.pe.articulos.modules.puntos.service.PuntoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/puntos")
@RequiredArgsConstructor
public class PuntoController {

    private final PuntoService puntoService;

    /**
     * Crear un nuevo punto
     * POST /api/puntos
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PuntoResponseDTO>> crearPunto(@Valid @RequestBody PuntoRequestDTO requestDTO) {
        PuntoResponseDTO responseDTO = puntoService.crearPunto(requestDTO);
        return new ResponseEntity<>(ApiResponse.<PuntoResponseDTO>builder()
                .success(true)
                .message("Punto creado exitosamente")
                .data(responseDTO)
                .build(), HttpStatus.CREATED);
    }

    /**
     * Obtener un punto por ID
     * GET /api/puntos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PuntoResponseDTO>> obtenerPuntoPorId(@PathVariable Long id) {
        PuntoResponseDTO responseDTO = puntoService.obtenerPuntoPorId(id);
        return ResponseEntity.ok(ApiResponse.<PuntoResponseDTO>builder()
                .success(true)
                .message("Punto obtenido exitosamente")
                .data(responseDTO)
                .build());
    }

    /**
     * Obtener todos los puntos paginados
     * GET /api/puntos
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PuntoResponseDTO>>> obtenerTodosPuntos(
            @RequestParam Integer idSucursal,
            @PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
        PageResponse<PuntoResponseDTO> response = puntoService.obtenerTodosPuntos(idSucursal, pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<PuntoResponseDTO>>builder()
                .success(true)
                .message("Puntos obtenidos exitosamente")
                .data(response)
                .build());
    }

    /**
     * Obtener puntos activos paginados
     * GET /api/puntos/activos
     */
    @GetMapping("/activos")
    public ResponseEntity<ApiResponse<PageResponse<PuntoResponseDTO>>> obtenerPuntosActivos(
            @RequestParam Integer idSucursal,
            @PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
        PageResponse<PuntoResponseDTO> response = puntoService.obtenerPuntosActivos(idSucursal, pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<PuntoResponseDTO>>builder()
                .success(true)
                .message("Puntos activos obtenidos exitosamente")
                .data(response)
                .build());
    }

    /**
     * Obtener lista de todos los puntos (sin paginación, para selects)
     * GET /api/puntos/list
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<PuntoResponseDTO>>> obtenerTodosPuntosList(
            @RequestParam Integer idSucursal) {
        List<PuntoResponseDTO> puntos = puntoService.obtenerTodosPuntosList(idSucursal);
        return ResponseEntity.ok(ApiResponse.<List<PuntoResponseDTO>>builder()
                .success(true)
                .message("Lista de puntos obtenida exitosamente")
                .data(puntos)
                .build());
    }

    /**
     * Obtener lista de puntos activos (sin paginación, para selects)
     * GET /api/puntos/activos/list
     */
    @GetMapping("/activos/list")
    public ResponseEntity<ApiResponse<List<PuntoResponseDTO>>> obtenerPuntosActivosList(
            @RequestParam Integer idSucursal) {
        List<PuntoResponseDTO> puntos = puntoService.obtenerPuntosActivosList(idSucursal);
        return ResponseEntity.ok(ApiResponse.<List<PuntoResponseDTO>>builder()
                .success(true)
                .message("Lista de puntos activos obtenida exitosamente")
                .data(puntos)
                .build());
    }

    /**
     * Actualizar un punto
     * PUT /api/puntos/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PuntoResponseDTO>> actualizarPunto(
            @PathVariable Long id,
            @Valid @RequestBody PuntoRequestDTO requestDTO) {
        PuntoResponseDTO responseDTO = puntoService.actualizarPunto(id, requestDTO);
        return ResponseEntity.ok(ApiResponse.<PuntoResponseDTO>builder()
                .success(true)
                .message("Punto actualizado exitosamente")
                .data(responseDTO)
                .build());
    }

    /**
     * Eliminar un punto
     * DELETE /api/puntos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPunto(@PathVariable Long id) {
        puntoService.eliminarPunto(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Buscar puntos por nombre
     * GET /api/puntos/buscar/nombre?nombre=valor
     */
    @GetMapping("/buscar/nombre")
    public ResponseEntity<ApiResponse<List<PuntoResponseDTO>>> buscarPorNombre(@RequestParam String nombre) {
        List<PuntoResponseDTO> puntos = puntoService.buscarPorNombre(nombre);
        return ResponseEntity.ok(ApiResponse.<List<PuntoResponseDTO>>builder()
                .success(true)
                .message("Búsqueda por nombre completada exitosamente")
                .data(puntos)
                .build());
    }

    /**
     * Buscar puntos por tipo
     * GET /api/puntos/buscar/tipo?tipo=valor
     */
    @GetMapping("/buscar/tipo")
    public ResponseEntity<ApiResponse<List<PuntoResponseDTO>>> buscarPorTipo(@RequestParam String tipo) {
        List<PuntoResponseDTO> puntos = puntoService.buscarPorTipo(tipo);
        return ResponseEntity.ok(ApiResponse.<List<PuntoResponseDTO>>builder()
                .success(true)
                .message("Búsqueda por tipo completada exitosamente")
                .data(puntos)
                .build());
    }

    /**
     * Buscar puntos por sucursal
     * GET /api/puntos/buscar/sucursal?idSucursal=1
     */
    @GetMapping("/buscar/sucursal")
    public ResponseEntity<ApiResponse<List<PuntoResponseDTO>>> buscarPorSucursal(@RequestParam Integer idSucursal) {
        List<PuntoResponseDTO> puntos = puntoService.buscarPorSucursal(idSucursal);
        return ResponseEntity.ok(ApiResponse.<List<PuntoResponseDTO>>builder()
                .success(true)
                .message("Búsqueda por sucursal completada exitosamente")
                .data(puntos)
                .build());
    }

    /**
     * Buscar puntos por múltiples criterios
     * GET /api/puntos/buscar?nombre=valor&tipo=valor&idSucursal=1
     */
    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<PuntoResponseDTO>>> buscarPorCriterios(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer idSucursal) {
        List<PuntoResponseDTO> puntos = puntoService.buscarPorCriterios(nombre, tipo, idSucursal);
        return ResponseEntity.ok(ApiResponse.<List<PuntoResponseDTO>>builder()
                .success(true)
                .message("Búsqueda por criterios completada exitosamente")
                .data(puntos)
                .build());
    }

    /**
     * Verificar si existe un punto por nombre
     * GET /api/puntos/existe/nombre?nombre=valor
     */
    @GetMapping("/existe/nombre")
    public ResponseEntity<ApiResponse<Boolean>> existePorNombre(@RequestParam String nombre) {
        boolean existe = puntoService.existePorNombre(nombre);
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .success(true)
                .message("Verificación de existencia completada exitosamente")
                .data(existe)
                .build());
    }

    /**
     * Contar puntos por sucursal
     * GET /api/puntos/contar/sucursal?idSucursal=1
     */
    @GetMapping("/contar/sucursal")
    public ResponseEntity<ApiResponse<Long>> contarPorSucursal(@RequestParam Integer idSucursal) {
        long cantidad = puntoService.contarPorSucursal(idSucursal);
        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .success(true)
                .message("Conteo de puntos completado exitosamente")
                .data(cantidad)
                .build());
    }

    /**
     * Buscar puntos paginados por término global
     * GET /api/puntos/search?q=valor
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<PuntoResponseDTO>>> buscar(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) String tipo,
            @RequestParam Integer idSucursal,
            @PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
        PageResponse<PuntoResponseDTO> response = puntoService.buscar(q, tipo, idSucursal, pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<PuntoResponseDTO>>builder()
                .success(true)
                .message("Búsqueda realizada exitosamente")
                .data(response)
                .build());
    }
}

package com.pe.articulos.modules.niveles.controller;

import com.pe.articulos.modules.niveles.dto.*;
import com.pe.articulos.modules.niveles.service.NivelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/niveles")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NivelController {

    private final NivelService nivelService;

    // ============================================
    // CRUD BÁSICO
    // ============================================

    @PostMapping
    public ResponseEntity<RespuestaGeneralDto<NivelDto>> crear(
            @Valid @RequestBody NivelCreateDto dto) {
        try {
            log.info("POST /api/v1/niveles - Crear nivel");
            NivelDto creado = nivelService.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(RespuestaGeneralDto.exitoso("Nivel creado exitosamente", creado));
        } catch (IllegalArgumentException e) {
            log.error("Error al crear nivel: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RespuestaGeneralDto.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al crear nivel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RespuestaGeneralDto.error("Error interno del servidor"));
        }
    }

    @GetMapping
    public ResponseEntity<RespuestaGeneralDto<Page<NivelDto>>> obtenerTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orden") String sortBy) {
        try {
            log.info("GET /api/v1/niveles - Obtener todos (page={}, size={})", page, size);

            Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
            Page<NivelDto> niveles = nivelService.obtenerTodosPaginado(pageable);

            return ResponseEntity.ok(
                    RespuestaGeneralDto.exitoso("Niveles obtenidos exitosamente", niveles));
        } catch (Exception e) {
            log.error("Error al obtener niveles", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RespuestaGeneralDto.error("Error al obtener niveles"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaGeneralDto<NivelDto>> obtenerPorId(@PathVariable Long id) {
        try {
            log.info("GET /api/v1/niveles/{} - Obtener por ID", id);
            return nivelService.obtenerPorId(id)
                    .map(dto -> ResponseEntity.ok(
                            RespuestaGeneralDto.exitoso("Nivel encontrado", dto)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(RespuestaGeneralDto.error("Nivel no encontrado")));
        } catch (Exception e) {
            log.error("Error al obtener nivel por ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RespuestaGeneralDto.error("Error al obtener nivel"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaGeneralDto<NivelDto>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody NivelDto dto) {
        try {
            log.info("PUT /api/v1/niveles/{} - Actualizar", id);
            NivelDto actualizado = nivelService.actualizar(id, dto);
            return ResponseEntity.ok(
                    RespuestaGeneralDto.exitoso("Nivel actualizado exitosamente", actualizado));
        } catch (IllegalArgumentException e) {
            log.error("Error al actualizar: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RespuestaGeneralDto.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al actualizar", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RespuestaGeneralDto.error("Error interno del servidor"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaGeneralDto<Void>> eliminar(@PathVariable Long id) {
        try {
            log.info("DELETE /api/v1/niveles/{} - Eliminar", id);
            nivelService.eliminar(id);
            return ResponseEntity.ok(
                    RespuestaGeneralDto.exitoso("Nivel eliminado exitosamente"));
        } catch (IllegalArgumentException e) {
            log.error("Error al eliminar: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RespuestaGeneralDto.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al eliminar", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RespuestaGeneralDto.error("Error interno"));
        }
    }

    // ============================================
    // ENDPOINTS DE JERARQUÍA
    // ============================================

    @GetMapping("/raiz")
    public ResponseEntity<RespuestaGeneralDto<Page<NivelDto>>> obtenerNivelesRaiz(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("GET /api/v1/niveles/raiz - Obtener niveles raíz");

            Pageable pageable = PageRequest.of(page, size, Sort.by("orden"));
            Page<NivelDto> niveles = nivelService.obtenerNivelesRaizPaginado(pageable);

            return ResponseEntity.ok(
                    RespuestaGeneralDto.exitoso("Niveles raíz obtenidos", niveles));
        } catch (Exception e) {
            log.error("Error al obtener niveles raíz", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RespuestaGeneralDto.error("Error al obtener niveles raíz"));
        }
    }

    @GetMapping("/{id}/hijos")
    public ResponseEntity<RespuestaGeneralDto<List<NivelDto>>> obtenerHijos(
            @PathVariable Long id) {
        try {
            log.info("GET /api/v1/niveles/{}/hijos - Obtener hijos", id);
            List<NivelDto> hijos = nivelService.obtenerHijos(id);
            return ResponseEntity.ok(
                    RespuestaGeneralDto.exitoso("Hijos obtenidos exitosamente", hijos));
        } catch (Exception e) {
            log.error("Error al obtener hijos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RespuestaGeneralDto.error("Error al obtener hijos"));
        }
    }

    @GetMapping("/arbol")
    public ResponseEntity<RespuestaGeneralDto<List<NivelTreeDto>>> obtenerArbolCompleto() {
        try {
            log.info("GET /api/v1/niveles/arbol - Obtener árbol completo");
            List<NivelTreeDto> arbol = nivelService.obtenerArbolCompleto();
            return ResponseEntity.ok(
                    RespuestaGeneralDto.exitoso("Árbol de niveles obtenido", arbol));
        } catch (Exception e) {
            log.error("Error al obtener árbol", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RespuestaGeneralDto.error("Error al obtener árbol"));
        }
    }

    @GetMapping("/{id}/arbol")
    public ResponseEntity<RespuestaGeneralDto<List<NivelTreeDto>>> obtenerArbolDesde(
            @PathVariable Long id) {
        try {
            log.info("GET /api/v1/niveles/{}/arbol - Obtener árbol desde nivel", id);
            List<NivelTreeDto> arbol = nivelService.obtenerArbolDesde(id);
            return ResponseEntity.ok(
                    RespuestaGeneralDto.exitoso("Árbol obtenido", arbol));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(RespuestaGeneralDto.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener árbol desde nivel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RespuestaGeneralDto.error("Error al obtener árbol"));
        }
    }

    // ============================================
    // OPERACIONES ESPECIALES
    // ============================================

    @PatchMapping("/mover")
    public ResponseEntity<RespuestaGeneralDto<Void>> moverNivel(
            @Valid @RequestBody NivelMoverDto dto) {
        try {
            log.info("PATCH /api/v1/niveles/mover - Mover nivel");
            nivelService.moverNivel(dto);
            return ResponseEntity.ok(
                    RespuestaGeneralDto.exitoso("Nivel movido exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RespuestaGeneralDto.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al mover nivel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RespuestaGeneralDto.error("Error al mover nivel"));
        }
    }

    @PatchMapping("/{id}/orden")
    public ResponseEntity<RespuestaGeneralDto<Void>> cambiarOrden(
            @PathVariable Long id,
            @RequestParam Integer nuevoOrden) {
        try {
            log.info("PATCH /api/v1/niveles/{}/orden - Cambiar orden", id);
            nivelService.cambiarOrden(id, nuevoOrden);
            return ResponseEntity.ok(
                    RespuestaGeneralDto.exitoso("Orden actualizado exitosamente"));
        } catch (Exception e) {
            log.error("Error al cambiar orden", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RespuestaGeneralDto.error("Error al cambiar orden"));
        }
    }

    // ============================================
    // BÚSQUEDAS Y ESTADÍSTICAS
    // ============================================

    @GetMapping("/buscar")
    public ResponseEntity<RespuestaGeneralDto<Page<NivelDto>>> buscar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long idNivelPadre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("GET /api/v1/niveles/buscar - Buscar con filtros");

            Pageable pageable = PageRequest.of(page, size, Sort.by("orden"));
            Page<NivelDto> resultados = nivelService.buscarConFiltros(
                    nombre, tipo, com.pe.articulos.core.enums.EstadoGeneral.fromCodigo(estado), idNivelPadre, pageable);

            return ResponseEntity.ok(
                    RespuestaGeneralDto.exitoso("Búsqueda completada", resultados));
        } catch (Exception e) {
            log.error("Error al buscar", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RespuestaGeneralDto.error("Error al buscar"));
        }
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<RespuestaGeneralDto<EstadisticasNivelDto>> obtenerEstadisticas() {
        try {
            log.info("GET /api/v1/niveles/estadisticas - Obtener estadísticas");
            EstadisticasNivelDto stats = nivelService.obtenerEstadisticas();
            return ResponseEntity.ok(
                    RespuestaGeneralDto.exitoso("Estadísticas obtenidas", stats));
        } catch (Exception e) {
            log.error("Error al obtener estadísticas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RespuestaGeneralDto.error("Error al obtener estadísticas"));
        }
    }
}
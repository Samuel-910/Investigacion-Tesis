package com.pe.articulos.modules.puntos.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.puntos.dto.PuntoDocumentoRequestDTO;
import com.pe.articulos.modules.puntos.dto.PuntoDocumentoResponseDTO;
import com.pe.articulos.modules.puntos.entity.PuntoDocumentoAuditoria;
import com.pe.articulos.modules.puntos.service.PuntoDocumentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PuntoDocumentoController {

    private final PuntoDocumentoService puntoDocumentoService;

    /**
     * Crear un nuevo documento
     * POST /api/documentos
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PuntoDocumentoResponseDTO>> crearDocumento(
            @Valid @RequestBody PuntoDocumentoRequestDTO requestDTO) {
        PuntoDocumentoResponseDTO responseDTO = puntoDocumentoService.crearDocumento(requestDTO);
        return new ResponseEntity<>(ApiResponse.<PuntoDocumentoResponseDTO>builder()
                .success(true)
                .message("Documento creado exitosamente")
                .data(responseDTO)
                .build(), HttpStatus.CREATED);
    }

    /**
     * Obtener un documento por ID
     * GET /api/documentos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PuntoDocumentoResponseDTO>> obtenerDocumentoPorId(@PathVariable Long id) {
        PuntoDocumentoResponseDTO responseDTO = puntoDocumentoService.obtenerDocumentoPorId(id);
        return ResponseEntity.ok(ApiResponse.<PuntoDocumentoResponseDTO>builder()
                .success(true)
                .message("Documento obtenido exitosamente")
                .data(responseDTO)
                .build());
    }

    /**
     * Obtener todos los documentos paginados
     * GET /api/documentos
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PuntoDocumentoResponseDTO>>> obtenerTodosDocumentos(
            @RequestParam(required = false) Long puntoId,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        PageResponse<PuntoDocumentoResponseDTO> response = puntoDocumentoService.obtenerTodosDocumentos(puntoId, pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<PuntoDocumentoResponseDTO>>builder()
                .success(true)
                .message("Documentos obtenidos exitosamente")
                .data(response)
                .build());
    }

    /**
     * Obtener todos los documentos como lista
     * GET /api/documentos/list
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<PuntoDocumentoResponseDTO>>> obtenerTodosDocumentosList() {
        List<PuntoDocumentoResponseDTO> documentos = puntoDocumentoService.obtenerTodosDocumentosList();
        return ResponseEntity.ok(ApiResponse.<List<PuntoDocumentoResponseDTO>>builder()
                .success(true)
                .message("Documentos obtenidos exitosamente")
                .data(documentos)
                .build());
    }

    /**
     * Actualizar un documento
     * PUT /api/documentos/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PuntoDocumentoResponseDTO>> actualizarDocumento(
            @PathVariable Long id,
            @Valid @RequestBody PuntoDocumentoRequestDTO requestDTO) {
        PuntoDocumentoResponseDTO responseDTO = puntoDocumentoService.actualizarDocumento(id, requestDTO);
        return ResponseEntity.ok(ApiResponse.<PuntoDocumentoResponseDTO>builder()
                .success(true)
                .message("Documento actualizado exitosamente")
                .data(responseDTO)
                .build());
    }

    /**
     * Eliminar un documento
     * DELETE /api/documentos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDocumento(@PathVariable Long id) {
        puntoDocumentoService.eliminarDocumento(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener documentos de un punto específico
     * GET /api/documentos/punto/{puntoId}
     */
    @GetMapping("/punto/{puntoId}")
    public ResponseEntity<ApiResponse<List<PuntoDocumentoResponseDTO>>> obtenerDocumentosPorPunto(
            @PathVariable Long puntoId) {
        List<PuntoDocumentoResponseDTO> documentos = puntoDocumentoService.obtenerDocumentosPorPunto(puntoId);
        return ResponseEntity.ok(ApiResponse.<List<PuntoDocumentoResponseDTO>>builder()
                .success(true)
                .message("Documentos del punto obtenidos exitosamente")
                .data(documentos)
                .build());
    }

    /**
     * Buscar documentos por tipo
     * GET /api/documentos/buscar/tipo?tipoDoc=valor
     */
    @GetMapping("/buscar/tipo")
    public ResponseEntity<ApiResponse<List<PuntoDocumentoResponseDTO>>> buscarPorTipoDocumento(
            @RequestParam String tipoDoc) {
        List<PuntoDocumentoResponseDTO> documentos = puntoDocumentoService.buscarPorTipoDocumento(tipoDoc);
        return ResponseEntity.ok(ApiResponse.<List<PuntoDocumentoResponseDTO>>builder()
                .success(true)
                .message("Documentos filtrados por tipo obtenidos exitosamente")
                .data(documentos)
                .build());
    }

    /**
     * Buscar documentos por serie
     * GET /api/documentos/buscar/serie?serie=valor
     */
    @GetMapping("/buscar/serie")
    public ResponseEntity<ApiResponse<List<PuntoDocumentoResponseDTO>>> buscarPorSerie(@RequestParam String serie) {
        List<PuntoDocumentoResponseDTO> documentos = puntoDocumentoService.buscarPorSerie(serie);
        return ResponseEntity.ok(ApiResponse.<List<PuntoDocumentoResponseDTO>>builder()
                .success(true)
                .message("Documentos filtrados por serie obtenidos exitosamente")
                .data(documentos)
                .build());
    }

    /**
     * Buscar documentos por estado
     * GET /api/documentos/buscar/estado?estado=valor
     */
    @GetMapping("/buscar/estado")
    public ResponseEntity<ApiResponse<List<PuntoDocumentoResponseDTO>>> buscarPorEstado(@RequestParam String estado) {
        List<PuntoDocumentoResponseDTO> documentos = puntoDocumentoService.buscarPorEstado(estado);
        return ResponseEntity.ok(ApiResponse.<List<PuntoDocumentoResponseDTO>>builder()
                .success(true)
                .message("Documentos filtrados por estado obtenidos exitosamente")
                .data(documentos)
                .build());
    }

    /**
     * Obtener documentos activos de un punto
     * GET /api/documentos/activos/punto/{puntoId}
     */
    @GetMapping("/activos/punto/{puntoId}")
    public ResponseEntity<ApiResponse<List<PuntoDocumentoResponseDTO>>> obtenerDocumentosActivosPorPunto(
            @PathVariable Long puntoId,
            @RequestParam(required = false) List<String> modulos) {
        List<PuntoDocumentoResponseDTO> documentos = puntoDocumentoService.obtenerDocumentosActivosPorPunto(puntoId, modulos);
        return ResponseEntity.ok(ApiResponse.<List<PuntoDocumentoResponseDTO>>builder()
                .success(true)
                .message("Documentos activos del punto obtenidos exitosamente")
                .data(documentos)
                .build());
    }

    /**
     * Obtener documentos activos de una sucursal
     * GET /api/documentos/activos/sucursal/{sucursalId}
     */
    @GetMapping("/activos/sucursal/{sucursalId}")
    public ResponseEntity<ApiResponse<List<PuntoDocumentoResponseDTO>>> obtenerDocumentosActivosPorSucursal(
            @PathVariable Long sucursalId,
            @RequestParam(required = false) List<String> modulos) {
        List<PuntoDocumentoResponseDTO> documentos = puntoDocumentoService.obtenerDocumentosActivosPorSucursal(sucursalId, modulos);
        return ResponseEntity.ok(ApiResponse.<List<PuntoDocumentoResponseDTO>>builder()
                .success(true)
                .message("Documentos activos de la sucursal obtenidos exitosamente")
                .data(documentos)
                .build());
    }

    /**
     * Buscar documentos por múltiples criterios
     * GET /api/documentos/buscar?puntoId=1&tipoDoc=FACTURA&estado=ACTIVO
     */
    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<PuntoDocumentoResponseDTO>>> buscarPorCriterios(
            @RequestParam(required = false) Long puntoId,
            @RequestParam(required = false) String tipoDoc,
            @RequestParam(required = false) String estado) {
        List<PuntoDocumentoResponseDTO> documentos = puntoDocumentoService.buscarPorCriterios(puntoId, tipoDoc, estado);
        return ResponseEntity.ok(ApiResponse.<List<PuntoDocumentoResponseDTO>>builder()
                .success(true)
                .message("Búsqueda de documentos completada exitosamente")
                .data(documentos)
                .build());
    }

    /**
     * Verificar si existe un documento con serie y número
     * GET /api/documentos/existe?serie=F001&numero=123
     */
    @GetMapping("/existe")
    public ResponseEntity<ApiResponse<Boolean>> existePorSerieYNumero(
            @RequestParam String serie,
            @RequestParam Integer numero) {
        boolean existe = puntoDocumentoService.existePorSerieYNumero(serie, numero);
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .success(true)
                .message("Verificación de existencia completada")
                .data(existe)
                .build());
    }

    /**
     * Contar documentos de un punto
     * GET /api/documentos/contar/punto/{puntoId}
     */
    @GetMapping("/contar/punto/{puntoId}")
    public ResponseEntity<ApiResponse<Long>> contarDocumentosPorPunto(@PathVariable Long puntoId) {
        long cantidad = puntoDocumentoService.contarDocumentosPorPunto(puntoId);
        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .success(true)
                .message("Conteo de documentos completado")
                .data(cantidad)
                .build());
    }

    /**
     * Obtener último número de documento por serie y tipo
     * GET /api/documentos/ultimo-numero?serie=F001&tipoDoc=FACTURA
     */
    @GetMapping("/ultimo-numero")
    public ResponseEntity<ApiResponse<Integer>> obtenerUltimoNumeroPorSerieYTipo(
            @RequestParam String serie,
            @RequestParam String tipoDoc) {
        Integer ultimoNumero = puntoDocumentoService.obtenerUltimoNumeroPorSerieYTipo(serie, tipoDoc);
        return ResponseEntity.ok(ApiResponse.<Integer>builder()
                .success(true)
                .message("Último número obtenido exitosamente")
                .data(ultimoNumero)
                .build());
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<ApiResponse<List<PuntoDocumentoAuditoria>>> obtenerHistorial(@PathVariable Long id) {
        List<PuntoDocumentoAuditoria> historial = puntoDocumentoService.listarHistorial(id);
        return ResponseEntity.ok(ApiResponse.<List<PuntoDocumentoAuditoria>>builder()
                .success(true)
                .message("Historial obtenido exitosamente")
                .data(historial)
                .build());
    }

    /**
     * Obtener tipos de documentos asignados
     * GET /api/documentos/tipos-asignados?modulo=VENTAS&sucursalId=1
     */
    @GetMapping("/tipos-asignados")
    public ResponseEntity<ApiResponse<List<com.pe.articulos.modules.venta_registro.entity.TipoDocumento>>> obtenerTiposAsignados(
            @RequestParam String modulo,
            @RequestParam(required = false) Long sucursalId) {
        List<com.pe.articulos.modules.venta_registro.entity.TipoDocumento> tipos = puntoDocumentoService.obtenerTiposDocumentosAsignados(modulo, sucursalId);
        return ResponseEntity.ok(ApiResponse.<List<com.pe.articulos.modules.venta_registro.entity.TipoDocumento>>builder()
                .success(true)
                .message("Tipos de documentos obtenidos exitosamente")
                .data(tipos)
                .build());
    }
}

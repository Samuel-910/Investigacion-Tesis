package com.pe.articulos.modules.compras.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.compras.dto.CompraRequest;
import com.pe.articulos.modules.compras.dto.OrdenCreateRequest;
import com.pe.articulos.modules.compras.dto.CompraResponse;
import com.pe.articulos.modules.compras.dto.DetalleCompraResponse;
import com.pe.articulos.modules.compras.service.CompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;
    private final com.pe.articulos.modules.documentos.services.DocumentoImpresionService documentoImpresionService;

    @PostMapping
    public ResponseEntity<ApiResponse<CompraResponse>> registrar(
            @Valid @RequestBody CompraRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String userId = String.valueOf(com.pe.articulos.core.security.SecurityUtils.getCurrentUserId());
        CompraResponse response = compraService.registrar(request, userId);
        return new ResponseEntity<>(
                new ApiResponse<>("Compra registrada exitosamente", response, HttpStatus.CREATED.value(), true),
                HttpStatus.CREATED);
    }

    @PostMapping("/orden")
    public ResponseEntity<ApiResponse<CompraResponse>> crearOrden(
            @Valid @RequestBody OrdenCreateRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String userId = String.valueOf(com.pe.articulos.core.security.SecurityUtils.getCurrentUserId());
        CompraResponse response = compraService.crearOrden(request, userId);
        return new ResponseEntity<>(
                new ApiResponse<>("Orden creada exitosamente", response, HttpStatus.CREATED.value(), true),
                HttpStatus.CREATED);
    }

    @PutMapping("/orden/{id}")
    public ResponseEntity<ApiResponse<CompraResponse>> actualizarOrden(
            @PathVariable Long id,
            @Valid @RequestBody OrdenCreateRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String userId = String.valueOf(com.pe.articulos.core.security.SecurityUtils.getCurrentUserId());
        CompraResponse response = compraService.actualizarOrden(id, request, userId);
        return new ResponseEntity<>(
                new ApiResponse<>("Orden actualizada exitosamente", response, HttpStatus.OK.value(), true),
                HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CompraResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CompraRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String userId = String.valueOf(com.pe.articulos.core.security.SecurityUtils.getCurrentUserId());
        CompraResponse response = compraService.actualizar(id, request, userId);
        return new ResponseEntity<>(
                new ApiResponse<>("Compra actualizada exitosamente", response, HttpStatus.OK.value(), true),
                HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompraResponse>> obtener(@PathVariable Long id) {
        CompraResponse response = compraService.obtener(id);
        return new ResponseEntity<>(new ApiResponse<>("Compra obtenida", response, HttpStatus.OK.value(), true),
                HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CompraResponse>>> listar(
            @RequestParam Long idSucursal,
            @RequestParam(required = false) String estado,
            Pageable pageable) {
        PageResponse<CompraResponse> response = compraService.listar(idSucursal, estado, pageable);
        return new ResponseEntity<>(
                new ApiResponse<>("Lista de compras obtenida", response, HttpStatus.OK.value(), true), HttpStatus.OK);
    }

    @GetMapping("/orden")
    public ResponseEntity<ApiResponse<PageResponse<CompraResponse>>> listarOrdenes(
            @RequestParam Long idSucursal,
            Pageable pageable) {
        PageResponse<CompraResponse> response = compraService.listarOrdenes(idSucursal, pageable);
        return new ResponseEntity<>(
                new ApiResponse<>("Lista de órdenes obtenida", response, HttpStatus.OK.value(), true), HttpStatus.OK);
    }

    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<PageResponse<CompraResponse>>> buscar(
            @RequestParam Long idSucursal,
            @RequestParam(required = false) Long idProveedor,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String estado,
            Pageable pageable) {
        PageResponse<CompraResponse> response = compraService.buscar(idSucursal, idProveedor, fechaInicio,
                fechaFin, estado, pageable);
        return new ResponseEntity<>(new ApiResponse<>("Búsqueda completada", response, HttpStatus.OK.value(), true),
                HttpStatus.OK);
    }

    @GetMapping("/detalles/buscar-producto")
    public ResponseEntity<ApiResponse<PageResponse<DetalleCompraResponse>>> buscarDetallesPorProducto(
            @RequestParam String term, @RequestHeader(value = "idSucursal", required = false) Long headerSucursal, @RequestParam(required = false) Long idSucursal, Pageable pageable) {
        Long sucursalId = headerSucursal != null ? headerSucursal : idSucursal;
        PageResponse<DetalleCompraResponse> response = compraService.buscarDetallesPorProducto(term, sucursalId, pageable);
        return new ResponseEntity<>(new ApiResponse<>("Detalles encontrados", response, HttpStatus.OK.value(), true),
                HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> anular(@PathVariable Long id, @RequestParam(required = false) String motivo) {
        try {
            compraService.solicitarAnulacion(id, motivo);
            return ResponseEntity
                    .ok(ApiResponse.builder().success(true).message("Solicitud de anulación enviada").build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Error al anular: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/producto/{idCatalogo}/precio-maximo")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> obtenerPrecioMaximo(@PathVariable Long idCatalogo) {
        java.math.BigDecimal response = compraService.obtenerPrecioMaximo(idCatalogo);
        return new ResponseEntity<>(new ApiResponse<>("Precio máximo", response, HttpStatus.OK.value(), true),
                HttpStatus.OK);
    }

    @GetMapping("/producto/{idCatalogo}/historial")
    public ResponseEntity<ApiResponse<PageResponse<DetalleCompraResponse>>> obtenerHistorialPorProducto(
            @PathVariable Long idCatalogo, Pageable pageable) {
        PageResponse<DetalleCompraResponse> response = compraService.obtenerHistorialPorProducto(idCatalogo, pageable);
        return new ResponseEntity<>(new ApiResponse<>("Historial obtenido", response, HttpStatus.OK.value(), true),
                HttpStatus.OK);
    }

    @GetMapping("/grupos")
    public ResponseEntity<ApiResponse<PageResponse<String>>> listarGrupos(
            @RequestParam Long idSucursal,
            Pageable pageable) {
        PageResponse<String> response = compraService.listarGrupos(idSucursal, pageable);
        return new ResponseEntity<>(new ApiResponse<>("Grupos obtenidos", response, HttpStatus.OK.value(), true),
                HttpStatus.OK);
    }

    @GetMapping("/grupos/productos")
    public ResponseEntity<ApiResponse<PageResponse<DetalleCompraResponse>>> obtenerProductosPorGrupo(
            @RequestParam String nombreGrupo,
            @RequestParam Long idSucursal,
            Pageable pageable) {
        PageResponse<DetalleCompraResponse> response = compraService.obtenerProductosPorGrupo(nombreGrupo, idSucursal,
                pageable);
        return new ResponseEntity<>(new ApiResponse<>("Detalles del grupo", response, HttpStatus.OK.value(), true),
                HttpStatus.OK);
    }

    @GetMapping("/grupos/proveedores")
    public ResponseEntity<ApiResponse<PageResponse<Long>>> obtenerProveedoresPorGrupo(
            @RequestParam String nombreGrupo,
            @RequestParam Long idSucursal,
            Pageable pageable) {
        PageResponse<Long> response = compraService.obtenerProveedoresPorGrupo(nombreGrupo, idSucursal, pageable);
        return new ResponseEntity<>(new ApiResponse<>("Proveedores del grupo", response, HttpStatus.OK.value(), true),
                HttpStatus.OK);
    }

    @PostMapping("/{id}/seleccionar-ganadora")
    public ResponseEntity<ApiResponse<CompraResponse>> seleccionarGanadora(
            @PathVariable Long id,
            @RequestParam String nombreGrupo) {
        CompraResponse response = compraService.seleccionarGanadora(id, nombreGrupo);
        return new ResponseEntity<>(new ApiResponse<>("Ganadora seleccionada", response, HttpStatus.OK.value(), true),
                HttpStatus.OK);
    }

    @GetMapping("/{id}/imprimir")
    public ResponseEntity<ApiResponse<String>> imprimirCompra(@PathVariable Long id) {
        String html = documentoImpresionService.generarHtmlCompra(id);
        return new ResponseEntity<>(new ApiResponse<>("HTML generado exitosamente", html, HttpStatus.OK.value(), true),
                HttpStatus.OK);
    }

    @GetMapping("/grupos/imprimir")
    public ResponseEntity<ApiResponse<String>> imprimirGrupo(
            @RequestParam String nombreGrupo,
            @RequestParam Long idSucursal) {
        String html = documentoImpresionService.generarHtmlMatriz(nombreGrupo, idSucursal);
        return new ResponseEntity<>(new ApiResponse<>("HTML generado exitosamente", html, HttpStatus.OK.value(), true),
                HttpStatus.OK);
    }
}

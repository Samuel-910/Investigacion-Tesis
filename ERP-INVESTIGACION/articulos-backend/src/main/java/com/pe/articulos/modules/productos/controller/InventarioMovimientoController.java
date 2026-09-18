package com.pe.articulos.modules.productos.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.productos.dto.AjusteInventarioRequest;
import com.pe.articulos.modules.productos.dto.MovimientoDiversoBatchRequest;
import com.pe.articulos.modules.productos.dto.AjusteInventarioResponse;
import com.pe.articulos.modules.productos.dto.ClasificacionMovimientoResponse;
import com.pe.articulos.modules.productos.dto.MovimientoDiversoDetalleResponse;
import com.pe.articulos.modules.productos.dto.MovimientoDiversoResponse;
import com.pe.articulos.modules.productos.entity.ClasificacionMovimiento;
import com.pe.articulos.modules.productos.services.InventarioService;
import com.pe.articulos.modules.kardex.dto.KardexDTO;
import com.pe.articulos.modules.kardex.service.KardexService;
import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.modules.aprobaciones.service.SolicitudAnulacionService;
import com.pe.articulos.modules.aprobaciones.entity.SolicitudesAnulacion;
import com.pe.articulos.modules.aprobaciones.dto.SolicitudAnulacionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioMovimientoController {

    private final InventarioService inventarioService;
    private final KardexService kardexService;
    private final SolicitudAnulacionService solicitudAnulacionService;

    @GetMapping("/movimientos")
    public ResponseEntity<ApiResponse<PageResponse<KardexDTO>>> listarMovimientos(
            @RequestParam Long idSucursal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String signo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long idClasificacion,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<KardexDTO> result = kardexService.buscarMovimientosGlobal(
                idSucursal, desde, hasta, signo, nombre, idClasificacion, PageRequest.of(page, size));

        return ResponseEntity.ok(new ApiResponse<>("Lista de movimientos", result, 200, true));
    }

    @GetMapping("/ajuste")
    public ResponseEntity<ApiResponse<PageResponse<AjusteInventarioResponse>>> listarAjustes(
            @RequestParam Integer idSucursal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<AjusteInventarioResponse> result = inventarioService.listarAjustes(idSucursal,
                PageRequest.of(page, size, Sort.by("id").descending()));

        return ResponseEntity.ok(new ApiResponse<>("Lista de ajustes", result, 200, true));
    }

    @PostMapping("/ajuste")
    public ResponseEntity<ApiResponse<Void>> registrarAjuste(@RequestBody AjusteInventarioRequest request) {
        inventarioService.registrarAjusteAgrupado(request);
        return ResponseEntity.ok(new ApiResponse<>("Ajuste de inventario registrado correctamente", null, 200, true));
    }

    @PostMapping("/movimiento-mixto")
    public ResponseEntity<ApiResponse<MovimientoDiversoResponse>> registrarMovimientoMixto(
            @RequestBody MovimientoDiversoBatchRequest request) {
        MovimientoDiversoResponse mov = inventarioService
                .registrarMovimientoMixtoBatch(request);
        return ResponseEntity.ok(new ApiResponse<>("Movimientos registrados correctamente", mov, 200, true));
    }

    @PostMapping("/ingreso-diverso")
    public ResponseEntity<ApiResponse<Void>> ingresoDiverso(
            @RequestParam Long idCatalogo,
            @RequestParam Long idSucursal,
            @RequestParam Long idAlmacen,
            @RequestParam BigDecimal cantidad,
            @RequestParam BigDecimal costo,
            @RequestParam String nroLote,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaVenc,
            @RequestParam String motivo,
            @RequestParam Long idUsuario,
            @RequestParam(required = false) Long idClasificacion) {

        inventarioService.registrarIngresoDiverso(idCatalogo, idSucursal, idAlmacen, cantidad, costo, nroLote,
                fechaVenc,
                motivo, idUsuario, idClasificacion);
        return ResponseEntity.ok(new ApiResponse<>("Ingreso diverso registrado correctamente", null, 200, true));
    }

    @PostMapping("/salida-diversa")
    public ResponseEntity<ApiResponse<Void>> salidaDiversa(
            @RequestParam Long idProducto,
            @RequestParam BigDecimal cantidad,
            @RequestParam String motivo,
            @RequestParam Long idUsuario,
            @RequestParam(required = false) Long idClasificacion) {

        inventarioService.registrarSalidaDiversa(idProducto, cantidad, motivo, idUsuario, idClasificacion);
        return ResponseEntity.ok(new ApiResponse<>("Salida diversa registrada correctamente", null, 200, true));
    }

    @GetMapping("/costo-previo-lote")
    public ResponseEntity<ApiResponse<BigDecimal>> obtenerCostoPrevioLote(
            @RequestParam Long idCatalogo,
            @RequestParam String nroLote) {
        BigDecimal costo = inventarioService.obtenerUltimoCostoLote(idCatalogo, nroLote);
        return ResponseEntity.ok(new ApiResponse<>("Costo previo del lote", costo, 200, true));
    }

    @GetMapping("/ultimo-costo")
    public ResponseEntity<ApiResponse<BigDecimal>> obtenerUltimoCosto(
            @RequestParam Long idCatalogo,
            @RequestParam Long idSucursal) {
        BigDecimal costo = inventarioService.obtenerUltimoCostoCompra(idCatalogo, idSucursal);
        return ResponseEntity.ok(new ApiResponse<>("Último costo de compra", costo, 200, true));
    }

    @GetMapping("/clasificaciones")
    public ResponseEntity<ApiResponse<PageResponse<ClasificacionMovimientoResponse>>> listarClasificaciones(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<ClasificacionMovimientoResponse> result = inventarioService.listarClasificaciones(tipo, q, page,
                size);
        return ResponseEntity.ok(new ApiResponse<>("Lista de clasificaciones", result, 200, true));
    }

    @PostMapping("/clasificaciones")
    public ResponseEntity<ApiResponse<ClasificacionMovimientoResponse>> guardarClasificacion(
            @RequestBody ClasificacionMovimiento clasificacion) {
        ClasificacionMovimientoResponse result = inventarioService.guardarClasificacion(clasificacion);
        return ResponseEntity.ok(new ApiResponse<>("Clasificación creada correctamente", result, 200, true));
    }

    @PutMapping("/clasificaciones/{id}")
    public ResponseEntity<ApiResponse<ClasificacionMovimientoResponse>> actualizarClasificacion(
            @PathVariable Long id,
            @RequestBody ClasificacionMovimiento clasificacion) {
        ClasificacionMovimientoResponse result = inventarioService.actualizarClasificacion(id, clasificacion);
        return ResponseEntity.ok(new ApiResponse<>("Clasificación actualizada correctamente", result, 200, true));
    }

    @DeleteMapping("/clasificaciones/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarClasificacion(
            @PathVariable Long id) {
        inventarioService.eliminarClasificacion(id);
        return ResponseEntity.ok(new ApiResponse<>("Clasificación eliminada correctamente", null, 200, true));
    }

    @GetMapping("/movimientos-diversos")
    public ResponseEntity<ApiResponse<PageResponse<MovimientoDiversoResponse>>> listarMovimientosDiversos(
            @RequestParam(required = false) Long idSucursal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String tipoDocumento,
            @RequestParam(required = false) String serie,
            @RequestParam(required = false) String numero,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        EstadoGeneral estadoEnum = (estado == null || estado.trim().isEmpty() || estado.equalsIgnoreCase("TODOS")) 
                ? null : EstadoGeneral.fromCodigo(estado);

        PageResponse<MovimientoDiversoResponse> result = inventarioService
                .buscarMovimientosDiversos(idSucursal, desde, hasta, estadoEnum, buscar, tipoDocumento, serie, numero,
                        PageRequest.of(page, size, Sort.by("id").descending()));

        return ResponseEntity
                .ok(new ApiResponse<>("Historial de movimientos diversos", result, 200, true));
    }

    @GetMapping("/movimientos-diversos/{id}/detalles")
    public ResponseEntity<ApiResponse<List<MovimientoDiversoDetalleResponse>>> obtenerDetallesDiverso(
            @PathVariable Long id) {

        List<MovimientoDiversoDetalleResponse> detalles = inventarioService
                .obtenerDetallesPorMovimiento(id);

        return ResponseEntity.ok(new ApiResponse<>("Detalles del movimiento diverso", detalles, 200, true));
    }

    @GetMapping("/movimientos-diversos/{id}")
    public ResponseEntity<ApiResponse<MovimientoDiversoResponse>> obtenerMovimientoDiverso(@PathVariable Long id) {
        MovimientoDiversoResponse movimiento = inventarioService.obtenerMovimientoDiversoPorId(id);
        return ResponseEntity.ok(new ApiResponse<>("Movimiento diverso encontrado", movimiento, 200, true));
    }

    @PostMapping("/movimientos-diversos/{id}/solicitar-anulacion")
    public ResponseEntity<ApiResponse<SolicitudAnulacionDTO>> solicitarAnulacion(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {

        String motivo = body.getOrDefault("motivo", "Anulación de movimiento");
        return ResponseEntity.ok(
                solicitudAnulacionService.solicitar(SolicitudesAnulacion.TipoSolicitud.MOVIMIENTO_DIVERSO, id, motivo));
    }
}

package com.pe.articulos.modules.reportes.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.modules.reportes.dto.*;
import com.pe.articulos.modules.reportes.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;
    private final com.pe.articulos.modules.reportes.service.ExcelExportService excelExportService;

    @GetMapping("/resumen")
    public ResponseEntity<ApiResponse<ReporteResumenDTO>> obtenerResumen(@RequestParam String idSucursal, @RequestParam(required = false, defaultValue = "TODOS") String idPuntoVenta) {
        ReporteResumenDTO data = reporteService.obtenerResumen(idSucursal, idPuntoVenta);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/export-rvie")
    public ResponseEntity<StreamingResponseBody> exportRVIE(
            @RequestParam String idSucursal, @RequestParam(required = false, defaultValue = "TODOS") String idPuntoVenta,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {

        java.time.LocalDate inicio = java.time.LocalDate.parse(fechaInicio);
        java.time.LocalDate fin = java.time.LocalDate.parse(fechaFin);

        java.io.ByteArrayInputStream bis = excelExportService.exportRVIE(idSucursal, idPuntoVenta, inicio, fin);

        StreamingResponseBody stream = outputStream -> {
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = bis.read(data, 0, data.length)) != -1) {
                outputStream.write(data, 0, nRead);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=RVIE_" + fechaInicio + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(stream);
    }

    @GetMapping("/export-rce")
    public ResponseEntity<StreamingResponseBody> exportRCE(
            @RequestParam String idSucursal, @RequestParam(required = false, defaultValue = "TODOS") String idPuntoVenta,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {

        java.time.LocalDate inicio = java.time.LocalDate.parse(fechaInicio);
        java.time.LocalDate fin = java.time.LocalDate.parse(fechaFin);

        java.io.ByteArrayInputStream bis = excelExportService.exportRCE(idSucursal, idPuntoVenta, inicio, fin);

        StreamingResponseBody stream = outputStream -> {
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = bis.read(data, 0, data.length)) != -1) {
                outputStream.write(data, 0, nRead);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=RCE_" + fechaInicio + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(stream);
    }

    @GetMapping("/export-cajas")
    public ResponseEntity<StreamingResponseBody> exportarCajas(
            @RequestParam String idSucursal, @RequestParam(required = false, defaultValue = "TODOS") String idPuntoVenta,
            @RequestParam Integer mes,
            @RequestParam Integer anio) {

        java.io.ByteArrayInputStream bis = excelExportService.exportarReporteCajas(idSucursal, idPuntoVenta, mes, anio);

        StreamingResponseBody stream = outputStream -> {
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = bis.read(data, 0, data.length)) != -1) {
                outputStream.write(data, 0, nRead);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ReporteCajas_" + anio + "_" + mes + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(stream);
    }

    @GetMapping("/export-descuentos")
    public ResponseEntity<StreamingResponseBody> exportarDescuentos(
            @RequestParam String idSucursal, @RequestParam(required = false, defaultValue = "TODOS") String idPuntoVenta,
            @RequestParam Integer mes,
            @RequestParam Integer anio) {

        java.io.ByteArrayInputStream bis = excelExportService.exportarReporteDescuentos(idSucursal, idPuntoVenta, mes, anio);

        StreamingResponseBody stream = outputStream -> {
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = bis.read(data, 0, data.length)) != -1) {
                outputStream.write(data, 0, nRead);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ReporteDescuentos_" + anio + "_" + mes + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(stream);
    }

    @GetMapping("/export-anulados")
    public ResponseEntity<StreamingResponseBody> exportarAnulados(
            @RequestParam String idSucursal, @RequestParam(required = false, defaultValue = "TODOS") String idPuntoVenta,
            @RequestParam Integer mes,
            @RequestParam Integer anio) {

        java.io.ByteArrayInputStream bis = excelExportService.exportarAnulados(idSucursal, idPuntoVenta, mes, anio);

        StreamingResponseBody stream = outputStream -> {
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = bis.read(data, 0, data.length)) != -1) {
                outputStream.write(data, 0, nRead);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ReporteAnulados_" + anio + "_" + mes + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(stream);
    }

    @GetMapping("/ventas")
    public ResponseEntity<ApiResponse<ReporteVentaDetalleDTO>> obtenerVentas(
            @RequestParam String idSucursal, @RequestParam(required = false, defaultValue = "TODOS") String idPuntoVenta) {
        ReporteVentaDetalleDTO data = reporteService.obtenerVentasMensuales(idSucursal, idPuntoVenta);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/compras")
    public ResponseEntity<ApiResponse<ReporteCompraDetalleDTO>> obtenerCompras(
            @RequestParam String idSucursal, @RequestParam(required = false, defaultValue = "TODOS") String idPuntoVenta) {
        ReporteCompraDetalleDTO data = reporteService.obtenerComprasMensuales(idSucursal, idPuntoVenta);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/cuentas-pagar")
    public ResponseEntity<ApiResponse<ReporteDeudaDetalleDTO>> obtenerDeudas() {
        ReporteDeudaDetalleDTO data = reporteService.obtenerDeudasPorProveedor();
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/sire-ventas")
    public ResponseEntity<ApiResponse<List<SireVentaDTO>>> obtenerSireVentas(
            @RequestParam String idSucursal, @RequestParam(required = false, defaultValue = "TODOS") String idPuntoVenta,
            @RequestParam String inicio,
            @RequestParam String fin) {
        List<SireVentaDTO> data = reporteService.obtenerSireVentas(idSucursal, idPuntoVenta, inicio, fin);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/sire-compras")
    public ResponseEntity<ApiResponse<List<SireCompraDTO>>> obtenerSireCompras(
            @RequestParam String idSucursal, @RequestParam(required = false, defaultValue = "TODOS") String idPuntoVenta,
            @RequestParam String inicio,
            @RequestParam String fin) {
        List<SireCompraDTO> data = reporteService.obtenerSireCompras(idSucursal, idPuntoVenta, inicio, fin);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/stats-ventas")
    public ResponseEntity<ApiResponse<EstadisticasDTO>> obtenerStatsVentas(
            @RequestParam String idSucursal, @RequestParam(required = false, defaultValue = "TODOS") String idPuntoVenta,
            @RequestParam String inicio,
            @RequestParam String fin) {
        EstadisticasDTO data = reporteService.obtenerEstadisticasVentas(idSucursal, idPuntoVenta, inicio, fin);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/stats-compras")
    public ResponseEntity<ApiResponse<EstadisticasDTO>> obtenerStatsCompras(
            @RequestParam String idSucursal, @RequestParam(required = false, defaultValue = "TODOS") String idPuntoVenta,
            @RequestParam String inicio,
            @RequestParam String fin) {
        EstadisticasDTO data = reporteService.obtenerEstadisticasCompras(idSucursal, idPuntoVenta, inicio, fin);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/productos")
    public ResponseEntity<ApiResponse<List<DataPuntoDTO>>> obtenerTopProductos(
            @RequestParam String idSucursal, @RequestParam(required = false, defaultValue = "TODOS") String idPuntoVenta) {
        List<DataPuntoDTO> data = reporteService.obtenerTopProductos(idSucursal, idPuntoVenta);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/cajas")
    public ResponseEntity<ApiResponse<List<ReporteCajaDetalleDTO>>> obtenerReporteCajas(
            @RequestParam String idSucursal, @RequestParam(required = false, defaultValue = "TODOS") String idPuntoVenta,
            @RequestParam Integer mes,
            @RequestParam Integer anio) {
        List<ReporteCajaDetalleDTO> data = reporteService.obtenerReporteCajas(idSucursal, idPuntoVenta, mes, anio);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/descuento")
    public ResponseEntity<ApiResponse<ReporteDescuentoDetalleDTO>> obtenerReporteDescuentos(
            @RequestParam String idSucursal, @RequestParam(required = false, defaultValue = "TODOS") String idPuntoVenta,
            @RequestParam Integer mes,
            @RequestParam Integer anio) {
        ReporteDescuentoDetalleDTO data = reporteService.obtenerReporteDescuentos(idSucursal, idPuntoVenta, mes, anio);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/comprobantes-anulados")
    public ResponseEntity<ApiResponse<ReporteAnuladoDetalleDTO>> obtenerComprobantesAnulados(
            @RequestParam String idSucursal, @RequestParam(required = false, defaultValue = "TODOS") String idPuntoVenta,
            @RequestParam Integer mes,
            @RequestParam Integer anio) {
        ReporteAnuladoDetalleDTO data = reporteService.obtenerComprobantesAnulados(idSucursal, idPuntoVenta, mes, anio);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/nota-credito")
    public ResponseEntity<ApiResponse<ReporteAnuladoDetalleDTO>> obtenerNotasCredito(
            @RequestParam String idSucursal, @RequestParam(required = false, defaultValue = "TODOS") String idPuntoVenta,
            @RequestParam Integer mes,
            @RequestParam Integer anio) {
        ReporteAnuladoDetalleDTO data = reporteService.obtenerNotasCredito(idSucursal, idPuntoVenta, mes, anio);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping("/column-config")
    public ResponseEntity<ApiResponse<String>> guardarConfiguracionColumnas(
            @RequestParam String reportKey,
            @RequestParam Long idUsuario,
            @RequestParam Long idSucursal,
            @RequestParam String visibleColumns) {
        String data = reporteService.guardarConfiguracionColumnas(reportKey, idUsuario, idSucursal, visibleColumns);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/column-config")
    public ResponseEntity<ApiResponse<String>> obtenerConfiguracionColumnas(
            @RequestParam String reportKey,
            @RequestParam Long idUsuario,
            @RequestParam Long idSucursal) {
        String data = reporteService.obtenerConfiguracionColumnas(reportKey, idUsuario, idSucursal);
        if (data != null) {
            return ResponseEntity.ok(ApiResponse.success(data));
        } else {
            return ResponseEntity.ok(ApiResponse.error("Sin configuración guardada", 404));
        }
    }
}

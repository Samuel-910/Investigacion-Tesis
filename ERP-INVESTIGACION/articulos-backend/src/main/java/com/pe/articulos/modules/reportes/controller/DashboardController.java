package com.pe.articulos.modules.reportes.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.modules.reportes.dto.DashboardSaveRequestDTO;
import com.pe.articulos.modules.reportes.entity.DashboardConfig;
import com.pe.articulos.modules.reportes.entity.DashboardWidget.*;
import com.pe.articulos.modules.reportes.dto.DataPuntoDTO;
import com.pe.articulos.modules.reportes.dto.MultiseriesDataDTO;
import com.pe.articulos.modules.reportes.service.DashboardConfigService;
import com.pe.articulos.modules.reportes.service.DynamicReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pe.articulos.modules.reportes.dto.DashboardOptionsDTO;
import java.util.List;
import java.util.Arrays;

@RestController
@RequestMapping("/api/reportes/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardConfigService configService;
    private final DynamicReportService dynamicReportService;

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<DashboardConfig>> saveConfig(
            @RequestBody DashboardSaveRequestDTO request,
            @RequestParam Long idUsuario,
            @RequestParam Long idSucursal) {
        String categoria = request.getCategoria() != null ? request.getCategoria() : "GENERAL";
        DashboardConfig saved = configService.guardarConfiguracion(request, idUsuario, idSucursal, categoria);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<DashboardConfig>> getCurrentConfig(
            @RequestParam Long idSucursal,
            @RequestParam(defaultValue = "GENERAL") String categoria) {
        DashboardConfig config = configService.obtenerConfiguracionActual(null, idSucursal, categoria);
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    @GetMapping("/data")
    public ResponseEntity<ApiResponse<List<DataPuntoDTO>>> getDynamicData(
            @RequestParam MetricaReporte metrica,
            @RequestParam(required = false) DimensionReporte dimension,
            @RequestParam String idSucursal) {
        List<DataPuntoDTO> data = dynamicReportService.obtenerDatos(metrica, dimension, idSucursal);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/multidata")
    public ResponseEntity<ApiResponse<MultiseriesDataDTO>> getMultiseriesData(
            @RequestParam MetricaReporte metrica,
            @RequestParam(required = false) DimensionReporte dimension,
            @RequestParam String idSucursal) {
        MultiseriesDataDTO data = dynamicReportService.obtenerDatosMultiseries(metrica, dimension, idSucursal);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DashboardConfig>> getById(@PathVariable Long id) {
        DashboardConfig config = configService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    @GetMapping("/options")
    public ResponseEntity<ApiResponse<DashboardOptionsDTO>> getOptions(
            @RequestParam(defaultValue = "GENERAL") String categoria) {
        
        List<MetricaReporte> metricas = Arrays.asList(MetricaReporte.values());
        List<DimensionReporte> dimensiones = Arrays.asList(DimensionReporte.values());

        DashboardOptionsDTO options = DashboardOptionsDTO.builder()
                .metricas(metricas)
                .dimensiones(dimensiones)
                .build();
                
        return ResponseEntity.ok(ApiResponse.success(options));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories(
            @RequestParam Long idSucursal) {
        List<String> categorias = configService.obtenerCategorias(null, idSucursal);
        return ResponseEntity.ok(ApiResponse.success(categorias));
    }
}

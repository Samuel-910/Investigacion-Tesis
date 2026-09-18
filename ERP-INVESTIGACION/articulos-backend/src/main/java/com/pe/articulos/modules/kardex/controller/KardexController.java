package com.pe.articulos.modules.kardex.controller;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.kardex.dto.KardexDTO;
import com.pe.articulos.modules.kardex.service.KardexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/kardex")
@RequiredArgsConstructor
public class KardexController {

    private final KardexService kardexService;
    private final com.pe.articulos.modules.reportes.service.ExcelExportService excelExportService;

    @GetMapping("/catalogo/{idCatalogo}")
    public ResponseEntity<ApiResponse<PageResponse<KardexDTO>>> listarMovimientos(
            @PathVariable Long idCatalogo,
            @RequestParam Long idSucursal,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("REST request para obtener Kardex del catálogo: {}, sucursal: {}", idCatalogo, idSucursal);

        LocalDate fechaDesde = desde != null ? LocalDate.parse(desde) : null;
        LocalDate fechaHasta = hasta != null ? LocalDate.parse(hasta) : null;

        PageResponse<KardexDTO> response;
        if (fechaDesde != null && fechaHasta != null) {
            response = kardexService.listarMovimientosConFiltros(idCatalogo, idSucursal, fechaDesde, fechaHasta,
                    PageRequest.of(page, size));
        } else {
            response = kardexService.listarMovimientos(idCatalogo, idSucursal, PageRequest.of(page, size));
        }
        return ResponseEntity.ok(ApiResponse.<PageResponse<KardexDTO>>builder()
                .message("Kardex obtenido correctamente")
                .data(response)
                .success(true)
                .build());
    }

    @GetMapping("/export/catalogo/{idCatalogo}")
    public StreamingResponseBody exportarExcel(
            @PathVariable Long idCatalogo,
            @RequestParam Long idSucursal,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            HttpServletResponse response) {

        log.info("REST request para exportar Kardex a Excel del catálogo: {}", idCatalogo);

        LocalDate fechaDesde = desde != null ? LocalDate.parse(desde) : null;
        LocalDate fechaHasta = hasta != null ? LocalDate.parse(hasta) : null;

        ByteArrayInputStream bis = excelExportService.exportarKardex(idCatalogo, idSucursal, fechaDesde, fechaHasta);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Kardex_" + idCatalogo + ".xlsx");

        return outputStream -> {
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = bis.read(data, 0, data.length)) != -1) {
                outputStream.write(data, 0, nRead);
            }
        };
    }
}

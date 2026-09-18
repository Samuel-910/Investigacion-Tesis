package com.pe.articulos.modules.venta_registro.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.modules.venta_registro.dto.CrearVentaRequest;
import com.pe.articulos.modules.venta_registro.dto.VentaRegistroDTO;
import com.pe.articulos.core.shared.dto.PageResponse;
import com.pe.articulos.modules.venta_registro.service.VentaService;
import com.pe.articulos.modules.auth.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@SuppressWarnings("null")
public class VentaController {

    private final VentaService ventaService;
    private final JwtService jwtService;
    private final com.pe.articulos.modules.aprobaciones.service.SolicitudAnulacionService solicitudAnulacionService;

    @PostMapping({ "", "/registro" })
    @PreAuthorize("hasAuthority('VENTA_CREAR')")
    public ResponseEntity<ApiResponse<VentaRegistroDTO>> crearVenta(
            @Valid @RequestBody CrearVentaRequest request) {
        try {
            VentaRegistroDTO venta = ventaService.crearVenta(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(venta, "Venta creada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al crear venta: " + e.getMessage(), 400));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VENTA_LEER')")
    public ResponseEntity<ApiResponse<VentaRegistroDTO>> obtenerVentaPorId(@PathVariable Long id) {
        try {
            VentaRegistroDTO venta = ventaService.obtenerVentaPorId(id);
            return ResponseEntity.ok(ApiResponse.success(venta));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VENTA_LEER')")
    public ResponseEntity<ApiResponse<Object>> listarVentas(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false, defaultValue = "false") Boolean all,
            HttpServletRequest request,
            @org.springframework.data.web.PageableDefault(sort = {"fecha", "idVenta"}, direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable) {

        com.pe.articulos.core.enums.EstadoGeneral estEnum = (estado != null && !estado.isEmpty())
                ? com.pe.articulos.core.enums.EstadoGeneral.fromCodigo(estado)
                : null;

        Long puntoId = null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            puntoId = jwtService.extractPuntoId(authHeader.substring(7));
        }

        if (all) {
            List<VentaRegistroDTO> ventas;
            if (estEnum != null) {
                ventas = ventaService.listarVentasPorEstado(estEnum);
            } else {
                ventas = ventaService.listarTodasVentas();
            }
            return ResponseEntity.ok(ApiResponse.success(ventas, "Ventas obtenidas exitosamente"));
        }

        org.springframework.data.domain.Page<VentaRegistroDTO> page;
        if (estEnum != null) {
            page = ventaService.listarVentasPorEstadoPaginado(estEnum, puntoId, pageable);
        } else {
            page = ventaService.listarVentasPaginado(puntoId, pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(PageResponse.fromPage(page), "Ventas obtenidas exitosamente"));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('VENTA_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<VentaRegistroDTO>>> search(
            @RequestParam String q,
            HttpServletRequest request,
            @org.springframework.data.web.PageableDefault(sort = {"fecha", "idVenta"}, direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable) {

        Long puntoId = null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            puntoId = jwtService.extractPuntoId(authHeader.substring(7));
        }

        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.fromPage(ventaService.buscarVentas(q, puntoId, pageable)), "Resultados de búsqueda"));
    }

    @GetMapping("/search-advanced")
    @PreAuthorize("hasAuthority('VENTA_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<VentaRegistroDTO>>> searchAdvanced(
            @RequestParam(required = false) String serie,
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) Integer numeroDesde,
            @RequestParam(required = false) Integer numeroHasta,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaHasta,
            @RequestParam(required = false) String idVendedor,
            @RequestParam(required = false) String condicionPago,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipoDoc,
            @RequestParam(required = false, defaultValue = "false") Boolean porSucursal,
            @RequestParam(required = false) Long idSucursal,
            @RequestParam(required = false) Long idPuntoVenta,
            HttpServletRequest request,
            @org.springframework.data.web.PageableDefault(sort = {"fecha", "idVenta"}, direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable) {

        Long puntoId = null;
        Long sucursalId = null;
        String authHeader = request.getHeader("Authorization");
        
        if (idSucursal != null) {
            if (idSucursal == 0 || idSucursal == -1) {
                // Search globally across all sucursales
                sucursalId = null;
                puntoId = null;
            } else {
                sucursalId = idSucursal;
                puntoId = null;
            }
        } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (Boolean.TRUE.equals(porSucursal)) {
                sucursalId = jwtService.extractSucursalId(token);
            } else {
                puntoId = jwtService.extractPuntoId(token);
            }
        }
        
        if (idPuntoVenta != null) {
            if (idPuntoVenta == 0 || idPuntoVenta == -1) {
                puntoId = null;
            } else {
                puntoId = idPuntoVenta;
            }
        }

        com.pe.articulos.core.enums.EstadoGeneral estEnum = (estado != null)
                ? com.pe.articulos.core.enums.EstadoGeneral.fromCodigo(estado)
                : null;
        return ResponseEntity.ok(ApiResponse.success(PageResponse.fromPage(ventaService.buscarVentasAvanzado(
                serie, numero, numeroDesde, numeroHasta, fechaDesde, fechaHasta, idVendedor, condicionPago, estEnum, puntoId, sucursalId, tipoDoc, pageable)),
                "Resultados de búsqueda avanzada"));
    }

    @GetMapping("/fecha/{fecha}")
    @PreAuthorize("hasAuthority('VENTA_LEER')")
    public ResponseEntity<ApiResponse<List<VentaRegistroDTO>>> listarVentasPorFecha(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha) {
        List<VentaRegistroDTO> ventas = ventaService.listarVentasPorFecha(fecha);
        return ResponseEntity.ok(ApiResponse.success(ventas));
    }

    @GetMapping("/paciente/{idPersonal}")
    @PreAuthorize("hasAuthority('VENTA_LEER')")
    public ResponseEntity<ApiResponse<List<VentaRegistroDTO>>> listarVentasPorPaciente(
            @PathVariable String idPersonal) {
        List<VentaRegistroDTO> ventas = ventaService.listarVentasPorPaciente(idPersonal);
        return ResponseEntity.ok(ApiResponse.success(ventas));
    }

    @GetMapping("/rango")
    @PreAuthorize("hasAuthority('VENTA_LEER')")
    public ResponseEntity<ApiResponse<List<VentaRegistroDTO>>> listarVentasPorRango(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin) {
        List<VentaRegistroDTO> ventas = ventaService.listarVentasPorRangoFechas(fechaInicio, fechaFin);
        return ResponseEntity.ok(ApiResponse.success(ventas));
    }

    @PutMapping("/{id}/anular")
    @PreAuthorize("hasAuthority('VENTA_ANULAR')")
    public ResponseEntity<ApiResponse<com.pe.articulos.modules.aprobaciones.dto.SolicitudAnulacionDTO>> anularVenta(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo) {
        try {
            ApiResponse<com.pe.articulos.modules.aprobaciones.dto.SolicitudAnulacionDTO> response = solicitudAnulacionService
                    .solicitar(com.pe.articulos.modules.aprobaciones.entity.SolicitudesAnulacion.TipoSolicitud.VENTA,
                            id,
                            motivo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al solicitar anulación: " + e.getMessage(), 400));
        }
    }

    @PostMapping("/{id}/devolver")
    @PreAuthorize("hasAuthority('VENTA_DEVOLVER')")
    public ResponseEntity<ApiResponse<VentaRegistroDTO>> realizarDevolucion(
            @PathVariable Long id,
            @RequestBody com.pe.articulos.modules.venta_registro.dto.DevolucionRequest request) {
        try {
            request.setIdVenta(id);
            VentaRegistroDTO venta = ventaService.realizarDevolucion(request);
            return ResponseEntity.ok(ApiResponse.success(venta, "Devolución procesada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al procesar devolución: " + e.getMessage(), 400));
        }
    }

    @GetMapping("/documento/{numdoc}")
    @PreAuthorize("hasAuthority('VENTA_LEER')")
    public ResponseEntity<ApiResponse<VentaRegistroDTO>> obtenerVentaPorNumdoc(@PathVariable String numdoc) {
        try {
            VentaRegistroDTO venta = ventaService.obtenerVentaPorNumdoc(numdoc);
            return ResponseEntity.ok(ApiResponse.success(venta));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/correlatividad")
    @PreAuthorize("hasAuthority('VENTA_LEER')")
    public ResponseEntity<ApiResponse<List<com.pe.articulos.modules.venta_registro.dto.CorrelatividadDTO>>> obtenerCorrelatividad(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin,
            @RequestParam(required = false) Long idSucursal,
            @RequestParam(required = false) Long puntoId,
            @RequestParam(required = false) String tipoDoc) {
        try {
            List<com.pe.articulos.modules.venta_registro.dto.CorrelatividadDTO> correlatividad = ventaService
                    .obtenerCorrelatividad(fechaInicio, fechaFin, idSucursal, puntoId, tipoDoc);
            return ResponseEntity
                    .ok(ApiResponse.success(correlatividad, "Correlatividad obtenida exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al obtener correlatividad: " + e.getMessage(), 400));
        }
    }

    @GetMapping("/correlatividad/detalle")
    @PreAuthorize("hasAuthority('VENTA_LEER')")
    public ResponseEntity<ApiResponse<List<VentaRegistroDTO>>> obtenerDetalleCorrelatividad(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin,
            @RequestParam(required = false) Long idSucursal,
            @RequestParam(required = false) Long puntoId,
            @RequestParam String tipoDoc,
            @RequestParam String serie) {
        try {
            List<VentaRegistroDTO> detalle = ventaService
                    .obtenerDetalleCorrelatividad(fechaInicio, fechaFin, idSucursal, puntoId, tipoDoc, serie);
            return ResponseEntity
                    .ok(ApiResponse.success(detalle, "Detalle de correlatividad obtenido exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al obtener detalle de correlatividad: " + e.getMessage(), 400));
        }
    }

    @GetMapping("/export/correlatividad")
    @PreAuthorize("hasAuthority('VENTA_LEER')")
    public ResponseEntity<org.springframework.core.io.Resource> exportarCorrelatividad(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin,
            @RequestParam(required = false) Long idSucursal,
            @RequestParam(required = false) Long puntoId,
            @RequestParam(required = false) String tipoDoc) {
        try {
            java.io.ByteArrayInputStream in = ventaService.exportarCorrelatividad(fechaInicio, fechaFin, idSucursal, puntoId,
                    tipoDoc);
            InputStreamResource resource = new InputStreamResource(in);

            String descTipo = "";
            if (tipoDoc != null && !tipoDoc.isEmpty()) {
                descTipo = "_" + switch (tipoDoc) {
                    case "01" -> "FACTURAS";
                    case "03" -> "BOLETAS";
                    case "07" -> "NOTAS_CREDITO";
                    case "08" -> "NOTAS_DEBITO";
                    case "VENTA" -> "VENTAS_GRAL";
                    case "ID", "INGRESO_DIVERSO" -> "INGRESOS_DIVERSOS";
                    case "SD", "SALIDA_DIVERSA" -> "SALIDAS_DIVERSAS";
                    default -> tipoDoc;
                };
            }
            String periodoStr = fechaInicio.getYear() + "_" + String.format("%02d", fechaInicio.getMonthValue());
            String fileName = "correlatividad_" + periodoStr + descTipo + ".xlsx";

            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=" + fileName)
                    .contentType(org.springframework.http.MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/xml")
    @PreAuthorize("hasAuthority('VENTA_LEER')")
    public ResponseEntity<byte[]> descargarXml(@PathVariable Long id) {
        try {
            VentaRegistroDTO venta = ventaService.obtenerVentaPorId(id);
            byte[] xml = ventaService.obtenerXmlVenta(id);
            String fileName = "20123456789-" + venta.getTipoDoc() + "-" + venta.getSerie() + "-" + venta.getNumero()
                    + ".xml";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.APPLICATION_XML)
                    .body(xml);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/html")
    @PreAuthorize("hasAuthority('VENTA_LEER')")
    public ResponseEntity<byte[]> descargarHtml(@PathVariable Long id) {
        try {
            VentaRegistroDTO venta = ventaService.obtenerVentaPorId(id);
            String html = ventaService.obtenerHtmlVenta(id);
            String fileName = "20123456789-" + venta.getTipoDoc() + "-" + venta.getSerie() + "-" + venta.getNumero()
                    + ".html";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.TEXT_HTML)
                    .body(html.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAuthority('VENTA_LEER')")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long id, @RequestParam Long idPlantilla) {
        try {
            VentaRegistroDTO venta = ventaService.obtenerVentaPorId(id);
            byte[] pdf = ventaService.obtenerPdfVenta(id, idPlantilla);
            String fileName = "20123456789-" + venta.getTipoDoc() + "-" + venta.getSerie() + "-" + venta.getNumero()
                    + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/enviar-email")
    @PreAuthorize("hasAuthority('VENTA_LEER')")
    public ResponseEntity<ApiResponse<Void>> enviarEmail(
            @PathVariable Long id,
            @RequestParam String email,
            @RequestParam Long idPlantilla) {
        try {
            ventaService.enviarPorEmail(id, email, idPlantilla);
            return ResponseEntity.ok(ApiResponse.success(null, "Email enviado exitosamente a " + email));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al enviar email: " + e.getMessage(), 400));
        }
    }

    @PostMapping("/{id}/enviar-whatsapp")
    @PreAuthorize("hasAuthority('VENTA_LEER')")
    public ResponseEntity<ApiResponse<Void>> enviarWhatsApp(
            @PathVariable Long id,
            @RequestParam String telefono,
            @RequestParam Long idPlantilla) {
        try {
            ventaService.enviarPorWhatsApp(id, telefono, idPlantilla);
            return ResponseEntity
                    .ok(ApiResponse.success(null, "Mensaje de WhatsApp enviado exitosamente a " + telefono));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al enviar WhatsApp: " + e.getMessage(), 400));
        }
    }

    @PostMapping("/{id}/reimprimir")
    @PreAuthorize("hasAuthority('VENTA_LEER')")
    public ResponseEntity<ApiResponse<VentaRegistroDTO>> reimprimir(
            @PathVariable Long id,
            HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "Reimpresión solicitada por usuario") String motivo) {
        try {
            String ip = request.getRemoteAddr();
            VentaRegistroDTO venta = ventaService.reimprimir(id, ip, motivo);
            return ResponseEntity.ok(ApiResponse.success(venta, "Auditoría de reimpresión registrada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al registrar reimpresión: " + e.getMessage(), 400));
        }
    }

    @GetMapping("/{id}/reimpresiones")
    @PreAuthorize("hasAuthority('VENTA_LEER')")
    public ResponseEntity<ApiResponse<List<com.pe.articulos.modules.venta_registro.dto.VentaReimpresionLogDTO>>> obtenerHistorialReimpresiones(
            @PathVariable Long id) {
        try {
            List<com.pe.articulos.modules.venta_registro.dto.VentaReimpresionLogDTO> historial = ventaService.obtenerHistorialReimpresiones(id);
            return ResponseEntity.ok(ApiResponse.success(historial, "Historial de reimpresiones obtenido exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al obtener el historial de reimpresiones: " + e.getMessage(), 400));
        }
    }
}

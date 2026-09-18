package com.pe.articulos.modules.reportes.service.impl;

import com.pe.articulos.modules.clinica.entity.Clinica;
import com.pe.articulos.modules.clinica.repository.ClinicaRepository;
import com.pe.articulos.modules.compras.entity.Compra;
import com.pe.articulos.modules.compras.repository.CompraRepository;
import com.pe.articulos.modules.reportes.service.ExcelExportService;
import com.pe.articulos.modules.venta_registro.entity.VentaRegistro;
import com.pe.articulos.modules.venta_registro.repository.VentaRegistroRepository;
import com.pe.articulos.modules.sucursal.entity.Sucursal;
import com.pe.articulos.modules.sucursal.repository.SucursalRepository;
import com.pe.articulos.modules.catalogo.repository.CatalogoRepository;
import com.pe.articulos.modules.kardex.entity.Kardex;
import com.pe.articulos.modules.kardex.repository.KardexRepository;
import com.pe.articulos.modules.productos.entity.Producto;
import com.pe.articulos.modules.productos.repository.ProductoRepository;
import com.pe.articulos.modules.caja_chica.entity.CajaChica;
import com.pe.articulos.modules.caja_chica.repository.CajaChicaRepository;
import com.pe.articulos.modules.venta_registro.entity.VentaDetalle;
import com.pe.articulos.modules.venta_registro.repository.VentaDetalleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ExcelExportServiceImpl implements ExcelExportService {

    private final VentaRegistroRepository ventaRepository;
    private final CompraRepository compraRepository;
    private final ClinicaRepository clinicaRepository;
    private final KardexRepository kardexRepository;
    private final CatalogoRepository catalogoRepository;
    private final SucursalRepository sucursalRepository;
    private final ProductoRepository productoRepository;
    private final CajaChicaRepository cajaChicaRepository;
    private final VentaDetalleRepository ventaDetalleRepository;

    @Override
    public ByteArrayInputStream exportRVIE(String idSucursal, String idPuntoVenta, LocalDate fechaInicio, LocalDate fechaFin) {
        List<VentaRegistro> ventas = ventaRepository.findByFechaBetween(fechaInicio, fechaFin);
        if (idSucursal != null && !idSucursal.isEmpty()) {
            try {
                Long sucursalId = Long.parseLong(idSucursal);
                ventas = ventas.stream()
                        .filter(v -> sucursalId.equals(v.getIdSucursal()))
                        .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                ventas = java.util.Collections.emptyList();
            }
        }

        Clinica clinica = clinicaRepository.findAll().stream().findFirst().orElse(null);
        String rucEmpresa = clinica != null ? clinica.getRuc() : "-";
        String nombreEmpresa = clinica != null ? clinica.getRazonSocial() : "-";

        LogoInfo logoInfo = getLogoInfo(clinica);
        byte[] logoBytes = logoInfo != null ? logoInfo.getBytes() : null;

        java.util.concurrent.atomic.AtomicInteger index = new java.util.concurrent.atomic.AtomicInteger(1);
        List<Map<String, Object>> items = ventas.stream().flatMap(v -> {
            boolean isAnulado = com.pe.articulos.core.enums.EstadoGeneral.ANULADO.equals(v.getEstado());
            Map<String, Object> row1 = mapVentaToMap(v, index, false, rucEmpresa, nombreEmpresa);
            if (isAnulado) {
                Map<String, Object> row2 = mapVentaToMap(v, index, true, rucEmpresa, nombreEmpresa);
                return java.util.stream.Stream.of(row1, row2);
            }
            return java.util.stream.Stream.of(row1);
        }).collect(Collectors.toList());

        Map<String, Object> model = new HashMap<>();
        model.put("items", items);
        model.put("empresa", nombreEmpresa);
        model.put("ruc", rucEmpresa);
        model.put("sucursal", idSucursal);
        model.put("periodoTexto", fechaInicio.format(DateTimeFormatter.ofPattern("MMMM yyyy")).toUpperCase());
        model.put("fechaHoy", LocalDate.now().toString());
        model.put("titulo", "REGISTRO DE VENTAS E INGRESOS ELECTRÓNICO - RVIE");
        model.put("logo", logoBytes);

        return generateExcel("templates/excel/rvie_template.xlsx", model);
    }

    private Map<String, Object> mapVentaToMap(VentaRegistro v, java.util.concurrent.atomic.AtomicInteger index,
            boolean isNeg, String rucEmpresa, String nombreEmpresa) {
        Map<String, Object> item = new HashMap<>();
        BigDecimal multiplier = isNeg ? new BigDecimal("-1") : BigDecimal.ONE;

        item.put("index", index.getAndIncrement());
        item.put("periodo", v.getFecha().format(DateTimeFormatter.ofPattern("yyyyMM00")));
        item.put("car", "");
        item.put("fechaEmision", v.getFecha().toString());
        item.put("tipoCP", v.getTipoDoc());
        item.put("serie", v.getSerie());
        item.put("numero", v.getNumdoc());
        item.put("tipoDocCli", "");
        item.put("nroDocCli", v.getNroDni() != null ? v.getNroDni() : "");
        item.put("razonSocial", v.getNombrePac() != null ? v.getNombrePac() : "");

        item.put("baseImp", v.getBaseImp().multiply(multiplier));
        item.put("igv", v.getIgv().multiply(multiplier));
        item.put("exonerado", v.getValorExo().multiply(multiplier));
        item.put("inafecto", v.getValorInaf().multiply(multiplier));
        item.put("biIvap", (v.getBiIvap() != null ? v.getBiIvap() : BigDecimal.ZERO).multiply(multiplier));
        item.put("ivap", (v.getIvap() != null ? v.getIvap() : BigDecimal.ZERO).multiply(multiplier));
        item.put("total", v.getTotal().multiply(multiplier));

        if (isNeg) {
            item.put("fechaRef",
                    v.getFechaAnul() != null ? v.getFechaAnul().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
            item.put("tipoRef", v.getTipoDoc());
            item.put("serieRef", v.getSerie());
            item.put("nroRef", v.getNumdoc());
        } else {
            item.put("fechaRef", "");
            item.put("tipoRef", "");
            item.put("serieRef", "");
            item.put("nroRef", "");
        }

        item.put("rucEmpresa", rucEmpresa);
        item.put("nombreEmpresa", nombreEmpresa);
        item.put("estado", isNeg ? "ANULADO" : (v.getEstado() != null ? v.getEstado().name() : "ACTIVO"));
        return item;
    }

    @Override
    public ByteArrayInputStream exportRCE(String idSucursal, String idPuntoVenta, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Compra> compras = compraRepository.findAll().stream()
                .filter(c -> !c.getFechaEmision().isBefore(fechaInicio) && !c.getFechaEmision().isAfter(fechaFin))
                .collect(Collectors.toList());

        if (idSucursal != null && !idSucursal.isEmpty()) {
            Long sucursalId = Long.parseLong(idSucursal);
            compras = compras.stream()
                    .filter(c -> c.getIdSucursal().equals(sucursalId))
                    .collect(Collectors.toList());
        }

        Clinica clinica = clinicaRepository.findAll().stream().findFirst().orElse(null);
        String rucEmpresa = clinica != null ? clinica.getRuc() : "-";
        String nombreEmpresa = clinica != null ? clinica.getRazonSocial() : "-";

        LogoInfo logoInfo = getLogoInfo(clinica);
        byte[] logoBytes = logoInfo != null ? logoInfo.getBytes() : null;

        java.util.concurrent.atomic.AtomicInteger index = new java.util.concurrent.atomic.AtomicInteger(1);
        List<Map<String, Object>> items = compras.stream().flatMap(c -> {
            boolean isAnulado = com.pe.articulos.core.enums.EstadoGeneral.ANULADO.equals(c.getEstado());
            Map<String, Object> row1 = mapCompraToMap(c, index, false, rucEmpresa, nombreEmpresa);
            if (isAnulado) {
                Map<String, Object> row2 = mapCompraToMap(c, index, true, rucEmpresa, nombreEmpresa);
                return java.util.stream.Stream.of(row1, row2);
            }
            return java.util.stream.Stream.of(row1);
        }).collect(Collectors.toList());

        Map<String, Object> model = new HashMap<>();
        model.put("items", items);
        model.put("empresa", nombreEmpresa);
        model.put("ruc", rucEmpresa);
        model.put("fechaHoy", LocalDate.now().toString());
        model.put("titulo", "REGISTRO DE COMPRAS ELECTRÓNICO - RCE");
        model.put("logo", logoBytes);

        return generateExcel("templates/excel/rce_template.xlsx", model);
    }

    private Map<String, Object> mapCompraToMap(Compra c, java.util.concurrent.atomic.AtomicInteger index, boolean isNeg,
            String rucEmpresa, String nombreEmpresa) {
        Map<String, Object> item = new HashMap<>();
        BigDecimal multiplier = isNeg ? new BigDecimal("-1") : BigDecimal.ONE;

        item.put("index", index.getAndIncrement());
        item.put("periodo", c.getFechaEmision().format(DateTimeFormatter.ofPattern("yyyyMM00")));
        item.put("fechaEmision", c.getFechaEmision().toString());

        String tipoCP = "01";
        if ("BOLETA".equalsIgnoreCase(c.getTipoComprobante()))
            tipoCP = "03";
        else if (c.getTipoComprobante() != null && c.getTipoComprobante().toUpperCase().contains("GUIA"))
            tipoCP = "09";

        item.put("tipoCP", tipoCP);
        item.put("serie", c.getSerie());
        item.put("numero", c.getCorrelativo());
        item.put("nroDocCli", c.getProveedor().getNumDocIdent());
        item.put("razonSocial", c.getProveedor().getRazonSocial());

        item.put("baseImp", c.getValorVentaGravado().multiply(multiplier));
        item.put("igv", c.getIgv().multiply(multiplier));
        item.put("exonerado", c.getValorVentaExonerado().multiply(multiplier));
        item.put("inafecto", c.getValorVentaInafecto().multiply(multiplier));
        item.put("total", c.getTotalPagar().multiply(multiplier));

        if (isNeg) {
            item.put("fechaRef",
                    c.getFechaAnulacion() != null
                            ? c.getFechaAnulacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            : "");
            item.put("tipoRef", tipoCP);
            item.put("serieRef", c.getSerie());
            item.put("nroRef", c.getCorrelativo());
        } else {
            item.put("fechaRef", "");
            item.put("tipoRef", "");
            item.put("serieRef", "");
            item.put("nroRef", "");
        }

        item.put("rucEmpresa", rucEmpresa);
        item.put("nombreEmpresa", nombreEmpresa);
        item.put("estado", isNeg ? "ANULADO" : (c.getEstado() != null ? c.getEstado().name() : "REGISTRADO"));
        return item;
    }

    @Override
    public ByteArrayInputStream exportarKardex(Long idCatalogo, Long idSucursal, LocalDate desde, LocalDate hasta) {
        List<Kardex> movimientos;
        if (desde != null && hasta != null) {
            java.time.LocalDateTime start = desde.atStartOfDay();
            java.time.LocalDateTime end = hasta.atTime(23, 59, 59);
            movimientos = kardexRepository
                    .findByIdCatalogoAndIdSucursalAndFechaBetweenOrderByFechaDescIdArticuloKardexDesc(idCatalogo,
                            idSucursal, start, end);
        } else {
            movimientos = kardexRepository.findByIdCatalogoAndIdSucursalOrderByFechaDescIdArticuloKardexDesc(idCatalogo,
                    idSucursal);
        }

        Clinica clinica = clinicaRepository.findAll().stream().findFirst().orElse(null);
        String nombreEmpresa = clinica != null ? clinica.getRazonSocial() : "-";
        String rucEmpresa = clinica != null ? clinica.getRuc() : "-";

        Sucursal sucursal = sucursalRepository.findById(idSucursal).orElse(null);
        String nombreSucursal = sucursal != null ? sucursal.getNombreSucursal() : "Sucursal " + idSucursal;
        String direccionSucursal = sucursal != null ? sucursal.getDireccion() : "-";

        LogoInfo logoInfo = getLogoInfo(clinica);

        String nombreProducto = catalogoRepository.findById(idCatalogo)
                .map(p -> p.getNombre())
                .orElse("Producto " + idCatalogo);

        // Obtener datos adicionales del producto
        Producto producto = productoRepository
                .findFirstByIdCatalogoAndIdSucursalOrderByFechaRegDesc(idCatalogo, idSucursal)
                .orElse(null);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Kardex");
            sheet.setColumnWidth(0, 4000); // Ancho para el logo (Col A)
            sheet.setColumnWidth(1, 4000); // Ancho para el logo (Col B)

            // Estilos
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);

            CellStyle blueGroup = workbook.createCellStyle();
            blueGroup.cloneStyleFrom(headerStyle);
            blueGroup.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());

            CellStyle redGroup = workbook.createCellStyle();
            redGroup.cloneStyleFrom(headerStyle);
            redGroup.setFillForegroundColor(IndexedColors.RED.getIndex());

            CellStyle greenGroup = workbook.createCellStyle();
            greenGroup.cloneStyleFrom(headerStyle);
            greenGroup.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);

            CellStyle amountStyle = workbook.createCellStyle();
            amountStyle.cloneStyleFrom(dataStyle);
            amountStyle.setDataFormat(workbook.createDataFormat().getFormat("\"S/ \" #,##0.00"));
            amountStyle.setAlignment(HorizontalAlignment.RIGHT);

            // --- CABECERA PROFESIONAL ---
            // Insertar Logo (Filas 0-4, Columna A)
            if (logoInfo != null && logoInfo.getBytes() != null) {
                log.info("Insertando logo en Kardex. Tamaño: {} bytes, Tipo: {}", logoInfo.getBytes().length,
                        logoInfo.getWorkbookType());

                CreationHelper helper = workbook.getCreationHelper();
                ClientAnchor anchor = helper.createClientAnchor();
                anchor.setCol1(0); // Inicio Columna A
                anchor.setRow1(0); // Inicio Fila 0
                anchor.setCol2(2); // Fin Columna C (exclusivo)
                anchor.setRow2(4); // Fin Fila 4 (exclusivo)
                anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);

                Row rowAviso = sheet.getRow(0);
                if (rowAviso == null)
                    rowAviso = sheet.createRow(0);
                Cell cellAviso = rowAviso.createCell(0);
                cellAviso.setCellValue("LOGO OK (" + logoInfo.getBytes().length + ")");
            }

            // Datos de la Empresa (Columna B adelante)
            CellStyle companyNameStyle = workbook.createCellStyle();
            Font companyFont = workbook.createFont();
            companyFont.setBold(true);
            companyFont.setFontHeightInPoints((short) 14);
            companyNameStyle.setFont(companyFont);

            Row r0 = sheet.getRow(0);
            if (r0 == null)
                r0 = sheet.createRow(0);
            Cell cClinic = r0.createCell(2);
            cClinic.setCellValue(nombreEmpresa);
            cClinic.setCellStyle(companyNameStyle);

            Row r1 = sheet.getRow(1);
            if (r1 == null)
                r1 = sheet.createRow(1);
            r1.createCell(2).setCellValue("RUC: " + rucEmpresa);

            Row r2 = sheet.getRow(2);
            if (r2 == null)
                r2 = sheet.createRow(2);
            r2.createCell(2).setCellValue(direccionSucursal + " - " + nombreSucursal);

            // Título del Reporte
            Row r4 = sheet.getRow(4);
            if (r4 == null)
                r4 = sheet.createRow(4);
            Cell titleCell = r4.createCell(0);
            titleCell.setCellValue("REPORTE KARDEX VALORADO");
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setUnderline(Font.U_SINGLE);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(4, 4, 0, 15));

            // Información del Producto y Fechas
            Row r6 = sheet.createRow(6);
            r6.createCell(0).setCellValue("PRODUCTO:");
            Cell pCell = r6.createCell(1);
            pCell.setCellValue(nombreProducto);
            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);
            pCell.setCellStyle(boldStyle);

            r6.createCell(4).setCellValue("LABORATORIO:");
            r6.createCell(5)
                    .setCellValue(producto != null && producto.getLaboratorio() != null
                            ? producto.getLaboratorio().getDescripcion()
                            : "-");

            r6.createCell(8).setCellValue("ALMACÉN:");
            r6.createCell(9).setCellValue(
                    producto != null && producto.getAlmacen() != null ? producto.getAlmacen().getNombre() : "-");

            r6.createCell(13).setCellValue("FECHA GEN.:");
            r6.createCell(14).setCellValue(LocalDate.now().toString());

            Row r7 = sheet.getRow(7);
            if (r7 == null)
                r7 = sheet.createRow(7);
            r7.createCell(0).setCellValue("CÓD. BARRAS:");
            r7.createCell(1).setCellValue(producto != null ? producto.getCodigoBarra() : "-");

            r7.createCell(4).setCellValue("CÓD. DIGEMID:");
            r7.createCell(5).setCellValue(producto != null ? producto.getCodDigemid() : "-");

            r7.createCell(8).setCellValue("PRESENTACIÓN:");
            r7.createCell(9).setCellValue(producto != null ? producto.getPresentacion() : "-");

            r7.createCell(13).setCellValue("PERIODO:");
            String periodoStr = (desde != null && hasta != null)
                    ? desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " al "
                            + hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : "Todo el histórico";
            Cell periodoCell = r7.createCell(14);
            periodoCell.setCellValue(periodoStr);
            periodoCell.setCellStyle(boldStyle);

            // --- TABLA DE MOVIMIENTOS ---
            int startRow = 9;
            Row groupRow = sheet.createRow(startRow);
            String[] firstLevel = { "#", "OPERACIÓN", "USUARIO", "FECHA", "DOCUMENTO", "LOTE", "DETALLE", "ENTRADAS",
                    "", "", "SALIDAS", "", "", "SALDO FINAL", "", "" };
            for (int i = 0; i < firstLevel.length; i++) {
                Cell cell = groupRow.createCell(i);
                cell.setCellValue(firstLevel[i]);
                if (i >= 7 && i <= 9)
                    cell.setCellStyle(blueGroup);
                else if (i >= 10 && i <= 12)
                    cell.setCellStyle(redGroup);
                else if (i >= 13 && i <= 15)
                    cell.setCellStyle(greenGroup);
                else
                    cell.setCellStyle(headerStyle);
            }

            // Combinar celdas de grupos
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(startRow, startRow + 1, 0, 0)); // #
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(startRow, startRow + 1, 1, 1)); // OPERACION
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(startRow, startRow + 1, 2, 2)); // USUARIO
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(startRow, startRow + 1, 3, 3)); // FECHA
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(startRow, startRow + 1, 4, 4)); // DOCUMENTO
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(startRow, startRow + 1, 5, 5)); // LOTE
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(startRow, startRow + 1, 6, 6)); // DETALLE
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(startRow, startRow, 7, 9)); // ENTRADAS
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(startRow, startRow, 10, 12)); // SALIDAS
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(startRow, startRow, 13, 15)); // SALDO
                                                                                                            // FINAL

            Row subHeaderRow = sheet.createRow(startRow + 1);
            String[] subHeaders = { "", "", "", "", "", "", "", "CANT.", "COSTO UNIT.", "COSTO TOTAL", "CANT.",
                    "COSTO UNIT.", "COSTO TOTAL", "CANT.", "PMP", "COSTO TOTAL" };
            for (int i = 7; i < subHeaders.length; i++) {
                Cell cell = subHeaderRow.createCell(i);
                cell.setCellValue(subHeaders[i]);
                if (i >= 7 && i <= 9)
                    cell.setCellStyle(blueGroup);
                else if (i >= 10 && i <= 12)
                    cell.setCellStyle(redGroup);
                else if (i >= 13 && i <= 15)
                    cell.setCellStyle(greenGroup);
            }

            // Datos
            int rowIdx = startRow + 2;
            int counter = 1;
            for (Kardex k : movimientos) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(counter++);
                row.createCell(1).setCellValue(k.getOperacion());
                row.createCell(2).setCellValue(k.getUsuario() != null ? k.getUsuario().getNombreCompleto() : "-");
                row.createCell(3).setCellValue(k.getFecha().toString());
                row.createCell(4).setCellValue(k.getNumDoc());
                row.createCell(5).setCellValue(k.getNroLote() != null ? k.getNroLote() : "-");
                row.createCell(6).setCellValue(k.getDetalle());

                boolean isEntrada = "+".equals(k.getSigno());

                row.createCell(7).setCellValue(isEntrada ? k.getCantidad().doubleValue() : 0);
                row.createCell(8).setCellValue(isEntrada ? k.getCostoUnitario().doubleValue() : 0);
                row.createCell(9).setCellValue(isEntrada ? k.getCostoTotal().doubleValue() : 0);

                row.createCell(10).setCellValue(!isEntrada ? k.getCantidad().doubleValue() : 0);
                row.createCell(11).setCellValue(!isEntrada ? k.getCostoUnitario().doubleValue() : 0);
                row.createCell(12).setCellValue(!isEntrada ? k.getCostoTotal().doubleValue() : 0);

                row.createCell(13).setCellValue(k.getSaldoCantidad().doubleValue());
                row.createCell(14).setCellValue(k.getSaldoCostoUnitario().doubleValue());
                row.createCell(15).setCellValue(k.getSaldoCostoTotal().doubleValue());

                for (int i = 0; i <= 6; i++)
                    row.getCell(i).setCellStyle(dataStyle);
                for (int i = 7; i <= 15; i++)
                    row.getCell(i).setCellStyle(amountStyle);
            }

            for (int i = 0; i < subHeaders.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            log.error("Error al generar Excel de Kardex", e);
            throw new RuntimeException("Error al generar Excel de Kardex", e);
        }
    }

    @Override
    public ByteArrayInputStream exportarCorrelatividad(LocalDate fechaInicio, LocalDate fechaFin, String tipoDoc,
            List<com.pe.articulos.modules.venta_registro.dto.CorrelatividadDTO> datos) {
        Clinica clinica = clinicaRepository.findAll().stream().findFirst().orElse(null);
        String nombreEmpresa = clinica != null ? clinica.getRazonSocial() : "-";
        String rucEmpresa = clinica != null ? clinica.getRuc() : "-";

        LogoInfo logoInfo = getLogoInfo(clinica);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Correlatividad");
            sheet.setColumnWidth(0, 4000);
            sheet.setColumnWidth(1, 4000);

            // --- ESTILOS ---
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);

            CellStyle amountStyle = workbook.createCellStyle();
            amountStyle.setDataFormat(workbook.createDataFormat().getFormat("\"S/ \" #,##0.00"));
            amountStyle.setAlignment(HorizontalAlignment.RIGHT);
            amountStyle.setBorderBottom(BorderStyle.THIN);
            amountStyle.setBorderLeft(BorderStyle.THIN);
            amountStyle.setBorderRight(BorderStyle.THIN);
            amountStyle.setBorderTop(BorderStyle.THIN);

            CellStyle totalRowStyle = workbook.createCellStyle();
            totalRowStyle.cloneStyleFrom(headerStyle);
            totalRowStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);

            // --- CABECERA PROFESIONAL ---
            if (logoInfo != null && logoInfo.getBytes() != null) {
                int pictureIdx = workbook.addPicture(logoInfo.getBytes(), logoInfo.getWorkbookType());
                CreationHelper helper = workbook.getCreationHelper();
                Drawing<?> drawing = sheet.createDrawingPatriarch();
                ClientAnchor anchor = helper.createClientAnchor();
                anchor.setCol1(0);
                anchor.setRow1(0);
                Picture pict = drawing.createPicture(anchor, pictureIdx);
                pict.resize(1.8, 4.0);
            }

            CellStyle companyNameStyle = workbook.createCellStyle();
            Font companyFont = workbook.createFont();
            companyFont.setBold(true);
            companyFont.setFontHeightInPoints((short) 14);
            companyNameStyle.setFont(companyFont);

            Row r0 = sheet.getRow(0);
            if (r0 == null)
                r0 = sheet.createRow(0);
            Cell cClinic = r0.createCell(2);
            cClinic.setCellValue(nombreEmpresa);
            cClinic.setCellStyle(companyNameStyle);

            Row r1 = sheet.getRow(1);
            if (r1 == null)
                r1 = sheet.createRow(1);
            r1.createCell(2).setCellValue("RUC: " + rucEmpresa);

            Row r4 = sheet.createRow(4);
            Cell titleCell = r4.createCell(0);
            titleCell.setCellValue("REPORTE DE CORRELATIVIDAD DE DOCUMENTOS");
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setUnderline(Font.U_SINGLE);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(4, 4, 0, 10));

            Row r6 = sheet.getRow(6);
            if (r6 == null)
                r6 = sheet.createRow(6);
            r6.createCell(0).setCellValue("PERIODO:");
            String periodoStr = (fechaInicio != null && fechaFin != null)
                    ? fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " al "
                            + fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : "Todo el histórico";
            Cell periodoCell = r6.createCell(1);
            periodoCell.setCellValue(periodoStr);
            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);
            periodoCell.setCellStyle(boldStyle);

            r6.createCell(9).setCellValue("FECHA GEN.:");
            r6.createCell(10).setCellValue(LocalDate.now().toString());

            if (tipoDoc != null && !tipoDoc.isEmpty()) {
                Row r7 = sheet.createRow(7);
                r7.createCell(0).setCellValue("DOCUMENTO:");
                String descFiltro = "OTROS";
                descFiltro = switch (tipoDoc) {
                    case "01" -> "FACTURA";
                    case "03" -> "BOLETA";
                    case "07" -> "NOTA DE CREDITO";
                    case "08" -> "NOTA DE DEBITO";
                    case "ID" -> "INGRESO DIVERSO";
                    case "SD" -> "SALIDA DIVERSA";
                    default -> "OTROS";
                };
                Cell descCell = r7.createCell(1);
                descCell.setCellValue(descFiltro);
                descCell.setCellStyle(boldStyle);
            }

            // --- TABLA DE DATOS ---
            int startRow = 9;
            Row headerRow = sheet.createRow(startRow);
            String[] headers = { "#", "TIPO DOC.", "SERIE", "CANT.", "DESDE", "HASTA", "BASE IMP.", "INAFECTO",
                    "EXONERADO", "IGV.", "TOTAL" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = startRow + 1;
            int counter = 1;
            BigDecimal sumBase = BigDecimal.ZERO;
            BigDecimal sumInaf = BigDecimal.ZERO;
            BigDecimal sumExo = BigDecimal.ZERO;
            BigDecimal sumIgv = BigDecimal.ZERO;
            BigDecimal sumTotal = BigDecimal.ZERO;

            for (com.pe.articulos.modules.venta_registro.dto.CorrelatividadDTO dto : datos) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(counter++);
                row.createCell(1).setCellValue(dto.getTipoDocDescripcion());
                row.createCell(2).setCellValue(dto.getSerie() != null ? dto.getSerie() : "-");
                row.createCell(3).setCellValue(dto.getCantidad() != null ? dto.getCantidad().doubleValue() : 0.0);
                row.createCell(4).setCellValue(dto.getDesde() != null ? dto.getDesde() : 0);
                row.createCell(5).setCellValue(dto.getHasta() != null ? dto.getHasta() : 0);

                BigDecimal base = dto.getBaseImp() != null ? dto.getBaseImp() : BigDecimal.ZERO;
                BigDecimal igv = dto.getIgv() != null ? dto.getIgv() : BigDecimal.ZERO;
                BigDecimal exo = dto.getValorExo() != null ? dto.getValorExo() : BigDecimal.ZERO;
                BigDecimal inaf = dto.getValorInaf() != null ? dto.getValorInaf() : BigDecimal.ZERO;
                BigDecimal total = dto.getTotal() != null ? dto.getTotal() : BigDecimal.ZERO;

                row.createCell(6).setCellValue(base.doubleValue());
                row.createCell(7).setCellValue(inaf.doubleValue());
                row.createCell(8).setCellValue(exo.doubleValue());
                row.createCell(9).setCellValue(igv.doubleValue());
                row.createCell(10).setCellValue(total.doubleValue());

                for (int i = 0; i <= 5; i++) {
                    if (row.getCell(i) == null)
                        row.createCell(i);
                    row.getCell(i).setCellStyle(dataStyle);
                }
                for (int i = 6; i <= 10; i++) {
                    if (row.getCell(i) == null)
                        row.createCell(i);
                    row.getCell(i).setCellStyle(amountStyle);
                }

                sumBase = sumBase.add(base);
                sumInaf = sumInaf.add(inaf);
                sumExo = sumExo.add(exo);
                sumIgv = sumIgv.add(igv);
                sumTotal = sumTotal.add(total);
            }

            // Fila de Totales
            Row footerRow = sheet.createRow(rowIdx);
            Cell totalLabel = footerRow.createCell(0);
            totalLabel.setCellValue("TOTAL GENERAL");
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx, rowIdx, 0, 5));
            for (int i = 0; i <= 5; i++) {
                Cell c = footerRow.getCell(i);
                if (c == null)
                    c = footerRow.createCell(i);
                c.setCellStyle(totalRowStyle);
            }

            footerRow.createCell(6).setCellValue(sumBase.doubleValue());
            footerRow.createCell(7).setCellValue(sumInaf.doubleValue());
            footerRow.createCell(8).setCellValue(sumExo.doubleValue());
            footerRow.createCell(9).setCellValue(sumIgv.doubleValue());
            footerRow.createCell(10).setCellValue(sumTotal.doubleValue());

            for (int i = 6; i <= 10; i++) {
                CellStyle totalAmountStyle = workbook.createCellStyle();
                totalAmountStyle.cloneStyleFrom(totalRowStyle);
                totalAmountStyle.setDataFormat(workbook.createDataFormat().getFormat("\"S/ \" #,##0.00"));
                footerRow.getCell(i).setCellStyle(totalAmountStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            log.error("Error al generar Excel de Correlatividad", e);
            throw new RuntimeException("Error al generar Excel de Correlatividad", e);
        }
    }

    private ByteArrayInputStream generateExcel(String templatePath, Map<String, Object> model) {
        try {
            log.info("Iniciando generación de Excel con plantilla: {}", templatePath);
            InputStream is = new ClassPathResource(templatePath).getInputStream();
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            Context context = new Context();
            if (model != null) {
                model.forEach(context::putVar);
            }

            log.info("Procesando plantilla JXLS...");
            JxlsHelper.getInstance().processTemplate(is, os, context);
            log.info("Excel generado exitosamente.");
            return new ByteArrayInputStream(os.toByteArray());
        } catch (Throwable t) {
            log.error("Error crítico (Throwable) al generar Excel: {}", t.getMessage(), t);
            throw new RuntimeException("Error al generar Excel con JXLS: " + t.getMessage(), t);
        }
    }

    private LogoInfo getLogoInfo(Clinica clinica) {
        if (clinica == null)
            return null;

        String logoBase64 = "RECTANGULAR".equals(clinica.getLogoPrincipal())
                ? clinica.getLogoRectangular()
                : clinica.getLogoCuadrado();

        if (logoBase64 == null || logoBase64.isEmpty())
            return null;

        try {
            int type = Workbook.PICTURE_TYPE_PNG;
            String base64Data = logoBase64;

            if (base64Data.contains(",")) {
                String prefix = base64Data.split(",")[0].toLowerCase();
                if (prefix.contains("jpeg") || prefix.contains("jpg")) {
                    type = Workbook.PICTURE_TYPE_JPEG;
                }
                base64Data = base64Data.split(",")[1];
            }

            // Limpieza básica de la cadena Base64
            base64Data = base64Data.replaceAll("\\s", "");

            byte[] bytes = Base64.getDecoder().decode(base64Data);
            log.info("Logo decodificado exitosamente. Tamaño: {} bytes", bytes.length);
            return new LogoInfo(bytes, type);
        } catch (Exception e) {
            log.error("Error al decodificar logo de clínica: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public ByteArrayInputStream exportarReporteCajas(String idSucursal, String idPuntoVenta, Integer mes, Integer anio) {
        Long idSuc;
        try {
            idSuc = Long.parseLong(idSucursal);
        } catch (Exception e) {
            idSuc = -1L;
        }

        List<CajaChica> cajas = (idSuc != -1L) ? cajaChicaRepository.findCajasArqueadas(idSuc, mes, anio)
                : java.util.Collections.emptyList();

        Clinica clinica = clinicaRepository.findAll().stream().findFirst().orElse(null);
        String nombreEmpresa = clinica != null ? clinica.getRazonSocial() : "-";
        String rucEmpresa = clinica != null ? clinica.getRuc() : "-";

        Sucursal sucursal = sucursalRepository.findById(idSuc).orElse(null);
        String nombreSucursal = sucursal != null ? sucursal.getNombreSucursal() : "Sucursal " + idSucursal;
        String direccionSucursal = sucursal != null ? sucursal.getDireccion() : "-";

        LogoInfo logoInfo = getLogoInfo(clinica);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte de Cajas");
            sheet.setColumnWidth(0, 4000);
            sheet.setColumnWidth(1, 4000);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);

            CellStyle amountStyle = workbook.createCellStyle();
            amountStyle.cloneStyleFrom(dataStyle);
            amountStyle.setDataFormat(workbook.createDataFormat().getFormat("\"S/ \" #,##0.00"));
            amountStyle.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle totalRowStyle = workbook.createCellStyle();
            totalRowStyle.cloneStyleFrom(headerStyle);
            totalRowStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());

            if (logoInfo != null && logoInfo.getBytes() != null) {
                int pictureIdx = workbook.addPicture(logoInfo.getBytes(), logoInfo.getWorkbookType());
                CreationHelper helper = workbook.getCreationHelper();
                Drawing<?> drawing = sheet.createDrawingPatriarch();
                ClientAnchor anchor = helper.createClientAnchor();
                anchor.setCol1(0);
                anchor.setRow1(0);
                Picture pict = drawing.createPicture(anchor, pictureIdx);
                pict.resize(1.8, 4.0);
            }

            CellStyle companyNameStyle = workbook.createCellStyle();
            Font companyFont = workbook.createFont();
            companyFont.setBold(true);
            companyFont.setFontHeightInPoints((short) 14);
            companyNameStyle.setFont(companyFont);

            Row r0 = sheet.getRow(0);
            if (r0 == null)
                r0 = sheet.createRow(0);
            Cell cClinic = r0.createCell(2);
            cClinic.setCellValue(nombreEmpresa);
            cClinic.setCellStyle(companyNameStyle);

            Row r1 = sheet.getRow(1);
            if (r1 == null)
                r1 = sheet.createRow(1);
            r1.createCell(2).setCellValue("RUC: " + rucEmpresa);

            Row r2 = sheet.getRow(2);
            if (r2 == null)
                r2 = sheet.createRow(2);
            r2.createCell(2).setCellValue(direccionSucursal + " - " + nombreSucursal);

            Row r4 = sheet.createRow(4);
            Cell titleCell = r4.createCell(0);
            titleCell.setCellValue("REPORTE HISTÓRICO DE ARQUEOS DE CAJA");
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setUnderline(Font.U_SINGLE);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(4, 4, 0, 8));

            Row r6 = sheet.createRow(6);
            r6.createCell(0).setCellValue("PERIODO:");
            String[] meses = { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre",
                    "Octubre", "Noviembre", "Diciembre" };
            String periodoStr = (mes >= 1 && mes <= 12 ? meses[mes - 1] : "Mes " + mes) + " de " + anio;
            Cell periodoCell = r6.createCell(1);
            periodoCell.setCellValue(periodoStr);
            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);
            periodoCell.setCellStyle(boldStyle);

            r6.createCell(7).setCellValue("FECHA GEN.:");
            r6.createCell(8).setCellValue(LocalDate.now().toString());

            int startRow = 9;
            Row headerRow = sheet.createRow(startRow);
            String[] headers = { "#", "CAJA", "USUARIO/CAJERO", "FECHA APERTURA", "FECHA CIERRE/ARQUEO",
                    "SALDO INICIAL", "SALDO TEÓRICO", "SALDO REAL (ARQUEO)", "DIFERENCIA/DESCUADRE" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = startRow + 1;
            int counter = 1;
            BigDecimal sumInicial = BigDecimal.ZERO;
            BigDecimal sumTeorico = BigDecimal.ZERO;
            BigDecimal sumReal = BigDecimal.ZERO;
            BigDecimal sumDiferencia = BigDecimal.ZERO;

            for (CajaChica c : cajas) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(counter++);
                row.createCell(1).setCellValue(c.getNombre() != null ? c.getNombre() : "-");
                row.createCell(2).setCellValue(c.getIdUsuarioCajero() != null ? c.getIdUsuarioCajero() : "-");
                row.createCell(3).setCellValue(c.getCreatedAt() != null ? c.getCreatedAt().toString() : "");
                row.createCell(4).setCellValue(c.getFechaCierre() != null ? c.getFechaCierre().toString() : "");

                BigDecimal saldoInicial = c.getSaldoInicial() != null ? c.getSaldoInicial() : BigDecimal.ZERO;
                BigDecimal saldoTeorico = c.getSaldoActual() != null ? c.getSaldoActual() : BigDecimal.ZERO;
                BigDecimal saldoReal = c.getSaldoCierreReal() != null ? c.getSaldoCierreReal() : BigDecimal.ZERO;
                BigDecimal diferencia = saldoReal.subtract(saldoTeorico);

                row.createCell(5).setCellValue(saldoInicial.doubleValue());
                row.createCell(6).setCellValue(saldoTeorico.doubleValue());
                row.createCell(7).setCellValue(saldoReal.doubleValue());
                row.createCell(8).setCellValue(diferencia.doubleValue());

                for (int i = 0; i <= 4; i++) {
                    if (row.getCell(i) == null)
                        row.createCell(i);
                    row.getCell(i).setCellStyle(dataStyle);
                }
                for (int i = 5; i <= 8; i++) {
                    if (row.getCell(i) == null)
                        row.createCell(i);
                    row.getCell(i).setCellStyle(amountStyle);
                }

                sumInicial = sumInicial.add(saldoInicial);
                sumTeorico = sumTeorico.add(saldoTeorico);
                sumReal = sumReal.add(saldoReal);
                sumDiferencia = sumDiferencia.add(diferencia);
            }

            Row footerRow = sheet.createRow(rowIdx);
            Cell totalLabel = footerRow.createCell(0);
            totalLabel.setCellValue("TOTAL GENERAL");
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx, rowIdx, 0, 4));
            for (int i = 0; i <= 4; i++) {
                Cell cell = footerRow.getCell(i);
                if (cell == null)
                    cell = footerRow.createCell(i);
                cell.setCellStyle(totalRowStyle);
            }

            footerRow.createCell(5).setCellValue(sumInicial.doubleValue());
            footerRow.createCell(6).setCellValue(sumTeorico.doubleValue());
            footerRow.createCell(7).setCellValue(sumReal.doubleValue());
            footerRow.createCell(8).setCellValue(sumDiferencia.doubleValue());

            for (int i = 5; i <= 8; i++) {
                CellStyle totalAmountStyle = workbook.createCellStyle();
                totalAmountStyle.cloneStyleFrom(totalRowStyle);
                totalAmountStyle.setDataFormat(workbook.createDataFormat().getFormat("\"S/ \" #,##0.00"));
                footerRow.getCell(i).setCellStyle(totalAmountStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            log.error("Error al generar Excel de Cajas", e);
            throw new RuntimeException("Error al generar Excel de Cajas", e);
        }
    }

    @Override
    public ByteArrayInputStream exportarReporteDescuentos(String idSucursal, String idPuntoVenta, Integer mes, Integer anio) {
        Long idSucursalLong = null;
        try {
            idSucursalLong = Long.parseLong(idSucursal);
        } catch (NumberFormatException e) {
        }

        List<VentaDetalle> detalles = (idSucursalLong != null)
                ? ventaDetalleRepository.findWithDescuentos(idSucursalLong, mes, anio)
                : java.util.Collections.emptyList();

        Clinica clinica = clinicaRepository.findAll().stream().findFirst().orElse(null);
        String nombreEmpresa = clinica != null ? clinica.getRazonSocial() : "-";
        String rucEmpresa = clinica != null ? clinica.getRuc() : "-";

        Sucursal sucursal = (idSucursalLong != null) ? sucursalRepository.findById(idSucursalLong).orElse(null) : null;
        String nombreSucursal = sucursal != null ? sucursal.getNombreSucursal() : "Sucursal " + idSucursal;
        String direccionSucursal = sucursal != null ? sucursal.getDireccion() : "-";

        LogoInfo logoInfo = getLogoInfo(clinica);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte de Descuentos");
            sheet.setColumnWidth(0, 4000);
            sheet.setColumnWidth(1, 4000);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);

            CellStyle amountStyle = workbook.createCellStyle();
            amountStyle.cloneStyleFrom(dataStyle);
            amountStyle.setDataFormat(workbook.createDataFormat().getFormat("\"S/ \" #,##0.00"));
            amountStyle.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle totalRowStyle = workbook.createCellStyle();
            totalRowStyle.cloneStyleFrom(headerStyle);
            totalRowStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());

            if (logoInfo != null && logoInfo.getBytes() != null) {
                int pictureIdx = workbook.addPicture(logoInfo.getBytes(), logoInfo.getWorkbookType());
                CreationHelper helper = workbook.getCreationHelper();
                Drawing<?> drawing = sheet.createDrawingPatriarch();
                ClientAnchor anchor = helper.createClientAnchor();
                anchor.setCol1(0);
                anchor.setRow1(0);
                Picture pict = drawing.createPicture(anchor, pictureIdx);
                pict.resize(1.8, 4.0);
            }

            CellStyle companyNameStyle = workbook.createCellStyle();
            Font companyFont = workbook.createFont();
            companyFont.setBold(true);
            companyFont.setFontHeightInPoints((short) 14);
            companyNameStyle.setFont(companyFont);

            Row r0 = sheet.getRow(0);
            if (r0 == null)
                r0 = sheet.createRow(0);
            Cell cClinic = r0.createCell(2);
            cClinic.setCellValue(nombreEmpresa);
            cClinic.setCellStyle(companyNameStyle);

            Row r1 = sheet.getRow(1);
            if (r1 == null)
                r1 = sheet.createRow(1);
            r1.createCell(2).setCellValue("RUC: " + rucEmpresa);

            Row r2 = sheet.getRow(2);
            if (r2 == null)
                r2 = sheet.createRow(2);
            r2.createCell(2).setCellValue(direccionSucursal + " - " + nombreSucursal);

            Row r4 = sheet.createRow(4);
            Cell titleCell = r4.createCell(0);
            titleCell.setCellValue("REPORTE HISTÓRICO DE DESCUENTOS EN VENTAS");
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setUnderline(Font.U_SINGLE);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(4, 4, 0, 7));

            Row r6 = sheet.createRow(6);
            r6.createCell(0).setCellValue("PERIODO:");
            String[] meses = { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre",
                    "Octubre", "Noviembre", "Diciembre" };
            String periodoStr = (mes >= 1 && mes <= 12 ? meses[mes - 1] : "Mes " + mes) + " de " + anio;
            Cell periodoCell = r6.createCell(1);
            periodoCell.setCellValue(periodoStr);
            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);
            periodoCell.setCellStyle(boldStyle);

            r6.createCell(6).setCellValue("FECHA GEN.:");
            r6.createCell(7).setCellValue(LocalDate.now().toString());

            int startRow = 9;
            Row headerRow = sheet.createRow(startRow);
            String[] headers = { "#", "FECHA", "DOCUMENTO", "CLIENTE", "SUSTENTO / CUPÓN", "VENTA BASE", "MTO. DESC.",
                    "TOTAL" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = startRow + 1;
            int counter = 1;
            BigDecimal sumBase = BigDecimal.ZERO;
            BigDecimal sumDesc = BigDecimal.ZERO;
            BigDecimal sumTotal = BigDecimal.ZERO;

            for (VentaDetalle d : detalles) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(counter++);
                row.createCell(1)
                        .setCellValue(d.getVentaRegistro() != null && d.getVentaRegistro().getFecha() != null
                                ? d.getVentaRegistro().getFecha().toString()
                                : "");
                row.createCell(2)
                        .setCellValue(d.getVentaRegistro() != null
                                ? d.getVentaRegistro().getSerie() + "-" + d.getVentaRegistro().getNumdoc()
                                : "-");
                row.createCell(3)
                        .setCellValue(d.getVentaRegistro() != null && d.getVentaRegistro().getNombrePac() != null
                                ? d.getVentaRegistro().getNombrePac()
                                : "-");
                row.createCell(4).setCellValue(d.getGlosa() != null ? d.getGlosa() : "-");

                BigDecimal base = d.getBaseImp() != null ? d.getBaseImp() : BigDecimal.ZERO;
                BigDecimal desc = d.getMontoDescuento() != null ? d.getMontoDescuento() : BigDecimal.ZERO;
                BigDecimal total = d.getTotal() != null ? d.getTotal() : BigDecimal.ZERO;

                row.createCell(5).setCellValue(base.doubleValue());
                row.createCell(6).setCellValue(desc.doubleValue());
                row.createCell(7).setCellValue(total.doubleValue());

                for (int i = 0; i <= 4; i++) {
                    if (row.getCell(i) == null)
                        row.createCell(i);
                    row.getCell(i).setCellStyle(dataStyle);
                }
                for (int i = 5; i <= 7; i++) {
                    if (row.getCell(i) == null)
                        row.createCell(i);
                    row.getCell(i).setCellStyle(amountStyle);
                }

                sumBase = sumBase.add(base);
                sumDesc = sumDesc.add(desc);
                sumTotal = sumTotal.add(total);
            }

            Row footerRow = sheet.createRow(rowIdx);
            Cell totalLabel = footerRow.createCell(0);
            totalLabel.setCellValue("TOTAL GENERAL");
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx, rowIdx, 0, 4));
            for (int i = 0; i <= 4; i++) {
                Cell cell = footerRow.getCell(i);
                if (cell == null)
                    cell = footerRow.createCell(i);
                cell.setCellStyle(totalRowStyle);
            }

            footerRow.createCell(5).setCellValue(sumBase.doubleValue());
            footerRow.createCell(6).setCellValue(sumDesc.doubleValue());
            footerRow.createCell(7).setCellValue(sumTotal.doubleValue());

            for (int i = 5; i <= 7; i++) {
                CellStyle totalAmountStyle = workbook.createCellStyle();
                totalAmountStyle.cloneStyleFrom(totalRowStyle);
                totalAmountStyle.setDataFormat(workbook.createDataFormat().getFormat("\"S/ \" #,##0.00"));
                footerRow.getCell(i).setCellStyle(totalAmountStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            log.error("Error al generar Excel de Descuentos", e);
            throw new RuntimeException("Error al generar Excel de Descuentos", e);
        }
    }

    @Override
    public ByteArrayInputStream exportarAnulados(String idSucursal, String idPuntoVenta, Integer mes, Integer anio) {
        Long idSucursalLong = null;
        try {
            idSucursalLong = Long.parseLong(idSucursal);
        } catch (NumberFormatException e) {
        }

        LocalDate inicio = LocalDate.of(anio, mes, 1);
        LocalDate fin = inicio.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());

        List<VentaRegistro> anulados = (idSucursalLong != null)
                ? ventaRepository.findSireVentas(idSucursalLong, inicio, fin)
                        .stream()
                        .filter(v -> v.getEstado() == com.pe.articulos.core.enums.EstadoGeneral.ANULADO)
                        .collect(java.util.stream.Collectors.toList())
                : java.util.Collections.emptyList();

        Clinica clinica = clinicaRepository.findAll().stream().findFirst().orElse(null);
        String nombreEmpresa = clinica != null ? clinica.getRazonSocial() : "-";
        String rucEmpresa = clinica != null ? clinica.getRuc() : "-";

        Sucursal sucursal = (idSucursalLong != null) ? sucursalRepository.findById(idSucursalLong).orElse(null) : null;
        String nombreSucursal = sucursal != null ? sucursal.getNombreSucursal() : "Sucursal " + idSucursal;
        String direccionSucursal = sucursal != null ? sucursal.getDireccion() : "-";

        LogoInfo logoInfo = getLogoInfo(clinica);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Comprobantes Anulados");
            sheet.setColumnWidth(0, 4000);
            sheet.setColumnWidth(1, 4000);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);

            CellStyle amountStyle = workbook.createCellStyle();
            amountStyle.cloneStyleFrom(dataStyle);
            amountStyle.setDataFormat(workbook.createDataFormat().getFormat("\"S/ \" #,##0.00"));
            amountStyle.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle totalRowStyle = workbook.createCellStyle();
            totalRowStyle.cloneStyleFrom(headerStyle);
            totalRowStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());

            if (logoInfo != null && logoInfo.getBytes() != null) {
                int pictureIdx = workbook.addPicture(logoInfo.getBytes(), logoInfo.getWorkbookType());
                CreationHelper helper = workbook.getCreationHelper();
                Drawing<?> drawing = sheet.createDrawingPatriarch();
                ClientAnchor anchor = helper.createClientAnchor();
                anchor.setCol1(0);
                anchor.setRow1(0);
                Picture pict = drawing.createPicture(anchor, pictureIdx);
                pict.resize(1.8, 4.0);
            }

            CellStyle companyNameStyle = workbook.createCellStyle();
            Font companyFont = workbook.createFont();
            companyFont.setBold(true);
            companyFont.setFontHeightInPoints((short) 14);
            companyNameStyle.setFont(companyFont);

            Row r0 = sheet.getRow(0);
            if (r0 == null)
                r0 = sheet.createRow(0);
            Cell cClinic = r0.createCell(2);
            cClinic.setCellValue(nombreEmpresa);
            cClinic.setCellStyle(companyNameStyle);

            Row r1 = sheet.getRow(1);
            if (r1 == null)
                r1 = sheet.createRow(1);
            r1.createCell(2).setCellValue("RUC: " + rucEmpresa);

            Row r2 = sheet.getRow(2);
            if (r2 == null)
                r2 = sheet.createRow(2);
            r2.createCell(2).setCellValue(direccionSucursal + " - " + nombreSucursal);

            Row r4 = sheet.createRow(4);
            Cell titleCell = r4.createCell(0);
            titleCell.setCellValue("REPORTE HISTÓRICO DE COMPROBANTES ANULADOS");
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setUnderline(Font.U_SINGLE);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(4, 4, 0, 6));

            Row r6 = sheet.createRow(6);
            r6.createCell(0).setCellValue("PERIODO:");
            String[] meses = { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre",
                    "Octubre", "Noviembre", "Diciembre" };
            String periodoStr = (mes >= 1 && mes <= 12 ? meses[mes - 1] : "Mes " + mes) + " de " + anio;
            Cell periodoCell = r6.createCell(1);
            periodoCell.setCellValue(periodoStr);
            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);
            periodoCell.setCellStyle(boldStyle);

            r6.createCell(5).setCellValue("FECHA GEN.:");
            r6.createCell(6).setCellValue(LocalDate.now().toString());

            int startRow = 9;
            Row headerRow = sheet.createRow(startRow);
            String[] headers = { "#", "FECHA ANULACIÓN", "DOCUMENTO", "CLIENTE", "MOTIVO DE ANULACIÓN", "IMPORTE",
                    "USUARIO" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = startRow + 1;
            int counter = 1;
            BigDecimal sumTotal = BigDecimal.ZERO;

            for (VentaRegistro v : anulados) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(counter++);
                row.createCell(1).setCellValue(v.getFechaAnul() != null ? v.getFechaAnul().toString()
                        : (v.getFecha() != null ? v.getFecha().toString() : ""));
                row.createCell(2).setCellValue(v.getSerie() + "-" + v.getNumdoc());
                row.createCell(3).setCellValue(v.getNombrePac() != null ? v.getNombrePac() : "-");
                row.createCell(4).setCellValue(v.getMotivoAnul2() != null ? v.getMotivoAnul2() : "-");

                BigDecimal total = v.getTotal() != null ? v.getTotal() : BigDecimal.ZERO;
                row.createCell(5).setCellValue(total.doubleValue());
                row.createCell(6).setCellValue(v.getIdPersonalUser() != null ? v.getIdPersonalUser() : "-");

                for (int i = 0; i <= 4; i++) {
                    if (row.getCell(i) == null)
                        row.createCell(i);
                    row.getCell(i).setCellStyle(dataStyle);
                }
                if (row.getCell(5) == null)
                    row.createCell(5);
                row.getCell(5).setCellStyle(amountStyle);
                if (row.getCell(6) == null)
                    row.createCell(6);
                row.getCell(6).setCellStyle(dataStyle);

                sumTotal = sumTotal.add(total);
            }

            Row footerRow = sheet.createRow(rowIdx);
            Cell totalLabel = footerRow.createCell(0);
            totalLabel.setCellValue("TOTAL GENERAL");
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx, rowIdx, 0, 4));
            for (int i = 0; i <= 4; i++) {
                Cell cell = footerRow.getCell(i);
                if (cell == null)
                    cell = footerRow.createCell(i);
                cell.setCellStyle(totalRowStyle);
            }

            footerRow.createCell(5).setCellValue(sumTotal.doubleValue());
            CellStyle totalAmountStyle = workbook.createCellStyle();
            totalAmountStyle.cloneStyleFrom(totalRowStyle);
            totalAmountStyle.setDataFormat(workbook.createDataFormat().getFormat("\"S/ \" #,##0.00"));
            footerRow.getCell(5).setCellStyle(totalAmountStyle);

            Cell dummyCell = footerRow.createCell(6);
            dummyCell.setCellStyle(totalRowStyle);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            log.error("Error al generar Excel de Anulados", e);
            throw new RuntimeException("Error al generar Excel de Anulados", e);
        }
    }

    @lombok.Value
    private static class LogoInfo {
        byte[] bytes;
        int workbookType;
    }
}

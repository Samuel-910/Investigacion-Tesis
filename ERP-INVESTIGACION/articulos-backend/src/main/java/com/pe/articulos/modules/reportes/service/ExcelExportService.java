package com.pe.articulos.modules.reportes.service;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

public interface ExcelExportService {
    ByteArrayInputStream exportRVIE(String idSucursal, String idPuntoVenta, LocalDate fechaInicio, LocalDate fechaFin);

    ByteArrayInputStream exportRCE(String idSucursal, String idPuntoVenta, LocalDate fechaInicio, LocalDate fechaFin);

    ByteArrayInputStream exportarKardex(Long idCatalogo, Long idSucursal, LocalDate desde, LocalDate hasta);

    ByteArrayInputStream exportarCorrelatividad(java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin, String tipoDoc, java.util.List<com.pe.articulos.modules.venta_registro.dto.CorrelatividadDTO> datos);

    ByteArrayInputStream exportarReporteCajas(String idSucursal, String idPuntoVenta, Integer mes, Integer anio);

    ByteArrayInputStream exportarReporteDescuentos(String idSucursal, String idPuntoVenta, Integer mes, Integer anio);

    ByteArrayInputStream exportarAnulados(String idSucursal, String idPuntoVenta, Integer mes, Integer anio);
}

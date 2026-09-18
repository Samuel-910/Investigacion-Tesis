package com.pe.articulos.modules.reportes.service;

import com.pe.articulos.modules.reportes.dto.*;
import java.util.List;

public interface ReporteService {
    ReporteResumenDTO obtenerResumen(String idSucursal, String idPuntoVenta);

    ReporteVentaDetalleDTO obtenerVentasMensuales(String idSucursal, String idPuntoVenta);

    ReporteCompraDetalleDTO obtenerComprasMensuales(String idSucursal, String idPuntoVenta);

    ReporteDeudaDetalleDTO obtenerDeudasPorProveedor();

    List<DataPuntoDTO> obtenerTopProductos(String idSucursal, String idPuntoVenta);

    List<SireVentaDTO> obtenerSireVentas(String idSucursal, String idPuntoVenta, String inicio, String fin);

    List<SireCompraDTO> obtenerSireCompras(String idSucursal, String idPuntoVenta, String inicio, String fin);

    EstadisticasDTO obtenerEstadisticasVentas(String idSucursal, String idPuntoVenta, String inicio, String fin);

    EstadisticasDTO obtenerEstadisticasCompras(String idSucursal, String idPuntoVenta, String inicio, String fin);

    List<ReporteCajaDetalleDTO> obtenerReporteCajas(String idSucursal, String idPuntoVenta, Integer mes, Integer anio);

    ReporteDescuentoDetalleDTO obtenerReporteDescuentos(String idSucursal, String idPuntoVenta, Integer mes, Integer anio);

    ReporteAnuladoDetalleDTO obtenerComprobantesAnulados(String idSucursal, String idPuntoVenta, Integer mes, Integer anio);

    ReporteAnuladoDetalleDTO obtenerNotasCredito(String idSucursal, String idPuntoVenta, Integer mes, Integer anio);

    // Configuración de columnas
    String guardarConfiguracionColumnas(String reportKey, Long idUsuario, Long idSucursal, String visibleColumns);
    String obtenerConfiguracionColumnas(String reportKey, Long idUsuario, Long idSucursal);
}

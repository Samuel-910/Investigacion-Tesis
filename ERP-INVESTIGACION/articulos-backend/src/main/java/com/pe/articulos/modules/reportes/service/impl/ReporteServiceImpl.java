package com.pe.articulos.modules.reportes.service.impl;

import com.pe.articulos.modules.compras.entity.Compra;
import com.pe.articulos.modules.compras.repository.CompraRepository;
import com.pe.articulos.modules.proveedores.repository.CuentaProveedorRepository;
import com.pe.articulos.modules.reportes.dto.ReporteResumenDTO;
import com.pe.articulos.modules.reportes.entity.ReportColumnConfig;
import com.pe.articulos.modules.reportes.repository.ReportColumnConfigRepository;
import com.pe.articulos.modules.reportes.service.ReporteService;
import com.pe.articulos.modules.venta_registro.entity.VentaDetalle;
import com.pe.articulos.modules.venta_registro.entity.VentaRegistro;
import com.pe.articulos.modules.venta_registro.repository.VentaDetalleRepository;
import com.pe.articulos.modules.venta_registro.repository.VentaRegistroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.pe.articulos.modules.reportes.dto.*;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ReporteServiceImpl implements ReporteService {

        private final VentaRegistroRepository ventaRepository;
        private final CompraRepository compraRepository;
        private final CuentaProveedorRepository cuentaProveedorRepository;
        private final VentaDetalleRepository ventaDetalleRepository;
        private final ReportColumnConfigRepository reportColumnConfigRepository;
        private final com.pe.articulos.modules.caja_chica.repository.CajaChicaRepository cajaChicaRepository;

        @Override
        public ReporteResumenDTO obtenerResumen(String idSucursal, String idPuntoVenta) {
                Long idSucursalLong = null;
                try {
                        idSucursalLong = Long.parseLong(idSucursal);
                } catch (NumberFormatException e) {
                }
                Long idPuntoVentaLong = null;
                if (idPuntoVenta != null && !idPuntoVenta.equals("TODOS")) {
                        try {
                                idPuntoVentaLong = Long.parseLong(idPuntoVenta);
                        } catch (NumberFormatException e) {
                        }
                }

                BigDecimal totalVentas = ventaRepository.totalVentas(idSucursalLong, idPuntoVentaLong);
                BigDecimal totalCompras = idSucursalLong != null ? compraRepository.totalCompras(idSucursalLong)
                                : BigDecimal.ZERO;
                BigDecimal totalPorPagar = cuentaProveedorRepository.totalCuentasPorPagar();

                return ReporteResumenDTO.builder()
                                .totalVentas(totalVentas != null ? totalVentas : BigDecimal.ZERO)
                                .totalCompras(totalCompras != null ? totalCompras : BigDecimal.ZERO)
                                .totalPorPagar(totalPorPagar != null ? totalPorPagar : BigDecimal.ZERO)
                                .ventasMensuales(mapToDataPuntoDTO(
                                                ventaRepository.ventasMensualesRaw(idSucursalLong, idPuntoVentaLong)))
                                .comprasMensuales(
                                                idSucursalLong != null
                                                                ? mapToDataPuntoDTO(compraRepository
                                                                                .comprasMensualesRaw(idSucursalLong,
                                                                                                org.springframework.data.domain.PageRequest
                                                                                                                .of(0, 100))
                                                                                .getContent())
                                                                : java.util.Collections.emptyList())
                                .deudasPorProveedor(
                                                mapToDataPuntoDTO(cuentaProveedorRepository.deudasPorProveedorRaw()))
                                .topProductosVendidos(
                                                mapToDataPuntoDTO(ventaRepository.topProductosVendidosRaw(
                                                                idSucursalLong, idPuntoVentaLong,
                                                                org.springframework.data.domain.PageRequest.of(0, 10))))
                                .build();
        }

        @Override
        public ReporteVentaDetalleDTO obtenerVentasMensuales(String idSucursal, String idPuntoVenta) {
                Long idSucursalLong = null;
                try {
                        idSucursalLong = Long.parseLong(idSucursal);
                } catch (NumberFormatException e) {
                }
                Long idPuntoVentaLong = null;
                if (idPuntoVenta != null && !idPuntoVenta.equals("TODOS")) {
                        try {
                                idPuntoVentaLong = Long.parseLong(idPuntoVenta);
                        } catch (NumberFormatException e) {
                        }
                }
                BigDecimal total = ventaRepository.totalVentas(idSucursalLong, idPuntoVentaLong);
                return ReporteVentaDetalleDTO.builder()
                                .total(total != null ? total : BigDecimal.ZERO)
                                .data(mapToDataPuntoDTO(
                                                ventaRepository.ventasMensualesRaw(idSucursalLong, idPuntoVentaLong)))
                                .build();
        }

        @Override
        public ReporteCompraDetalleDTO obtenerComprasMensuales(String idSucursal, String idPuntoVenta) {
                Long idSucursalLong = null;
                try {
                        idSucursalLong = Long.parseLong(idSucursal);
                } catch (NumberFormatException e) {
                }
                Long idPuntoVentaLong = null;
                if (idPuntoVenta != null && !idPuntoVenta.equals("TODOS")) {
                        try {
                                idPuntoVentaLong = Long.parseLong(idPuntoVenta);
                        } catch (NumberFormatException e) {
                        }
                }

                BigDecimal total = idSucursalLong != null ? compraRepository.totalCompras(idSucursalLong)
                                : BigDecimal.ZERO;
                return ReporteCompraDetalleDTO.builder()
                                .total(total != null ? total : BigDecimal.ZERO)
                                .data(idSucursalLong != null ? mapToDataPuntoDTO(compraRepository
                                                .comprasMensualesRaw(idSucursalLong,
                                                                org.springframework.data.domain.PageRequest.of(0, 100))
                                                .getContent()) : java.util.Collections.emptyList())
                                .build();
        }

        @Override
        public ReporteDeudaDetalleDTO obtenerDeudasPorProveedor() {
                BigDecimal total = cuentaProveedorRepository.totalCuentasPorPagar();
                return ReporteDeudaDetalleDTO.builder()
                                .total(total != null ? total : BigDecimal.ZERO)
                                .data(mapToDataPuntoDTO(cuentaProveedorRepository.deudasPorProveedorRaw()))
                                .build();
        }

        @Override
        public List<DataPuntoDTO> obtenerTopProductos(String idSucursal, String idPuntoVenta) {
                Long idSucursalLong = null;
                try {
                        idSucursalLong = Long.parseLong(idSucursal);
                } catch (NumberFormatException e) {
                }
                Long idPuntoVentaLong = null;
                if (idPuntoVenta != null && !idPuntoVenta.equals("TODOS")) {
                        try {
                                idPuntoVentaLong = Long.parseLong(idPuntoVenta);
                        } catch (NumberFormatException e) {
                        }
                }
                return mapToDataPuntoDTO(ventaRepository.topProductosVendidosRaw(idSucursalLong, idPuntoVentaLong,
                                org.springframework.data.domain.PageRequest.of(0, 10)));
        }

        @Override
        public List<SireVentaDTO> obtenerSireVentas(String idSucursal, String idPuntoVenta, String inicio, String fin) {
                Long idSucursalLong = null;
                try {
                        idSucursalLong = Long.parseLong(idSucursal);
                } catch (NumberFormatException e) {
                }
                Long idPuntoVentaLong = null;
                if (idPuntoVenta != null && !idPuntoVenta.equals("TODOS")) {
                        try {
                                idPuntoVentaLong = Long.parseLong(idPuntoVenta);
                        } catch (NumberFormatException e) {
                        }
                }
                LocalDate start = LocalDate.parse(inicio);
                LocalDate end = LocalDate.parse(fin);
                List<VentaRegistro> ventas = ventaRepository.findSireVentas(idSucursalLong, start, end);
                return ventas.stream().map(v -> SireVentaDTO.builder()
                                .periodo(v.getFecha() != null ? v.getFecha()
                                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM00")) : "")
                                .carSunat("")
                                .fecha(v.getFecha())
                                .fechaVencimiento(v.getFecha())
                                .tipoDoc(v.getTipoDoc())
                                .serie(v.getSerie())
                                .numero(v.getNumdoc())
                                .nroDocCli(v.getNroDni() != null ? v.getNroDni()
                                                : (v.getRuc() != null ? v.getRuc() : ""))
                                .cliente(v.getNombrePac() != null ? v.getNombrePac()
                                                : (v.getRazon() != null ? v.getRazon() : ""))
                                .baseImponible(v.getBaseImp() != null ? v.getBaseImp() : BigDecimal.ZERO)
                                .igv(v.getIgv() != null ? v.getIgv() : BigDecimal.ZERO)
                                .exonerado(v.getValorExo() != null ? v.getValorExo() : BigDecimal.ZERO)
                                .inafecto(v.getValorInaf() != null ? v.getValorInaf() : BigDecimal.ZERO)
                                .descuentoBase(v.getDescuento() != null ? v.getDescuento() : BigDecimal.ZERO)
                                .descuentoIgv(v.getIgvDescuento() != null ? v.getIgvDescuento() : BigDecimal.ZERO)
                                .exportacion(BigDecimal.ZERO)
                                .isc(BigDecimal.ZERO)
                                .baseArroz(BigDecimal.ZERO)
                                .igvArroz(BigDecimal.ZERO)
                                .icbper(BigDecimal.ZERO)
                                .otrosConceptos(BigDecimal.ZERO)
                                .total(v.getTotal() != null ? v.getTotal() : BigDecimal.ZERO)
                                .moneda(v.getMoneda() != null ? v.getMoneda() : "PEN")
                                .tipoCambio(v.getTc() != null ? v.getTc() : BigDecimal.ONE)
                                .estado(v.getEstado())
                                .build()).collect(java.util.stream.Collectors.toList());
        }

        @Override
        public List<SireCompraDTO> obtenerSireCompras(String idSucursal, String idPuntoVenta, String inicio,
                        String fin) {
                Long idSuc = Long.parseLong(idSucursal);
                LocalDate start = LocalDate.parse(inicio);
                LocalDate end = LocalDate.parse(fin);

                List<Compra> compras = compraRepository.findSireCompras(idSuc, start, end,
                                org.springframework.data.domain.PageRequest.of(0, 10000)).getContent();

                return compras.stream().map(c -> SireCompraDTO.builder()
                                .periodo(c.getFechaEmision()
                                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM00")))
                                .carSunat("")
                                .fecha(c.getFechaEmision())
                                .fechaVencimiento(c.getFechaVencimiento())
                                .tipoDoc(c.getTipoComprobante())
                                .serie(c.getSerie())
                                .numero(c.getCorrelativo())
                                .numeroFinal("")
                                .nroDocProv(c.getProveedor() != null ? c.getProveedor().getNumDocIdent() : "")
                                .proveedor(c.getProveedor() != null ? c.getProveedor().getRazonSocial() : "")
                                .exportacion(BigDecimal.ZERO)
                                .baseImponible(c.getValorVentaGravado() != null ? c.getValorVentaGravado()
                                                : BigDecimal.ZERO)
                                .descuentoBase(BigDecimal.ZERO)
                                .igv(c.getIgv() != null ? c.getIgv() : BigDecimal.ZERO)
                                .descuentoIgv(c.getIgvDescuento() != null ? c.getIgvDescuento() : BigDecimal.ZERO)
                                .exonerado(c.getValorVentaExonerado() != null ? c.getValorVentaExonerado()
                                                : BigDecimal.ZERO)
                                .inafecto(c.getValorVentaInafecto() != null ? c.getValorVentaInafecto()
                                                : BigDecimal.ZERO)
                                .isc(BigDecimal.ZERO)
                                .baseArroz(BigDecimal.ZERO)
                                .igvArroz(BigDecimal.ZERO)
                                .icbper(BigDecimal.ZERO)
                                .otrosConceptos(c.getOtrosTributos() != null ? c.getOtrosTributos() : BigDecimal.ZERO)
                                .total(c.getTotal() != null ? c.getTotal() : BigDecimal.ZERO)
                                .moneda(c.getMoneda() != null ? c.getMoneda() : "PEN")
                                .tipoCambio(c.getTipoCambio() != null ? c.getTipoCambio() : BigDecimal.ONE)
                                .estado(c.getEstado())
                                .build()).collect(java.util.stream.Collectors.toList());
        }

        @Override
        public EstadisticasDTO obtenerEstadisticasVentas(String idSucursal, String idPuntoVenta, String inicio,
                        String fin) {
                Long idSucursalLong = null;
                try {
                        idSucursalLong = Long.parseLong(idSucursal);
                } catch (NumberFormatException e) {
                }
                Long idPuntoVentaLong = null;
                if (idPuntoVenta != null && !idPuntoVenta.equals("TODOS")) {
                        try {
                                idPuntoVentaLong = Long.parseLong(idPuntoVenta);
                        } catch (NumberFormatException e) {
                        }
                }
                LocalDate start = LocalDate.parse(inicio);
                LocalDate end = LocalDate.parse(fin);
                Object[] basics = (Object[]) ventaRepository.getStatsBasics(idSucursalLong, start, end);
                Long cantidad = basics != null ? (Long) basics[0] : 0L;
                Double promedio = basics != null && basics[1] != null ? (Double) basics[1] : 0.0;
                Long nuevos = ventaRepository.countNuevosClientes(idSucursalLong, start, end);
                Long anulaciones = ventaRepository.countAnulaciones(idSucursalLong, start, end);

                return EstadisticasDTO.builder()
                                .cantidadDocumentos(cantidad)
                                .ticketPromedio(BigDecimal.valueOf(promedio))
                                .nuevosRegistros(nuevos)
                                .anulaciones(anulaciones)
                                .build();
        }

        @Override
        public EstadisticasDTO obtenerEstadisticasCompras(String idSucursal, String idPuntoVenta, String inicio,
                        String fin) {
                Long idSuc = Long.parseLong(idSucursal);
                LocalDate start = LocalDate.parse(inicio);
                LocalDate end = LocalDate.parse(fin);
                Object[] basics = (Object[]) compraRepository.getStatsBasics(idSuc, start, end);
                Long cantidad = basics != null ? (Long) basics[0] : 0L;
                Double promedio = basics != null && basics[1] != null ? (Double) basics[1] : 0.0;
                Long nuevos = compraRepository.countNuevosProveedores(idSuc, start, end);
                Long anulaciones = compraRepository.countAnulaciones(idSuc, start, end);

                return EstadisticasDTO.builder()
                                .cantidadDocumentos(cantidad)
                                .ticketPromedio(BigDecimal.valueOf(promedio))
                                .nuevosRegistros(nuevos)
                                .anulaciones(anulaciones)
                                .build();
        }

        @Override
        public List<ReporteCajaDetalleDTO> obtenerReporteCajas(String idSucursal, String idPuntoVenta, Integer mes,
                        Integer anio) {
                Long idSuc;
                try {
                        idSuc = Long.parseLong(idSucursal);
                } catch (Exception e) {
                        return java.util.Collections.emptyList();
                }

                List<com.pe.articulos.modules.caja_chica.entity.CajaChica> cajas = cajaChicaRepository
                                .findCajasArqueadas(idSuc, mes, anio);

                return cajas.stream()
                                .map(c -> {
                                        BigDecimal saldoTeorico = c.getSaldoActual() != null ? c.getSaldoActual()
                                                        : BigDecimal.ZERO;
                                        BigDecimal saldoReal = c.getSaldoCierreReal() != null ? c.getSaldoCierreReal()
                                                        : BigDecimal.ZERO;
                                        BigDecimal diferencia = saldoReal.subtract(saldoTeorico);

                                        return ReporteCajaDetalleDTO.builder()
                                                        .id(c.getId())
                                                        .nombre(c.getNombre())
                                                        .usuario(c.getIdUsuarioCajero())
                                                        .fechaApertura(c.getCreatedAt() != null
                                                                        ? c.getCreatedAt().toString()
                                                                        : "")
                                                        .fechaCierre(c.getFechaCierre() != null
                                                                        ? c.getFechaCierre().toString()
                                                                        : "")
                                                        .saldoInicial(c.getSaldoInicial() != null ? c.getSaldoInicial()
                                                                        : BigDecimal.ZERO)
                                                        .saldoTeorico(saldoTeorico)
                                                        .saldoReal(saldoReal)
                                                        .diferencia(diferencia)
                                                        .build();
                                })
                                .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public ReporteDescuentoDetalleDTO obtenerReporteDescuentos(String idSucursal, String idPuntoVenta,
                        Integer mes,
                        Integer anio) {
                Long idSucursalLong = null;
                try {
                        idSucursalLong = Long.parseLong(idSucursal);
                } catch (NumberFormatException e) {
                }
                Long idPuntoVentaLong = null;
                if (idPuntoVenta != null && !idPuntoVenta.equals("TODOS")) {
                        try {
                                idPuntoVentaLong = Long.parseLong(idPuntoVenta);
                        } catch (NumberFormatException e) {
                        }
                }
                List<VentaDetalle> detalles = ventaDetalleRepository.findWithDescuentos(idSucursalLong, mes, anio);

                BigDecimal totalDesc = detalles.stream()
                                .map(d -> d.getMontoDescuento() != null ? d.getMontoDescuento() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                List<ReporteDescuentoDetalleDTO.DescuentoItemDTO> items = detalles.stream()
                                .map(d -> ReporteDescuentoDetalleDTO.DescuentoItemDTO.builder()
                                                .fecha(d.getVentaRegistro().getFecha().toString())
                                                .comprobante(d.getVentaRegistro().getSerie() + "-"
                                                                + d.getVentaRegistro().getNumdoc())
                                                .cliente(d.getVentaRegistro().getNombrePac())
                                                .motivo(d.getGlosa())
                                                .base(d.getBaseImp())
                                                .descuento(d.getMontoDescuento())
                                                .total(d.getTotal())
                                                .build())
                                .collect(java.util.stream.Collectors.toList());

                return ReporteDescuentoDetalleDTO.builder()
                                .totalDescuentos(totalDesc)
                                .cantidadVentas((long) items.size())
                                .detalle(items)
                                .build();
        }

        @Override
        public ReporteAnuladoDetalleDTO obtenerComprobantesAnulados(String idSucursal, String idPuntoVenta,
                        Integer mes,
                        Integer anio) {
                return obtenerReporteAnulados(idSucursal, idPuntoVenta, mes, anio);
        }

        @Override
        public ReporteAnuladoDetalleDTO obtenerNotasCredito(String idSucursal, String idPuntoVenta, Integer mes,
                        Integer anio) {
                Long idSucursalLong = null;
                try {
                        idSucursalLong = Long.parseLong(idSucursal);
                } catch (NumberFormatException e) {
                }
                Long idPuntoVentaLong = null;
                if (idPuntoVenta != null && !idPuntoVenta.equals("TODOS")) {
                        try {
                                idPuntoVentaLong = Long.parseLong(idPuntoVenta);
                        } catch (NumberFormatException e) {
                        }
                }
                List<VentaRegistro> notas = ventaRepository.findSireVentas(idSucursalLong,
                                LocalDate.of(anio, mes, 1),
                                LocalDate.of(anio, mes, 1).with(java.time.temporal.TemporalAdjusters.lastDayOfMonth()))
                                .stream().filter(v -> "07".equals(v.getTipoDoc()))
                                .collect(java.util.stream.Collectors.toList());

                BigDecimal total = notas.stream()
                                .map(v -> v.getTotal() != null ? v.getTotal() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                List<ReporteAnuladoDetalleDTO.AnuladoItemDTO> items = notas.stream()
                                .map(v -> ReporteAnuladoDetalleDTO.AnuladoItemDTO.builder()
                                                .idVenta(v.getIdVenta())
                                                .fecha(v.getFecha().toString())
                                                .tipo(v.getTipoDoc() != null ? (v.getTipoDoc().equals("03") ? "BOLETA"
                                                                : (v.getTipoDoc().equals("01") ? "FACTURA"
                                                                                : (v.getTipoDoc().equals("07")
                                                                                                ? "N. CREDITO"
                                                                                                : "TICKET")))
                                                                : "TICKET")
                                                .serie(v.getSerie())
                                                .numero(v.getNumdoc())
                                                .doc(v.getSerie() + "-" + v.getNumdoc())
                                                .cliente(v.getNombrePac())
                                                .motivo(v.getMotivoAnul2())
                                                .monto(v.getTotal())
                                                .usuario(v.getIdPersonalUser())
                                                .build())
                                .collect(java.util.stream.Collectors.toList());

                return ReporteAnuladoDetalleDTO.builder()
                                .cantidadAnulados((long) items.size())
                                .montoTotal(total)
                                .detalle(items)
                                .build();
        }

        @Override
        public String guardarConfiguracionColumnas(String reportKey, Long idUsuario, Long idSucursal,
                        String visibleColumns) {
                ReportColumnConfig config = reportColumnConfigRepository
                                .findByReportKeyAndIdSucursal(reportKey, idSucursal)
                                .orElse(ReportColumnConfig.builder()
                                                .reportKey(reportKey)
                                                .idSucursal(idSucursal)
                                                .build());

                config.setIdUsuario(idUsuario);
                config.setVisibleColumns(visibleColumns);
                reportColumnConfigRepository.save(config);

                return visibleColumns;
        }

        @Override
        public String obtenerConfiguracionColumnas(String reportKey, Long idUsuario, Long idSucursal) {
                return reportColumnConfigRepository.findByReportKeyAndIdSucursal(reportKey, idSucursal)
                                .map(ReportColumnConfig::getVisibleColumns)
                                .orElse(null);
        }

        private ReporteAnuladoDetalleDTO obtenerReporteAnulados(String idSucursal, String idPuntoVenta,
                        Integer mes,
                        Integer anio) {
                Long idSucursalLong = null;
                try {
                        idSucursalLong = Long.parseLong(idSucursal);
                } catch (NumberFormatException e) {
                }
                Long idPuntoVentaLong = null;
                if (idPuntoVenta != null && !idPuntoVenta.equals("TODOS")) {
                        try {
                                idPuntoVentaLong = Long.parseLong(idPuntoVenta);
                        } catch (NumberFormatException e) {
                        }
                }
                LocalDate inicio = LocalDate.of(anio, mes, 1);
                LocalDate fin = inicio.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());

                List<VentaRegistro> anulados = ventaRepository.findSireVentas(idSucursalLong, inicio, fin)
                                .stream()
                                .filter(v -> v.getEstado() == com.pe.articulos.core.enums.EstadoGeneral.ANULADO)
                                .collect(java.util.stream.Collectors.toList());

                BigDecimal total = anulados.stream()
                                .map(v -> v.getTotal() != null ? v.getTotal() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                List<ReporteAnuladoDetalleDTO.AnuladoItemDTO> items = anulados.stream()
                                .map(v -> ReporteAnuladoDetalleDTO.AnuladoItemDTO.builder()
                                                .idVenta(v.getIdVenta())
                                                .fecha(v.getFecha().toString())
                                                .tipo(v.getTipoDoc() != null ? (v.getTipoDoc().equals("03") ? "BOLETA"
                                                                : (v.getTipoDoc().equals("01") ? "FACTURA"
                                                                                : (v.getTipoDoc().equals("07")
                                                                                                ? "N. CREDITO"
                                                                                                : "TICKET")))
                                                                : "TICKET")
                                                .serie(v.getSerie())
                                                .numero(v.getNumdoc())
                                                .doc(v.getSerie() + "-" + v.getNumdoc())
                                                .cliente(v.getNombrePac())
                                                .motivo(v.getMotivoAnul2())
                                                .monto(v.getTotal())
                                                .usuario(v.getIdPersonalUser())
                                                .build())
                                .collect(java.util.stream.Collectors.toList());

                return ReporteAnuladoDetalleDTO.builder()
                                .cantidadAnulados((long) items.size())
                                .montoTotal(total)
                                .detalle(items)
                                .build();
        }

        private List<DataPuntoDTO> mapToDataPuntoDTO(List<Object[]> results) {
                return results.stream()
                                .map(row -> DataPuntoDTO.builder()
                                                .label(row[0] != null ? row[0].toString() : "N/A")
                                                .value(toBigDecimal(row[1]))
                                                .build())
                                .collect(java.util.stream.Collectors.toList());
        }

        private BigDecimal toBigDecimal(Object value) {
                if (value == null)
                        return BigDecimal.ZERO;
                if (value instanceof BigDecimal)
                        return (BigDecimal) value;
                if (value instanceof Number)
                        return new BigDecimal(value.toString());
                return BigDecimal.ZERO;
        }
}

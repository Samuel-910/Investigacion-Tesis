package com.pe.articulos.modules.reportes.service.strategy;

import com.pe.articulos.modules.reportes.entity.DashboardWidget.DimensionReporte;
import com.pe.articulos.modules.reportes.entity.DashboardWidget.MetricaReporte;
import com.pe.articulos.modules.venta_registro.repository.VentaRegistroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteVentasStrategy implements ReporteStrategy {

    private final VentaRegistroRepository ventaRepository;

    @Override
    public boolean supports(MetricaReporte metrica) {
        return metrica == MetricaReporte.VENTAS || metrica == MetricaReporte.VENTAS_CANTIDAD ||
               metrica == MetricaReporte.UTILIDAD_NETA || metrica == MetricaReporte.TICKET_PROMEDIO ||
               metrica == MetricaReporte.DESCUENTOS_TOTALES || metrica == MetricaReporte.RENTABILIDAD ||
               metrica == MetricaReporte.ARTICULOS_PERDIDA;
    }

    @Override
    public List<Object[]> obtenerDatosRaw(MetricaReporte metrica, DimensionReporte dimension, Long idSucursal) {
        if (metrica == MetricaReporte.VENTAS) {
            if (dimension == DimensionReporte.TIEMPO_MES) {
                return ventaRepository.ventasMensualesRaw(idSucursal, null);
            } else if (dimension == DimensionReporte.TIEMPO_DIA) {
                return ventaRepository.ventasDiariasRaw(idSucursal, LocalDate.now().minusDays(30));
            } else if (dimension == DimensionReporte.USUARIO_REGISTRO) {
                return ventaRepository.ventasPorUsuarioRaw(idSucursal);
            } else if (dimension == DimensionReporte.PRODUCTO_NOMBRE) {
                return ventaRepository.topProductosVendidosRaw(idSucursal, null,
                        org.springframework.data.domain.PageRequest.of(0, 15));
            } else if (dimension == DimensionReporte.METODO_PAGO) {
                return ventaRepository.ventasPorMetodoPagoRaw(idSucursal, org.springframework.data.domain.PageRequest.of(0, 15));
            } else if (dimension == DimensionReporte.CLIENTE) {
                return ventaRepository.ventasPorClienteRaw(idSucursal, org.springframework.data.domain.PageRequest.of(0, 15));
            } else if (dimension == DimensionReporte.DIA_SEMANA) {
                return ventaRepository.ventasPorDiaSemanaRaw(idSucursal);
            } else if (dimension == DimensionReporte.MARCA) {
                return ventaRepository.ventasPorMarcaRaw(idSucursal, org.springframework.data.domain.PageRequest.of(0, 15));
            }
        } else if (metrica == MetricaReporte.VENTAS_CANTIDAD) {
            if (dimension == DimensionReporte.TIEMPO_DIA) {
                return ventaRepository.cantidadVentasDiariasRaw(idSucursal, LocalDate.now().minusDays(30));
            }
        } else if (metrica == MetricaReporte.UTILIDAD_NETA) {
            return java.util.Collections.singletonList(new Object[]{"Utilidad Neta", ventaRepository.utilidadNetaVentas(idSucursal)});
        } else if (metrica == MetricaReporte.TICKET_PROMEDIO) {
            return java.util.Collections.singletonList(new Object[]{"Ticket Promedio", ventaRepository.ticketPromedioVentas(idSucursal)});
        } else if (metrica == MetricaReporte.DESCUENTOS_TOTALES) {
            return java.util.Collections.singletonList(new Object[]{"Descuentos Totales", ventaRepository.descuentosTotalesVentas(idSucursal)});
        } else if (metrica == MetricaReporte.RENTABILIDAD) {
            return java.util.Collections.singletonList(new Object[]{"Rentabilidad (%)", ventaRepository.rentabilidadVentas(idSucursal)});
        } else if (metrica == MetricaReporte.ARTICULOS_PERDIDA) {
            return ventaRepository.articulosConPerdidaRaw(idSucursal, org.springframework.data.domain.PageRequest.of(0, 15));
        }
        return new ArrayList<>();
    }
}

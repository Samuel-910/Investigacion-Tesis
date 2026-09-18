package com.pe.articulos.modules.reportes.service.strategy;

import com.pe.articulos.modules.caja_chica.repository.CajaChicaMovimientoRepository;
import com.pe.articulos.modules.reportes.entity.DashboardWidget.DimensionReporte;
import com.pe.articulos.modules.reportes.entity.DashboardWidget.MetricaReporte;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteCajaStrategy implements ReporteStrategy {

    private final CajaChicaMovimientoRepository cajaRepository;

    @Override
    public boolean supports(MetricaReporte metrica) {
        return metrica == MetricaReporte.INGRESOS 
            || metrica == MetricaReporte.GASTOS 
            || metrica == MetricaReporte.SALDO_CAJA;
    }

    @Override
    public List<Object[]> obtenerDatosRaw(MetricaReporte metrica, DimensionReporte dimension, Long idSucursal) {
        if (idSucursal == null) return new ArrayList<>();

        if (metrica == MetricaReporte.INGRESOS) {
            return cajaRepository.ingresosDiariosRaw(idSucursal);
        } else if (metrica == MetricaReporte.GASTOS) {
            if (dimension == DimensionReporte.CONCEPTO_GASTO) {
                return cajaRepository.gastosPorConceptoRaw(idSucursal, org.springframework.data.domain.PageRequest.of(0, 15)).getContent();
            }
            return cajaRepository.egresosDiariosRaw(idSucursal);
        } else if (metrica == MetricaReporte.SALDO_CAJA) {
            return cajaRepository.movimientosPorMetodoPagoRaw(idSucursal);
        }
        return new ArrayList<>();
    }
}

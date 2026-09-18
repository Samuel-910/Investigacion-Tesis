package com.pe.articulos.modules.reportes.service.strategy;

import com.pe.articulos.modules.proveedores.repository.CuentaProveedorRepository;
import com.pe.articulos.modules.reportes.entity.DashboardWidget.DimensionReporte;
import com.pe.articulos.modules.reportes.entity.DashboardWidget.MetricaReporte;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteProveedoresStrategy implements ReporteStrategy {

    private final CuentaProveedorRepository cuentaProveedorRepository;

    @Override
    public boolean supports(MetricaReporte metrica) {
        return metrica == MetricaReporte.DEUDA;
    }

    @Override
    public List<Object[]> obtenerDatosRaw(MetricaReporte metrica, DimensionReporte dimension, Long idSucursal) {
        if (metrica == MetricaReporte.DEUDA) {
            if (dimension == DimensionReporte.PROVEEDOR) {
                return cuentaProveedorRepository.deudasPorProveedorRaw();
            }
        }
        return new ArrayList<>();
    }
}

package com.pe.articulos.modules.reportes.service.strategy;

import com.pe.articulos.modules.compras.repository.CompraRepository;
import com.pe.articulos.modules.reportes.entity.DashboardWidget.DimensionReporte;
import com.pe.articulos.modules.reportes.entity.DashboardWidget.MetricaReporte;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteComprasStrategy implements ReporteStrategy {

    private final CompraRepository compraRepository;

    @Override
    public boolean supports(MetricaReporte metrica) {
        return metrica == MetricaReporte.COMPRAS || metrica == MetricaReporte.COMPRAS_CANTIDAD || metrica == MetricaReporte.TICKET_COMPRA_PROMEDIO;
    }

    @Override
    public List<Object[]> obtenerDatosRaw(MetricaReporte metrica, DimensionReporte dimension, Long idSucursal) {
        if (metrica == MetricaReporte.COMPRAS) {
            if (dimension == DimensionReporte.TIEMPO_MES) {
                return compraRepository.comprasMensualesRaw(idSucursal, org.springframework.data.domain.PageRequest.of(0, 100))
                        .getContent();
            } else if (dimension == DimensionReporte.TIEMPO_DIA) {
                return compraRepository.comprasDiariasRaw(idSucursal, LocalDate.now().minusDays(30),
                        org.springframework.data.domain.PageRequest.of(0, 100)).getContent();
            } else if (dimension == DimensionReporte.PROVEEDOR) {
                return compraRepository.comprasPorProveedorRaw(idSucursal,
                        org.springframework.data.domain.PageRequest.of(0, 15)).getContent();
            } else if (dimension == DimensionReporte.SUCURSAL) {
                return compraRepository.comprasPorSucursalRaw(org.springframework.data.domain.PageRequest.of(0, 10))
                        .getContent();
            } else if (dimension == DimensionReporte.USUARIO_REGISTRO) {
                return compraRepository.comprasPorUsuarioRaw(idSucursal,
                        org.springframework.data.domain.PageRequest.of(0, 15)).getContent();
            } else if (dimension == DimensionReporte.CATEGORIA) {
                return compraRepository.comprasPorCategoriaRaw(idSucursal, org.springframework.data.domain.PageRequest.of(0, 15)).getContent();
            }
        } else if (metrica == MetricaReporte.COMPRAS_CANTIDAD) {
            if (dimension == DimensionReporte.TIEMPO_MES) {
                return compraRepository.cantidadComprasMensualesRaw(idSucursal,
                        org.springframework.data.domain.PageRequest.of(0, 100)).getContent();
            }
        } else if (metrica == MetricaReporte.TICKET_COMPRA_PROMEDIO) {
            return java.util.Collections.singletonList(new Object[]{"Ticket Compra", compraRepository.ticketCompraPromedioRaw(idSucursal)});
        }
        return new ArrayList<>();
    }
}

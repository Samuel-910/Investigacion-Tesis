package com.pe.articulos.modules.reportes.service.strategy;

import com.pe.articulos.modules.productos.repository.ProductoRepository;
import com.pe.articulos.modules.reportes.entity.DashboardWidget.DimensionReporte;
import com.pe.articulos.modules.reportes.entity.DashboardWidget.MetricaReporte;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteProductosStrategy implements ReporteStrategy {

    private final ProductoRepository productoRepository;

    @Override
    public boolean supports(MetricaReporte metrica) {
        return metrica == MetricaReporte.STOCK_FISICO 
            || metrica == MetricaReporte.VALOR_INVENTARIO 
            || metrica == MetricaReporte.PRODUCTOS_VENCIDOS
            || metrica == MetricaReporte.STOCK_CRITICO;
    }

    @Override
    public List<Object[]> obtenerDatosRaw(MetricaReporte metrica, DimensionReporte dimension, Long idSucursal) {
        if (idSucursal == null) return new ArrayList<>();

        if (metrica == MetricaReporte.STOCK_FISICO) {
            if (dimension == DimensionReporte.CATEGORIA) {
                return productoRepository.stockPorCategoriaRaw(idSucursal);
            } else if (dimension == DimensionReporte.MARCA) {
                return productoRepository.inventarioPorMarcaRaw(idSucursal, org.springframework.data.domain.PageRequest.of(0, 15)).getContent();
            }
        } else if (metrica == MetricaReporte.VALOR_INVENTARIO) {
            if (dimension == DimensionReporte.CATEGORIA) {
                return productoRepository.valorInventarioPorCategoriaRaw(idSucursal);
            } else if (dimension == DimensionReporte.MARCA) {
                return productoRepository.inventarioPorMarcaRaw(idSucursal, org.springframework.data.domain.PageRequest.of(0, 15)).getContent();
            }
        } else if (metrica == MetricaReporte.PRODUCTOS_VENCIDOS) {
            return productoRepository.productosVencidosPorLaboratorioRaw(idSucursal);
        } else if (metrica == MetricaReporte.STOCK_CRITICO) {
            return java.util.Collections.singletonList(new Object[]{"Stock Crítico", productoRepository.contarStockCritico(idSucursal)});
        }
        return new ArrayList<>();
    }
}

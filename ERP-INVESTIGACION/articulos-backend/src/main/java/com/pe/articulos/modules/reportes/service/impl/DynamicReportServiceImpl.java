package com.pe.articulos.modules.reportes.service.impl;

import com.pe.articulos.modules.compras.repository.CompraRepository;
import com.pe.articulos.modules.reportes.dto.DataPuntoDTO;
import com.pe.articulos.modules.reportes.dto.MultiseriesDataDTO;
import com.pe.articulos.modules.reportes.dto.SeriesDataDTO;
import com.pe.articulos.modules.reportes.entity.DashboardWidget.*;
import com.pe.articulos.modules.reportes.service.DynamicReportService;
import com.pe.articulos.modules.reportes.service.strategy.ReporteStrategy;
import com.pe.articulos.modules.venta_registro.repository.VentaRegistroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DynamicReportServiceImpl implements DynamicReportService {

    private final List<ReporteStrategy> strategies;
    private final VentaRegistroRepository ventaRepository;
    private final CompraRepository compraRepository;

    @Override
    public List<DataPuntoDTO> obtenerDatos(MetricaReporte metrica, DimensionReporte dimension, String idSucursal) {
        Long idSucursalLong = null;
        try {
            idSucursalLong = Long.parseLong(idSucursal);
        } catch (Exception e) {
        }

        List<Object[]> rawData = new ArrayList<>();

        for (ReporteStrategy strategy : strategies) {
            if (strategy.supports(metrica)) {
                rawData = strategy.obtenerDatosRaw(metrica, dimension, idSucursalLong);
                break;
            }
        }

        return mapToDataPuntoDTO(rawData);
    }

    @Override
    public MultiseriesDataDTO obtenerDatosMultiseries(MetricaReporte metrica, DimensionReporte dimension,
            String idSucursal) {

        Long idSucursalLong = null;
        try {
            idSucursalLong = Long.parseLong(idSucursal);
        } catch (Exception e) {
        }

        if (dimension == DimensionReporte.TIEMPO_MES) {
            List<Object[]> ventasRaw = ventaRepository.ventasMensualesRaw(idSucursalLong, null);
            List<Object[]> comprasRaw = (idSucursalLong != null)
                    ? compraRepository
                            .comprasMensualesRaw(idSucursalLong, org.springframework.data.domain.PageRequest.of(0, 100))
                            .getContent()
                    : new ArrayList<>();

            Map<String, BigDecimal> ventasMap = ventasRaw.stream()
                    .collect(Collectors.toMap(r -> r[0].toString(), r -> toBigDecimal(r[1]), (v1, v2) -> v1,
                            TreeMap::new));
            Map<String, BigDecimal> comprasMap = comprasRaw.stream()
                    .collect(Collectors.toMap(r -> r[0].toString(), r -> toBigDecimal(r[1]), (v1, v2) -> v1,
                            TreeMap::new));

            List<String> labels = new ArrayList<>(ventasMap.keySet());
            comprasMap.keySet().forEach(k -> {
                if (!labels.contains(k))
                    labels.add(k);
            });
            labels.sort(null);

            List<BigDecimal> dataVentas = labels.stream().map(l -> ventasMap.getOrDefault(l, BigDecimal.ZERO))
                    .collect(Collectors.toList());
            List<BigDecimal> dataCompras = labels.stream().map(l -> comprasMap.getOrDefault(l, BigDecimal.ZERO))
                    .collect(Collectors.toList());

            return MultiseriesDataDTO.builder()
                    .labels(labels)
                    .series(Arrays.asList(
                            new SeriesDataDTO("Ventas", dataVentas),
                            new SeriesDataDTO("Compras", dataCompras)))
                    .build();
        }

        throw new IllegalArgumentException("Métrica o Dimensión no soportada para multiserie");
    }

    private List<DataPuntoDTO> mapToDataPuntoDTO(List<Object[]> results) {
        return results.stream()
                .map(row -> DataPuntoDTO.builder()
                        .label(row[0] != null ? row[0].toString() : "N/A")
                        .value(toBigDecimal(row[1]))
                        .build())
                .collect(Collectors.toList());
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

package com.pe.articulos.modules.reportes.service.impl;

import com.pe.articulos.modules.reportes.dto.DashboardSaveRequestDTO;
import com.pe.articulos.modules.reportes.entity.DashboardConfig;
import com.pe.articulos.modules.reportes.entity.DashboardWidget;
import com.pe.articulos.modules.reportes.entity.DashboardWidget.DimensionReporte;
import com.pe.articulos.modules.reportes.entity.DashboardWidget.MetricaReporte;
import com.pe.articulos.modules.reportes.entity.DashboardWidget.TipoGrafico;
import com.pe.articulos.modules.reportes.repository.DashboardConfigRepository;
import com.pe.articulos.modules.reportes.service.DashboardConfigService;
import com.pe.articulos.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class DashboardConfigServiceImpl implements DashboardConfigService {

        private final DashboardConfigRepository configRepository;
        private final ObjectMapper objectMapper;

        @Override
        @Transactional
        public DashboardConfig guardarConfiguracion(DashboardSaveRequestDTO request, Long idUsuario,
                        Long idSucursal, String categoria) {
                configRepository.findFirstByIdSucursalAndCategoriaAndActivoTrue(idSucursal, categoria)
                                .ifPresent(c -> {
                                        c.setActivo(false);
                                        configRepository.save(c);
                                });

                DashboardConfig config = DashboardConfig.builder()
                                .nombre(request.getNombre())
                                .idUsuario(idUsuario)
                                .idSucursal(idSucursal)
                                .categoria(categoria)
                                .activo(true)
                                .widgets(new ArrayList<>())
                                .build();

                config.setWidgets(request.getWidgets().stream()
                                .map(dto -> DashboardWidget.builder()
                                                .titulo(dto.getTitulo())
                                                .tipoGrafico(dto.getTipoGrafico())
                                                .metrica(dto.getMetrica())
                                                .dimension(dto.getDimension())
                                                .orden(dto.getOrden())
                                                .columns(dto.getColumns())
                                                .height(dto.getHeight())
                                                .dashboardConfig(config)
                                                .build())
                                .collect(Collectors.toList()));

                return configRepository.save(config);
        }

        @Override
        @Transactional
        public DashboardConfig obtenerConfiguracionActual(Long idUsuario, Long idSucursal, String categoria) {
                return configRepository
                                .findFirstByIdSucursalAndCategoriaAndActivoTrue(idSucursal, categoria)
                                .map(config -> {
                                        if (config.getWidgets() == null || config.getWidgets().size() <= 10) {
                                                config.setActivo(false);
                                                configRepository.save(config);
                                                return generarDashboardPorDefecto(idUsuario, idSucursal, categoria);
                                        }
                                        return config;
                                })
                                .orElseGet(() -> generarDashboardPorDefecto(idUsuario, idSucursal, categoria));
        }

        private DashboardConfig generarDashboardPorDefecto(Long idUsuario, Long idSucursal, String categoria) {
                DashboardConfig config = DashboardConfig.builder()
                                .nombre("Dashboard " + categoria)
                                .idUsuario(idUsuario)
                                .idSucursal(idSucursal)
                                .categoria(categoria)
                                .activo(true)
                                .widgets(new ArrayList<>())
                                .build();

                List<DashboardWidget> widgets = new ArrayList<>();
                int orden = 0;

                if ("VENTAS".equalsIgnoreCase(categoria)) {
                        widgets.add(DashboardWidget.builder().titulo("Ganancias (Utilidad Neta)").tipoGrafico(TipoGrafico.CARD)
                                        .metrica(MetricaReporte.UTILIDAD_NETA).orden(orden++).columns(3).height(1)
                                        .dashboardConfig(config).build());
                        widgets.add(DashboardWidget.builder().titulo("Rentabilidad").tipoGrafico(TipoGrafico.CARD)
                                        .metrica(MetricaReporte.RENTABILIDAD).orden(orden++).columns(3).height(1)
                                        .dashboardConfig(config).build());
                        widgets.add(DashboardWidget.builder().titulo("Ticket Promedio").tipoGrafico(TipoGrafico.CARD)
                                        .metrica(MetricaReporte.TICKET_PROMEDIO).orden(orden++).columns(3).height(1)
                                        .dashboardConfig(config).build());
                        widgets.add(DashboardWidget.builder().titulo("Descuentos Totales").tipoGrafico(TipoGrafico.CARD)
                                        .metrica(MetricaReporte.DESCUENTOS_TOTALES).orden(orden++).columns(3).height(1)
                                        .dashboardConfig(config).build());

                        widgets.add(DashboardWidget.builder().titulo("Ventas por Día").tipoGrafico(TipoGrafico.AREA)
                                        .metrica(MetricaReporte.VENTAS).dimension(DimensionReporte.TIEMPO_DIA)
                                        .orden(orden++).columns(8).height(2).dashboardConfig(config).build());
                        widgets.add(DashboardWidget.builder().titulo("Método de Pago").tipoGrafico(TipoGrafico.DONUT)
                                        .metrica(MetricaReporte.VENTAS).dimension(DimensionReporte.METODO_PAGO)
                                        .orden(orden++).columns(4).height(2).dashboardConfig(config).build());

                        widgets.add(DashboardWidget.builder().titulo("Día de la Semana").tipoGrafico(TipoGrafico.COLUMN)
                                        .metrica(MetricaReporte.VENTAS).dimension(DimensionReporte.DIA_SEMANA)
                                        .orden(orden++).columns(6).height(2).dashboardConfig(config).build());
                        widgets.add(DashboardWidget.builder().titulo("Top Productos").tipoGrafico(TipoGrafico.BAR)
                                        .metrica(MetricaReporte.VENTAS).dimension(DimensionReporte.PRODUCTO_NOMBRE)
                                        .orden(orden++).columns(6).height(2).dashboardConfig(config).build());

                        widgets.add(DashboardWidget.builder().titulo("Top Clientes").tipoGrafico(TipoGrafico.TABLE)
                                        .metrica(MetricaReporte.VENTAS).dimension(DimensionReporte.CLIENTE)
                                        .orden(orden++).columns(6).height(2).dashboardConfig(config).build());
                        widgets.add(DashboardWidget.builder().titulo("Ventas por Marca").tipoGrafico(TipoGrafico.BAR)
                                        .metrica(MetricaReporte.VENTAS).dimension(DimensionReporte.MARCA).orden(orden++)
                                        .columns(6).height(2).dashboardConfig(config).build());
                        
                        widgets.add(DashboardWidget.builder().titulo("Productos de Mayor Rotación").tipoGrafico(TipoGrafico.BAR)
                                        .metrica(MetricaReporte.VENTAS_CANTIDAD).dimension(DimensionReporte.PRODUCTO_NOMBRE)
                                        .orden(orden++).columns(6).height(2).dashboardConfig(config).build());
                        
                        widgets.add(DashboardWidget.builder().titulo("Artículos con Pérdidas").tipoGrafico(TipoGrafico.TABLE)
                                        .metrica(MetricaReporte.ARTICULOS_PERDIDA)
                                        .orden(orden++).columns(6).height(2).dashboardConfig(config).build());
                        
                        widgets.add(DashboardWidget.builder().titulo("Alerta: Artículos por Vencer").tipoGrafico(TipoGrafico.TABLE)
                                        .metrica(MetricaReporte.PRODUCTOS_VENCIDOS)
                                        .orden(orden++).columns(12).height(2).dashboardConfig(config).build());

                } else if ("COMPRAS".equalsIgnoreCase(categoria)) {
                        widgets.add(DashboardWidget.builder().titulo("Ticket Compra").tipoGrafico(TipoGrafico.CARD)
                                        .metrica(MetricaReporte.TICKET_COMPRA_PROMEDIO).orden(orden++).columns(4)
                                        .height(1).dashboardConfig(config).build());

                        widgets.add(DashboardWidget.builder().titulo("Compras Mensuales").tipoGrafico(TipoGrafico.AREA)
                                        .metrica(MetricaReporte.COMPRAS).dimension(DimensionReporte.TIEMPO_MES)
                                        .orden(orden++).columns(8).height(2).dashboardConfig(config).build());
                        widgets.add(DashboardWidget.builder().titulo("Categorías de Gasto")
                                        .tipoGrafico(TipoGrafico.DONUT).metrica(MetricaReporte.COMPRAS)
                                        .dimension(DimensionReporte.CATEGORIA).orden(orden++).columns(4).height(2)
                                        .dashboardConfig(config).build());

                        widgets.add(DashboardWidget.builder().titulo("Compras Diarias").tipoGrafico(TipoGrafico.LINE)
                                        .metrica(MetricaReporte.COMPRAS).dimension(DimensionReporte.TIEMPO_DIA)
                                        .orden(orden++).columns(6).height(2).dashboardConfig(config).build());
                        widgets.add(DashboardWidget.builder().titulo("Top Proveedores").tipoGrafico(TipoGrafico.BAR)
                                        .metrica(MetricaReporte.COMPRAS).dimension(DimensionReporte.PROVEEDOR)
                                        .orden(orden++).columns(6).height(2).dashboardConfig(config).build());

                } else if ("PRODUCTOS".equalsIgnoreCase(categoria)) {
                        widgets.add(DashboardWidget.builder().titulo("Stock Crítico").tipoGrafico(TipoGrafico.CARD)
                                        .metrica(MetricaReporte.STOCK_CRITICO).orden(orden++).columns(4).height(1)
                                        .dashboardConfig(config).build());

                        widgets.add(DashboardWidget.builder().titulo("Valor de Inventario por Marca")
                                        .tipoGrafico(TipoGrafico.BAR).metrica(MetricaReporte.VALOR_INVENTARIO)
                                        .dimension(DimensionReporte.MARCA).orden(orden++).columns(6).height(2)
                                        .dashboardConfig(config).build());
                        widgets.add(DashboardWidget.builder().titulo("Stock Físico por Categoría")
                                        .tipoGrafico(TipoGrafico.PIE).metrica(MetricaReporte.STOCK_FISICO)
                                        .dimension(DimensionReporte.CATEGORIA).orden(orden++).columns(6).height(2)
                                        .dashboardConfig(config).build());

                        widgets.add(DashboardWidget.builder().titulo("Productos Vencidos (o por vencer)")
                                        .tipoGrafico(TipoGrafico.TABLE).metrica(MetricaReporte.PRODUCTOS_VENCIDOS)
                                        .orden(orden++).columns(12).height(2).dashboardConfig(config).build());

                } else if ("FINANZAS".equalsIgnoreCase(categoria) || "CAJA".equalsIgnoreCase(categoria) || "GASTOS".equalsIgnoreCase(categoria)) {
                        widgets.add(DashboardWidget.builder().titulo("Ingresos Diarios").tipoGrafico(TipoGrafico.LINE).metrica(MetricaReporte.INGRESOS).orden(orden++).columns(6).height(2).dashboardConfig(config).build());
                        widgets.add(DashboardWidget.builder().titulo("Gastos Diarios").tipoGrafico(TipoGrafico.LINE).metrica(MetricaReporte.GASTOS).orden(orden++).columns(6).height(2).dashboardConfig(config).build());
                        
                        widgets.add(DashboardWidget.builder().titulo("Gastos por Concepto").tipoGrafico(TipoGrafico.DONUT).metrica(MetricaReporte.GASTOS).dimension(DimensionReporte.CONCEPTO_GASTO).orden(orden++).columns(6).height(2).dashboardConfig(config).build());
                        widgets.add(DashboardWidget.builder().titulo("Saldo de Caja (Movimientos)").tipoGrafico(TipoGrafico.COLUMN).metrica(MetricaReporte.SALDO_CAJA).orden(orden++).columns(6).height(2).dashboardConfig(config).build());
                } else {
                        // General fallback
                        widgets.add(DashboardWidget.builder().titulo("Ventas vs Compras").tipoGrafico(TipoGrafico.BAR)
                                        .metrica(MetricaReporte.VENTAS_VS_COMPRAS)
                                        .dimension(DimensionReporte.TIEMPO_MES).orden(orden++).columns(12).height(2)
                                        .dashboardConfig(config).build());
                }

                config.setWidgets(widgets);
                return configRepository.save(config);
        }

        @Override
        public DashboardConfig obtenerPorId(Long id) {
                return configRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No se encontró la configuración con ID: " + id));
        }

        @Override
        public List<String> obtenerCategorias(Long idUsuario, Long idSucursal) {
                return configRepository.findDistinctCategoriaByIdSucursalAndActivoTrue(idSucursal);
        }

        @Override
        @Transactional
        public void inicializarParaSucursal(Long idSucursal) {
                try {
                        InputStream is = new ClassPathResource("data/seed/sucursal/dashboard_config.json").getInputStream();
                        List<DashboardConfig> configs = objectMapper.readValue(is, new TypeReference<List<DashboardConfig>>() {});
                        
                        List<DashboardConfig> configsToSave = new ArrayList<>();
                        for (DashboardConfig config : configs) {
                                // Filter to only copy the base default dashboards (usually those with idUsuario = null or specific user, and active)
                                if (config.isActivo()) {
                                        DashboardConfig newConfig = DashboardConfig.builder()
                                                        .nombre(config.getNombre())
                                                        .idUsuario(config.getIdUsuario()) // Mantener el idUsuario si es específico o null si es general
                                                        .idSucursal(idSucursal)
                                                        .categoria(config.getCategoria())
                                                        .activo(true)
                                                        .widgets(new ArrayList<>())
                                                        .build();

                                        if (config.getWidgets() != null) {
                                                for (DashboardWidget w : config.getWidgets()) {
                                                        DashboardWidget nw = DashboardWidget.builder()
                                                                        .titulo(w.getTitulo())
                                                                        .tipoGrafico(w.getTipoGrafico())
                                                                        .metrica(w.getMetrica())
                                                                        .dimension(w.getDimension())
                                                                        .orden(w.getOrden())
                                                                        .columns(w.getColumns())
                                                                        .height(w.getHeight())
                                                                        .dashboardConfig(newConfig)
                                                                        .build();
                                                        newConfig.getWidgets().add(nw);
                                                }
                                        }
                                        configsToSave.add(newConfig);
                                }
                        }
                        
                        if (!configsToSave.isEmpty()) {
                                configRepository.saveAll(configsToSave);
                                log.info("Se crearon {} dashboards iniciales para la sucursal {}", configsToSave.size(), idSucursal);
                        }
                } catch (Exception e) {
                        log.error("No se pudieron inicializar los dashboards para la sucursal {}", idSucursal, e);
                }
        }
}

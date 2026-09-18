package com.pe.articulos.modules.notificaciones.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.pe.articulos.modules.productos.repository.ProductoRepository;
import com.pe.articulos.modules.productos.entity.Producto;
import com.pe.articulos.modules.compras.repository.CompraRepository;
import com.pe.articulos.modules.compras.entity.Compra;
import com.pe.articulos.core.enums.EstadoGeneral;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.math.BigDecimal;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AlertaSchedulerService {

    private final NotificacionService notificacionService;
    private final ProductoRepository productoRepository;
    private final CompraRepository compraRepository;

    // Se ejecuta todos los días a medianoche (00:00:00)
    // Para pruebas puedes usar cron = "0 * * * * ?" (cada minuto)
    @Scheduled(cron = "0 0 0 * * ?")
    public void generarAlertasDiarias() {
        log.info("Iniciando generación automática de alertas diarias...");
        
        generarAlertasVencimientoProductos();
        generarAlertasStockCritico();
        generarAlertasVencimientoCompras();
        
        log.info("Generación de alertas finalizada.");
    }

    public void generarAlertasParaSucursal(Long idSucursal) {
        if (idSucursal == null) return;
        log.info("Iniciando generación de alertas para sucursal {}", idSucursal);
        generarAlertasVencimientoProductosPorSucursal(idSucursal);
        generarAlertasStockCriticoPorSucursal(idSucursal);
        // Podrías añadir generarAlertasVencimientoComprasPorSucursal si es necesario
    }

    private void generarAlertasVencimientoProductosPorSucursal(Long idSucursal) {
        LocalDate hoy = LocalDate.now();
        log.info("🔍 Buscando alertas de vencimiento para la sucursal: {} con fecha actual: {}", idSucursal, hoy);

        List<Producto> productos = productoRepository.findProductosEnAlertaVencimientoPorSucursal(idSucursal, hoy);
        log.info("📊 Productos encontrados en rango de alerta: {}", productos.size());
        
        for (Producto p : productos) {
            long diasRestantes = ChronoUnit.DAYS.between(hoy, p.getFechaVencimiento());
            int diasAlerta = p.getDiasAlertaVencimiento() != null && p.getDiasAlertaVencimiento() > 0 ? p.getDiasAlertaVencimiento() : 90;
            double porcentaje = ((double) diasRestantes / diasAlerta) * 100;
            
            log.info("⚙️ Evaluando Producto ID: {} | Lote: {} | Faltan {} días | Alerta configurada: {} días | Porcentaje: {}%", 
                    p.getIdProducto(), p.getNroLote(), diasRestantes, diasAlerta, String.format("%.2f", porcentaje));
            
            String tipoNotificacion = porcentaje <= 30.0 ? "PRODUCTO_VENCE_CRITICO" : "PRODUCTO_PRONTO_VENCE";
            String titulo = porcentaje <= 30.0 ? "Producto en estado crítico de vencimiento" : "Producto pronto a vencer";
            String msj = String.format("El producto '%s' (Lote: %s) vence el %s (faltan %d días).",
                    p.getCatalogo() != null ? p.getCatalogo().getNombre() : "N/A",
                    p.getNroLote() != null ? p.getNroLote() : "S/L",
                    p.getFechaVencimiento().toString(),
                    diasRestantes);

            log.info("✅ Generando notificación: [{}] para el producto {}", tipoNotificacion, p.getIdProducto());
            notificacionService.crearNotificacion(
                    tipoNotificacion,
                    titulo,
                    msj,
                    "PROD_" + p.getIdProducto(),
                    p.getIdSucursal()
            );
        }
        log.info("🏁 Fin de generación de alertas de vencimiento para la sucursal {}", idSucursal);
    }

    private void generarAlertasStockCriticoPorSucursal(Long idSucursal) {
        List<Producto> productos = productoRepository.findStockCriticoPorSucursal(idSucursal);
        
        for (Producto p : productos) {
            String titulo = "Stock Crítico";
            String msj = String.format("El producto '%s' tiene un stock actual de %s, el cual es menor o igual al mínimo permitido (%s).",
                    p.getCatalogo() != null ? p.getCatalogo().getNombre() : "N/A",
                    p.getStock() != null ? p.getStock().stripTrailingZeros().toPlainString() : "0",
                    p.getStockMinimo() != null ? p.getStockMinimo().stripTrailingZeros().toPlainString() : "0");

            notificacionService.crearNotificacion(
                    "STOCK_CRITICO",
                    titulo,
                    msj,
                    "STOCK_" + p.getIdProducto(),
                    p.getIdSucursal()
            );
        }
    }

    private void generarAlertasVencimientoProductos() {
        LocalDate hoy = LocalDate.now();

        List<Producto> productos = productoRepository.findProductosEnAlertaVencimientoGlobal(hoy);
        
        for (Producto p : productos) {
            long diasRestantes = ChronoUnit.DAYS.between(hoy, p.getFechaVencimiento());
            int diasAlerta = p.getDiasAlertaVencimiento() != null && p.getDiasAlertaVencimiento() > 0 ? p.getDiasAlertaVencimiento() : 90;
            double porcentaje = ((double) diasRestantes / diasAlerta) * 100;
            
            String tipoNotificacion = porcentaje <= 30.0 ? "PRODUCTO_VENCE_CRITICO" : "PRODUCTO_PRONTO_VENCE";
            String titulo = porcentaje <= 30.0 ? "Producto en estado crítico de vencimiento" : "Producto pronto a vencer";
            String msj = String.format("El producto '%s' (Lote: %s) vence el %s (faltan %d días).",
                    p.getCatalogo() != null ? p.getCatalogo().getNombre() : "N/A",
                    p.getNroLote() != null ? p.getNroLote() : "S/L",
                    p.getFechaVencimiento().toString(),
                    diasRestantes);

            notificacionService.crearNotificacion(
                    tipoNotificacion,
                    titulo,
                    msj,
                    "PROD_" + p.getIdProducto(),
                    p.getIdSucursal()
            );
        }
    }

    private void generarAlertasStockCritico() {
        List<Producto> productos = productoRepository.findStockCriticoGlobal();
        
        for (Producto p : productos) {
            String titulo = "Stock Crítico";
            String msj = String.format("El producto '%s' tiene un stock actual de %s, el cual es menor o igual al mínimo permitido (%s).",
                    p.getCatalogo() != null ? p.getCatalogo().getNombre() : "N/A",
                    p.getStock() != null ? p.getStock().stripTrailingZeros().toPlainString() : "0",
                    p.getStockMinimo() != null ? p.getStockMinimo().stripTrailingZeros().toPlainString() : "0");

            notificacionService.crearNotificacion(
                    "STOCK_CRITICO",
                    titulo,
                    msj,
                    "STOCK_" + p.getIdProducto(),
                    p.getIdSucursal()
            );
        }
    }

    private void generarAlertasVencimientoCompras() {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(7); // Alertar 7 días antes del pago

        // Buscar compras a crédito o por pagar que estén vigentes
        List<Compra> compras = compraRepository.findComprasPorVencerGlobal(hoy, limite, EstadoGeneral.VIGENTE);
        
        for (Compra c : compras) {
            String titulo = "Pago a Proveedor Próximo a Vencer";
            String msj = String.format("El comprobante %s-%s del proveedor %s vence el %s por un total de %s %s.",
                    c.getSerie(),
                    c.getCorrelativo(),
                    c.getProveedor() != null ? c.getProveedor().getRazonSocial() : "N/A",
                    c.getFechaVencimiento().toString(),
                    c.getMoneda(),
                    c.getTotalPagar() != null ? c.getTotalPagar().toString() : "0.00");

            notificacionService.crearNotificacion(
                    "PAGO_VENCE",
                    titulo,
                    msj,
                    "COMPRA_" + c.getId(),
                    c.getIdSucursal()
            );
        }
    }
}

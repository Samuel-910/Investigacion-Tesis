package com.pe.articulos.modules.descuentos.service;

import com.pe.articulos.modules.descuentos.dto.DescuentoDTO;
import com.pe.articulos.modules.descuentos.dto.DescuentoDetalleDTO;
import com.pe.articulos.modules.venta_registro.entity.VentaDetalle;
import com.pe.articulos.modules.venta_registro.entity.VentaRegistro;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio centralizado para gestionar el cálculo de descuentos en ventas.
 */
@Service
@RequiredArgsConstructor
public class CalculadorDescuentoService {

    private final DescuentoService descuentoService;

    /**
     * Aplica la lógica de descuentos a toda una venta basándose en las reglas
     * dinámicas.
     */
    public void procesarDescuentos(VentaRegistro venta) {
        if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
            return;
        }

        // 1. Cargar descuentos vigentes desde el nuevo servicio Maestro-Detalle
        List<DescuentoDTO> vigentes = descuentoService.listarVigentes(org.springframework.data.domain.Pageable.unpaged()).getContent();

        for (VentaDetalle detalle : venta.getDetalles()) {
            BigDecimal descuentoTotal = BigDecimal.ZERO;
            boolean descuentoAutomaticoAplicado = false;

            // 2. Evaluar cada descuento vigente para el producto específico
            for (DescuentoDTO descuento : vigentes) {
                if (descuento.getDetalles() == null)
                    continue;

                for (DescuentoDetalleDTO detDto : descuento.getDetalles()) {
                    boolean aplica = false;

                    // Si el descuento es GLOBAL, aplica a cualquier producto
                    if ("GLOBAL".equals(descuento.getTipoAlcance())) {
                        aplica = true;
                    }
                    // Si es POR_PRODUCTO, validamos el ID del catálogo
                    else if (detDto.getIdCatalogo() != null && detDto.getIdCatalogo().equals(detalle.getIdCatalogo())) {
                        aplica = true;
                    }

                    if (aplica && (detDto.getCantidad() == null
                            || detalle.getCantidad().compareTo(new BigDecimal(detDto.getCantidad())) >= 0)) {
                        if ("PORCENTAJE".equals(detDto.getTipoDescuento())) {
                            BigDecimal base = detalle.getCantidad().multiply(detalle.getPrecioVenta());
                            descuentoTotal = base.multiply(detDto.getValorDescuento())
                                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                        } else if ("MONTO_FIJO".equals(detDto.getTipoDescuento())) {
                            descuentoTotal = detDto.getValorDescuento().multiply(detalle.getCantidad());
                        } else if ("CANTIDAD".equals(detDto.getTipoDescuento())) {
                            // En CANTIDAD, el valorDescuento es el nuevo precio unitario especial
                            BigDecimal ahorroPorUnidad = detalle.getPrecioVenta().subtract(detDto.getValorDescuento());
                            if (ahorroPorUnidad.compareTo(BigDecimal.ZERO) > 0) {
                                descuentoTotal = ahorroPorUnidad.multiply(detalle.getCantidad());
                            }
                        }

                        if (descuentoTotal.compareTo(BigDecimal.ZERO) > 0) {
                            descuentoAutomaticoAplicado = true;
                            break;
                        }
                    }
                }
                if (descuentoAutomaticoAplicado)
                    break;
            }

            // 3. Fallback: Si no hay descuento automático, aplicar descuento manual si
            // existe
            if (!descuentoAutomaticoAplicado) {
                if (detalle.getPorcentajeDescuento() != null
                        && detalle.getPorcentajeDescuento().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal base = detalle.getCantidad().multiply(detalle.getPrecioVenta());
                    descuentoTotal = base.multiply(detalle.getPorcentajeDescuento())
                            .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                }
            }

            detalle.setMontoDescuento(descuentoTotal);
            detalle.calcularTotales();
        }

        venta.calcularTotales();
    }
}

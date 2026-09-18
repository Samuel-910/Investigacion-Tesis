package com.pe.articulos.modules.compras.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.modules.compras.entity.Compra;
import com.pe.articulos.modules.compras.repository.CompraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import com.pe.articulos.service.OutboxEvent;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class CompraPagoConsumer {

    private final ObjectMapper objectMapper;
    private final CompraRepository compraRepository;

    @EventListener
    public void consumirPago(OutboxEvent event) {
        if (!"compras.pago.realizado".equals(event.getColaEvento().getTopico())) {
            return;
        }
        String mensajeJson = event.getColaEvento().getContenido();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> evento = objectMapper.readValue(mensajeJson, Map.class);
            
            if (evento.containsKey("idCompra")) {
                Long idCompra = Long.valueOf(evento.get("idCompra").toString());
                
                Compra compra = compraRepository.findById(idCompra).orElse(null);
                if (compra != null) {
                    compra.setEstado(EstadoGeneral.PAGADO);
                    compraRepository.save(compra);
                    log.info("Estado de la compra {} actualizado a PAGADO vía Outbox", idCompra);
                } else {
                    log.warn("Se recibió evento de pago pero la compra con ID {} no existe", idCompra);
                }
            }
        } catch (Exception e) {
            log.error("Error al procesar evento de pago de compra", e);
        }
    }
}

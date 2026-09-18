package com.pe.articulos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class WebSocketNotificationListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @EventListener
    public void onOutboxEvent(OutboxEvent event) {
        if (!"sistema.notificaciones".equals(event.getColaEvento().getTopico())) {
            return;
        }

        try {
            String contenidoJson = event.getColaEvento().getContenido();
            @SuppressWarnings("unchecked")
            Map<String, Object> notification = objectMapper.readValue(contenidoJson, Map.class);
            
            log.info("Procesando notificación Outbox para WebSocket: {}", notification);

            String userId = notification.containsKey("userId") ? notification.get("userId").toString() : null;
            String titulo = notification.containsKey("titulo") ? notification.get("titulo").toString() : "";
            Map<String, Object> contenidoObj = (Map<String, Object>) notification.get("contenido");
            
            String sucursalId = "all";
            if (contenidoObj != null && contenidoObj.containsKey("idSucursal")) {
                Object idSuc = contenidoObj.get("idSucursal");
                if (idSuc != null && !idSuc.toString().trim().isEmpty()) {
                    sucursalId = idSuc.toString();
                }
            }

            // Determinar módulo destino en base a la lógica de negocio
            String targetModulo = "all";
            if (titulo.startsWith("Solicitud de Pago")) {
                targetModulo = "finanzas";
            } else if (titulo.equals("Pago Realizado")) {
                targetModulo = "articulos";
            }

            if (userId != null && !userId.isEmpty()) {
                messagingTemplate.convertAndSendToUser(
                        userId,
                        "/queue/notifications",
                        notification);
                log.info("Notificación enviada al usuario: {}", userId);
            } else {
                String destination = "/topic/notifications/" + targetModulo + "/" + sucursalId;
                messagingTemplate.convertAndSend(destination, notification);
                log.info("Notificación enviada al tópico: {}", destination);
            }
        } catch (Exception e) {
            log.error("Error al enviar notificación WebSocket", e);
        }
    }
}

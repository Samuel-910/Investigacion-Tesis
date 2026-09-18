package com.pe.articulos.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final ColaEventoRepository colaEventoRepository;
    private final ObjectMapper objectMapper;
    private static final String TOPIC = "sistema.notificaciones";

    /**
     * Envía una notificación al microservicio centralizado.
     * 
     * @param userId ID del usuario destinatario (si es null, es broadcast)
     * @param modulo Módulo que origina la notificación (Compras, Inventario, etc.)
     * @param titulo Título de la alerta
     * @param contenido Objeto flexible con datos adicionales
     * @param tipo INFO | SUCCESS | ERROR
     */
    public void enviarNotificacion(String userId, String modulo, String titulo, Object contenido, String tipo) {
        Map<String, Object> message = Map.of(
            "userId", userId != null ? userId : "",
            "modulo", modulo,
            "titulo", titulo,
            "contenido", contenido != null ? contenido : "",
            "tipo", tipo
        );
        
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            
            ColaEvento evento = ColaEvento.builder()
                .modulo(modulo)
                .topico(TOPIC)
                .titulo(titulo)
                .contenido(jsonMessage)
                .tipo(tipo)
                .build();
                
            colaEventoRepository.save(evento);
            log.info("Guardado evento en outbox: {}", jsonMessage);
        } catch (JsonProcessingException e) {
            log.error("Error al serializar mensaje", e);
        }
    }
}

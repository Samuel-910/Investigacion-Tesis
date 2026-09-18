package com.pe.articulos.core.shared.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WhatsAppService {

    @Value("${mensajeria.api.url:http://localhost:8087}")
    private String mensajeriaApiUrl;

    public void sendWhatsAppMessage(String to, String body) {
        enviarPorMensajeriaApi(to, body, null, null, null, null);
    }

    public void sendWhatsAppTemplate(String to, String templateName, List<String> params, byte[] attachment, String fileName) {
        enviarPorMensajeriaApi(to, null, templateName, params, attachment, fileName);
    }

    private void enviarPorMensajeriaApi(String to, String body, String templateName, List<String> params, byte[] attachment, String fileName) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();

            // Obtain the current JWT token
            try {
                var attributes = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
                if (attributes instanceof org.springframework.web.context.request.ServletRequestAttributes) {
                    jakarta.servlet.http.HttpServletRequest req = ((org.springframework.web.context.request.ServletRequestAttributes) attributes).getRequest();
                    String authHeader = req.getHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        headers.set("Authorization", authHeader);
                    }
                }
            } catch (Exception e) {
                log.warn("No se pudo obtener el token de autorización actual: {}", e.getMessage());
            }

            if (attachment != null) {
                // Modo Multipart para adjuntos
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                MultiValueMap<String, Object> request = new LinkedMultiValueMap<>();
                request.add("numero", to);
                request.add("templateName", templateName);
                if (params != null) {
                    for (String param : params) {
                        request.add("templateParams", param);
                    }
                }
                
                ByteArrayResource resource = new ByteArrayResource(attachment) {
                    @Override
                    public String getFilename() {
                        return fileName != null ? fileName : "document.pdf";
                    }
                };
                request.add("file", resource);

                HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(request, headers);
                String url = mensajeriaApiUrl + "/api/v1/mensajes/whatsapp/enviar-con-adjunto";
                restTemplate.postForObject(url, entity, String.class);
            } else {
                // Modo JSON sin adjuntos
                headers.setContentType(MediaType.APPLICATION_JSON);
                Map<String, Object> request = new HashMap<>();
                request.put("numero", to);
                if (body != null) {
                    request.put("mensaje", body);
                }
                if (templateName != null) {
                    request.put("templateName", templateName);
                    request.put("templateParams", params);
                }

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
                String url = mensajeriaApiUrl + "/api/v1/mensajes/whatsapp/enviar";
                restTemplate.postForObject(url, entity, String.class);
            }
            
            log.info("WhatsApp enviado a {} mediante mensajeria_api", to);
        } catch (Exception e) {
            log.error("Error enviando WhatsApp a {} mediante mensajeria_api: {}", to, e.getMessage());
            throw new RuntimeException("Error al enviar WhatsApp mediante mensajeria_api", e);
        }
    }
}

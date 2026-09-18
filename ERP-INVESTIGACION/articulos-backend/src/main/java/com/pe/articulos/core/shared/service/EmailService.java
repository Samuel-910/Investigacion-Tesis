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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Value("${mensajeria.api.url:http://localhost:8087}")
    private String mensajeriaApiUrl;

    public void sendEmailWithAttachment(String to, String subject, String body, byte[] attachment, String fileName) {
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

            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> request = new LinkedMultiValueMap<>();
            request.add("to", to);
            request.add("subject", subject);
            request.add("body", body);

            if (attachment != null && fileName != null) {
                // Para enviar un archivo como byte[] en RestTemplate necesitamos envolverlo
                ByteArrayResource resource = new ByteArrayResource(attachment) {
                    @Override
                    public String getFilename() {
                        return fileName;
                    }
                };
                request.add("file", resource);
            }

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(request, headers);
            String url = mensajeriaApiUrl + "/api/email/send-with-attachment";
            
            restTemplate.postForObject(url, entity, String.class);
            log.info("Email enviado exitosamente a {} mediante mensajeria_api", to);
        } catch (Exception e) {
            log.error("Error al enviar email a {} mediante mensajeria_api: {}", to, e.getMessage());
            throw new RuntimeException("Error al enviar el correo electrónico mediante mensajeria_api", e);
        }
    }
}

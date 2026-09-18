package com.pe.articulos.core.security.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.HttpServletRequest;

public class HttpUtils {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    /**
     * Obtiene el HttpServletRequest actual del contexto de Spring
     */
    /**
     * Obtiene el HttpServletRequest actual del contexto de Spring
     */
    public static HttpServletRequest getCurrentRequest() {
        try {
            var attributes = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attributes instanceof org.springframework.web.context.request.ServletRequestAttributes) {
                return ((org.springframework.web.context.request.ServletRequestAttributes) attributes).getRequest();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Obtiene la IP del request actual (contexto automático)
     */
    public static String getClientIpAddress() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? getClientIpAddress(request) : "Unknown";
    }

    /**
     * Obtiene el User-Agent del request actual (contexto automático)
     */
    public static String getUserAgent() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? getUserAgent(request) : "Unknown";
    }

    /**
     * Obtiene la dirección IP real del cliente, considerando proxies y
     * balanceadores
     * de carga
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null)
            return "Unknown";
        for (String header : IP_HEADER_CANDIDATES) {
            String ipList = request.getHeader(header);
            if (ipList != null && ipList.length() != 0 && !"unknown".equalsIgnoreCase(ipList)) {
                // Si hay múltiples IPs (por proxies), tomar la primera
                String ip = ipList.split(",")[0];
                return ip.trim();
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * Obtiene el User-Agent del navegador/cliente
     */
    public static String getUserAgent(HttpServletRequest request) {
        if (request == null)
            return "Unknown";
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "Unknown";
    }

    /**
     * Obtiene el username del usuario autenticado actual
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return "System";
    }
}

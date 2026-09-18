package com.pe.articulos.core.reports.audit.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.pe.articulos.core.reports.audit.entity.AuditLog;
import com.pe.articulos.core.reports.audit.repository.AuditRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class AuditService {

    private final AuditRepository auditRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String module, String action, String details,
            String username, Long userId, String ipAddress,
            String status, String userAgent, String url, String method) {

        AuditLog log = AuditLog.builder()
                .module(module)
                .action(action)
                .details(details)
                .username(username)
                .userId(userId)
                .ipAddress(ipAddress)
                .status(status)
                .userAgent(userAgent)
                .url(url)
                .method(method)
                .timestamp(LocalDateTime.now())
                .build();
        auditRepository.save(log);
    }

    /**
     * Registra una acción de usuario capturando automáticamente el contexto
     * (Usuario, IP, etc)
     */
    public void logUserAction(String module, String action, String details) {
        try {
            String username = com.pe.articulos.core.security.util.HttpUtils.getCurrentUsername();
            String ipAddress = com.pe.articulos.core.security.util.HttpUtils.getClientIpAddress();
            String userAgent = com.pe.articulos.core.security.util.HttpUtils.getUserAgent();

            // Intentar obtener URL y método si es posible
            String url = "N/A";
            String method = "N/A";
            var request = com.pe.articulos.core.security.util.HttpUtils.getCurrentRequest();
            if (request != null) {
                url = request.getRequestURI();
                method = request.getMethod();
            }

            // UserId lo dejamos null o intentamos buscarlo si es crítico (por ahora
            // username basta para el log)
            // Si necesitamos userId, tendríamos que inyectar DatosPersonalesRepository o
            // extraerlo del JWT claim si está en el contexto
            Long userId = null;

            logAction(module, action, details, username, userId, ipAddress, "Éxito", userAgent, url, method);
        } catch (Exception e) {
            // Silently fail to not block business logic
            System.err.println("Error al registrar auditoría: " + e.getMessage());
        }
    }

    public List<AuditLog> getAllLogs() {
        return auditRepository.findAllByOrderByTimestampDesc();
    }

    public List<AuditLog> getLogsByUser(String username) {
        return auditRepository.findByUsernameOrderByTimestampDesc(username);
    }

    /**
     * Búsqueda avanzada para la bitácora
     */
    public List<AuditLog> searchLogs(String username, List<String> actions,
            LocalDateTime startDate, LocalDateTime endDate) {

        // Manejo de strings vacíos como null para ignorar filtro
        String userFilter = null;
        if (username != null && !username.trim().isEmpty()) {
            userFilter = "%" + username.trim() + "%";
        }

        // 1. Sanitize the list: remove nulls and empty strings
        List<String> validActions = null;
        if (actions != null) {
            validActions = actions.stream()
                    .filter(a -> a != null && !a.trim().isEmpty())
                    .toList();
        }

        // 4. Default Date Range to avoid NULL parameters in SQL (Fixes Postgres Type
        // Error)
        LocalDateTime effectiveStartDate = startDate != null ? startDate : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime effectiveEndDate = endDate != null ? endDate : LocalDateTime.of(2100, 12, 31, 23, 59);

        // 2. Si la lista filtrada es nula o vacía, usamos el método SIN filtro de
        // acciones
        if (validActions == null || validActions.isEmpty()) {
            return auditRepository.searchLogsNoActions(userFilter, effectiveStartDate, effectiveEndDate);
        }

        // 3. Si hay acciones específicas válidas, usamos el método normal
        return auditRepository.searchLogs(userFilter, validActions, effectiveStartDate, effectiveEndDate);
    }
}

package com.pe.articulos.core.security.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.pe.articulos.modules.auth.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Interceptor para extraer el contexto de sucursal del JWT
 * y guardarlo en los atributos del request para uso posterior
 */
@Component
@Slf4j
public class SucursalContextInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtService jwtService;

    @Override
    public boolean preHandle(@org.springframework.lang.NonNull HttpServletRequest request,
            @org.springframework.lang.NonNull HttpServletResponse response,
            @org.springframework.lang.NonNull Object handler) {
        String token = extractToken(request);

        if (token != null) {
            try {
                Long sucursalId = jwtService.extractSucursalId(token);
                String sucursalNombre = jwtService.extractSucursalNombre(token);

                if (sucursalId != null) {
                    request.setAttribute("sucursalId", sucursalId);
                    request.setAttribute("sucursalNombre", sucursalNombre);
                    log.debug("Sucursal context set: {} - {}", sucursalId, sucursalNombre);
                }
            } catch (Exception e) {
                // Token sin sucursal (login inicial) o token inválido
                log.debug("No sucursal context in token: {}", e.getMessage());
            }
        }

        return true;
    }

    /**
     * Extrae el token JWT del header Authorization
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

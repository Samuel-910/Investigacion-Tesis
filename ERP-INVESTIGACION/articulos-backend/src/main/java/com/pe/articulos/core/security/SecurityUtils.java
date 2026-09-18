package com.pe.articulos.core.security;

import com.pe.articulos.modules.users.entity.DatosPersonales;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    /**
     * Obtiene el ID del usuario actualmente autenticado.
     * 
     * @return El ID del usuario o null si no hay autenticación.
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof DatosPersonales) {
            return ((DatosPersonales) principal).getId();
        }

        return null;
    }

    /**
     * Obtiene el ID de la sucursal actual del usuario autenticado.
     * 
     * @return El ID de la sucursal o null si no se puede determinar.
     */
    public static Long getCurrentUserSucursalId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof DatosPersonales) {
            DatosPersonales user = (DatosPersonales) principal;
            if (user.getSucursalActual() != null) {
                return user.getSucursalActual().getIdSucursal();
            }
        }

        return null;
    }

    /**
     * Obtiene el login del usuario actualmente autenticado.
     * 
     * @return El login del usuario o "anonymousUser" si no hay autenticación.
     */
    public static String getCurrentUserLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null) ? authentication.getName() : "anonymousUser";
    }
}

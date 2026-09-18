package com.pe.articulos.core.system.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pe.articulos.core.exception.ValidationException;
import com.pe.articulos.core.system.entity.Configuracion;
import com.pe.articulos.core.system.repository.ConfiguracionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ConfiguracionService {

    private final ConfiguracionRepository configuracionRepository;

    public Configuracion getConfig(String clave) {
        return configuracionRepository.findByClave(clave)
                .orElseThrow(() -> new ValidationException("Configuración no encontrada: " + clave));
    }

    @Transactional
    public Long getGlobalSessionTimeout() {
        return configuracionRepository.findByClave("GLOBAL_SESSION_TIMEOUT")
                .map(config -> {
                    try {
                        return Long.parseLong(config.getValor());
                    } catch (NumberFormatException e) {
                        return 30L; // Default fallback if parsing fails
                    }
                })
                .orElseGet(() -> {
                    // Create default configuration if it doesn't exist
                    Configuracion defaultConfig = Configuracion.builder()
                            .clave("GLOBAL_SESSION_TIMEOUT")
                            .valor("30") // 30 minutes default
                            .descripcion("Tiempo de expiración de sesión global en minutos")
                            .build();
                    configuracionRepository.save(defaultConfig);
                    return 30L;
                });
    }

    @Transactional
    public Configuracion updateGlobalSessionTimeout(Long minutes) {
        // Validar mínimo de 15 minutos
        if (minutes < 15) {
            throw new IllegalArgumentException("El tiempo mínimo de sesión es de 15 minutos");
        }

        return configuracionRepository.findByClave("GLOBAL_SESSION_TIMEOUT")
                .map(config -> {
                    config.setValor(String.valueOf(minutes));
                    return configuracionRepository.save(config);
                })
                .orElseGet(() -> {
                    Configuracion newConfig = Configuracion.builder()
                            .clave("GLOBAL_SESSION_TIMEOUT")
                            .valor(String.valueOf(minutes))
                            .descripcion("Tiempo de expiración de sesión global en minutos")
                            .build();
                    return configuracionRepository.save(newConfig);
                });
    }

    @Transactional
    public Configuracion updateConfig(String clave, String valor) {
        Configuracion config = getConfig(clave);
        config.setValor(valor);
        return configuracionRepository.save(config);
    }
}

package com.pe.articulos.core.system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.pe.articulos.core.system.entity.Configuracion;
import com.pe.articulos.core.system.service.ConfiguracionService;

import lombok.RequiredArgsConstructor;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    @GetMapping("/{clave}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Configuracion> getConfig(@PathVariable String clave) {
        return ResponseEntity.ok(configuracionService.getConfig(clave));
    }

    @PutMapping("/{clave}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Configuracion> updateConfig(@PathVariable String clave,
            @RequestBody Map<String, String> body) {
        String valor = body.get("valor");
        if (valor == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(configuracionService.updateConfig(clave, valor));
    }

    @GetMapping("/session-timeout")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Map<String, Long>> getSessionTimeout() {
        return ResponseEntity.ok(Map.of("minutes", configuracionService.getGlobalSessionTimeout()));
    }

    @PostMapping("/session-timeout")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Configuracion> updateSessionTimeout(@RequestBody Map<String, Object> body) {
        try {
            Object minutesObj = body.get("minutes");
            if (minutesObj == null) {
                return ResponseEntity.badRequest().build();
            }

            Long minutes;
            if (minutesObj instanceof Number) {
                minutes = ((Number) minutesObj).longValue();
            } else {
                try {
                    minutes = Long.parseLong(minutesObj.toString());
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().build();
                }
            }

            if (minutes <= 0) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(configuracionService.updateGlobalSessionTimeout(minutes));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ERROR UPDATING SESSION TIMEOUT: " + e.getMessage());
            throw e;
        }
    }
}

package com.pe.articulos.core.security.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntrusionAttemptDTO {

    private String ipAddress;
    private String username;
    private Integer totalIntentos;
    private LocalDateTime ultimoIntento;
    private LocalDateTime tiempoDesbloqueo;
    private String motivoBloqueo;
}

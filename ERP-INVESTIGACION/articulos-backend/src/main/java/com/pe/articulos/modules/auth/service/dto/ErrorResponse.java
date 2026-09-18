package com.pe.articulos.modules.auth.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private boolean success;
    private String message;
    private Integer remainingAttempts;

    @Builder.Default
    private boolean blocked = false;
    private LocalDateTime unblockTime;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}

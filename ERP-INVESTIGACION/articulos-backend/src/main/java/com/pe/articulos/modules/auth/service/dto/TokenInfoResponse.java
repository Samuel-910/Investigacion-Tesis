package com.pe.articulos.modules.auth.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenInfoResponse {
    private Long timeRemainingSeconds;
    private String token;
    private Long timeRemainingMs;
    private Long expirationTimeMs;
    private Boolean isExpired;
}
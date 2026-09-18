package com.pe.articulos.core.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.pe.articulos.core.security.entity.SessionStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionInfoDTO {
    private Long id;
    private String username;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime loginTime;
    private LocalDateTime lastAccessTime;
    private SessionStatus status;
    private boolean isSuspicious;
    private String location;
}

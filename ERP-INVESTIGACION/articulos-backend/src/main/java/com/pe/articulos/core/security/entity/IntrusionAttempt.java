package com.pe.articulos.core.security.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "intruso", indexes = {
        @Index(name = "idx_intruso_username", columnList = "username"),
        @Index(name = "idx_intruso_ip", columnList = "ipAddress"),
        @Index(name = "idx_intruso_attempt_time", columnList = "attemptTime")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntrusionAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "attempt_time", nullable = false)
    private LocalDateTime attemptTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_reason", nullable = false, length = 50)
    private FailureReason failureReason;

    @Column(name = "consecutive_attempts")
    @Builder.Default
    private Integer consecutiveAttempts = 1;

    @Column(name = "caused_block")
    @Builder.Default
    private boolean causedBlock = false;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @PrePersist
    protected void onCreate() {
        if (attemptTime == null) {
            attemptTime = LocalDateTime.now();
        }
    }
}

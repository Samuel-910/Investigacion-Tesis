package com.pe.articulos.core.security.entity;

import java.time.LocalDateTime;

import com.pe.articulos.modules.users.entity.DatosPersonales;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sessiones", indexes = {
        @Index(name = "idx_sessiones_user_id", columnList = "user_id"),
        @Index(name = "idx_sessiones_ip", columnList = "ipAddress"),
        @Index(name = "idx_sessiones_status", columnList = "status"),
        @Index(name = "idx_sessiones_login_time", columnList = "loginTime")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private DatosPersonales user;

    @Column(name = "session_token", unique = true, nullable = false, columnDefinition = "TEXT")
    private String sessionToken;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;

    @Column(name = "last_access_time")
    private LocalDateTime lastAccessTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(length = 100)
    private String location;

    @Column(name = "is_suspicious", nullable = false)
    @Builder.Default
    private boolean isSuspicious = false;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @PrePersist
    protected void onCreate() {
        if (loginTime == null) {
            loginTime = LocalDateTime.now();
        }
        if (lastAccessTime == null) {
            lastAccessTime = loginTime;
        }
    }
}

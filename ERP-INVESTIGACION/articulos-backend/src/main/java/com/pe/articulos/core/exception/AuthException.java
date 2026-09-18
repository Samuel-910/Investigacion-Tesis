package com.pe.articulos.core.exception;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class AuthException extends RuntimeException {

    private final Integer remainingAttempts;

    private final LocalDateTime unblockTime;

    private final boolean blocked;

    public AuthException(String message) {
        super(message);
        this.remainingAttempts = null;
        this.unblockTime = null;
        this.blocked = false;
    }

    public AuthException(String message, int remainingAttempts) {
        super(message);
        this.remainingAttempts = remainingAttempts;
        this.blocked = remainingAttempts == 0;
        this.unblockTime = null;
    }

    public AuthException(String message, int remainingAttempts, LocalDateTime unblockTime) {
        super(message);
        this.remainingAttempts = remainingAttempts;
        this.blocked = remainingAttempts == 0;
        this.unblockTime = unblockTime;
    }

    public AuthException(String message, int remainingAttempts, boolean blocked, LocalDateTime unblockTime) {
        super(message);
        this.remainingAttempts = remainingAttempts;
        this.blocked = blocked;
        this.unblockTime = unblockTime;
    }
}
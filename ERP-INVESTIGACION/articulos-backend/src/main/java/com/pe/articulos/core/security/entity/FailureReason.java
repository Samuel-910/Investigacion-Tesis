package com.pe.articulos.core.security.entity;

public enum FailureReason {
    INVALID_CREDENTIALS,

    USER_NOT_FOUND,

    ACCOUNT_BLOCKED,

    AUTHENTICATION_ERROR,

    IP_BLOCKED,
    
    UNAUTHORIZED_ACCESS
}

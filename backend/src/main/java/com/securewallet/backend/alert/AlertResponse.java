package com.securewallet.backend.alert;

import java.time.Instant;

public record AlertResponse(
        Long id,
        String alertType,
        String severity,
        String email,
        String message,
        Instant createdAt
) {
    public static AlertResponse from(SecurityAlert a) {
        return new AlertResponse(
                a.getId(),
                a.getAlertType(),
                a.getSeverity(),
                a.getEmail(),
                a.getMessage(),
                a.getCreatedAt()
        );
    }
}
package com.securewallet.backend.auth.dto;

public record AuthResponse(
        String token,
        long expiresInMinutes,
        UserResponse user
) {}
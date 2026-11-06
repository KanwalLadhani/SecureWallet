package com.securewallet.backend.auth.dto;

import com.securewallet.backend.user.Role;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        Role role
) {}
package com.securewallet.backend.auth;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/me")
public class MeController {

    @GetMapping
    public Map<String, Object> me(Authentication authentication) {
        return Map.of(
                "email", authentication.getName(),
                "authorities", authentication.getAuthorities().toString()
        );
    }
}
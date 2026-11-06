package com.securewallet.backend.n8n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class LoginGuardClient {

    private static final Logger log = LoggerFactory.getLogger(LoginGuardClient.class);

    private final String webhookUrl;
    private final String webhookSecret;
    private final RestTemplate restTemplate;

    public LoginGuardClient(
            @Value("${securewallet.n8n.login-guard-url}") String webhookUrl,
            @Value("${securewallet.n8n.webhook-secret}") String webhookSecret) {
        this.webhookUrl = webhookUrl;
        this.webhookSecret = webhookSecret;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000); // give up connecting after 2 seconds
        factory.setReadTimeout(3000);    // give up waiting for a reply after 3 seconds
        this.restTemplate = new RestTemplate(factory);
    }

    /** Sends one login attempt to n8n in the background. Never throws. */
    public void report(String email, boolean success) {
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> body = Map.of(
                        "email", email,
                        "success", success,
                        "time", Instant.now().toString()
                );

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("X-Webhook-Secret", webhookSecret); // the "password" n8n will check

                restTemplate.postForEntity(webhookUrl, new HttpEntity<>(body, headers), String.class);
            } catch (Exception e) {
                log.warn("Login Guard (n8n) could not be reached: {}", e.getMessage());
            }
        });
    }
}
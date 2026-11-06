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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Component
public class FraudCheckClient {

    private static final Logger log = LoggerFactory.getLogger(FraudCheckClient.class);
    private static final Set<String> ALLOWED = Set.of("COMPLETED", "FLAGGED", "BLOCKED");

    /** The officer's answer: a traffic light (decision) and a short reason. */
    public record Verdict(String decision, String reason) {}

    private final String webhookUrl;
    private final String webhookSecret;
    private final RestTemplate restTemplate;

    public FraudCheckClient(
            @Value("${securewallet.n8n.fraud-check-url}") String webhookUrl,
            @Value("${securewallet.n8n.webhook-secret}") String webhookSecret) {
        this.webhookUrl = webhookUrl;
        this.webhookSecret = webhookSecret;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000); // give up connecting after 2 seconds
        factory.setReadTimeout(3000);    // give up waiting for the answer after 3 seconds
        this.restTemplate = new RestTemplate(factory);
    }

    /** Asks n8n if this transfer looks safe. Waits for the answer. Never throws. */
    public Verdict check(String senderEmail, String receiverEmail, BigDecimal amount) {
        try {
            Map<String, Object> body = Map.of(
                    "senderEmail", senderEmail,
                    "receiverEmail", receiverEmail,
                    "amount", amount,
                    "time", Instant.now().toString()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Webhook-Secret", webhookSecret); // the "password" n8n will check

            Verdict verdict = restTemplate.postForObject(
                    webhookUrl, new HttpEntity<>(body, headers), Verdict.class);

            if (verdict == null || verdict.decision() == null || !ALLOWED.contains(verdict.decision())) {
                log.warn("Fraud Detector gave an unusable answer, allowing the transfer");
                return new Verdict("COMPLETED", "Fraud Detector gave an unusable answer");
            }
            return verdict;
        } catch (Exception e) {
            log.warn("Fraud Detector (n8n) could not be reached, allowing the transfer: {}", e.getMessage());
            return new Verdict("COMPLETED", "Fraud Detector unavailable");
        }
    }
}
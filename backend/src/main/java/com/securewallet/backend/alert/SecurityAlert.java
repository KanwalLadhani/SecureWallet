package com.securewallet.backend.alert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

@Entity
@Immutable
@Table(name = "security_alerts")
public class SecurityAlert {

    @Id
    private Long id;

    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType;

    @Column(nullable = false, length = 20)
    private String severity;

    @Column(length = 255)
    private String email;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected SecurityAlert() {
        // needed by JPA
    }

    public Long getId() { return id; }
    public String getAlertType() { return alertType; }
    public String getSeverity() { return severity; }
    public String getEmail() { return email; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
}
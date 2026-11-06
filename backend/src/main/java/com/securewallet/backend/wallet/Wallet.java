package com.securewallet.backend.wallet;

import com.securewallet.backend.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Wallet() {
    }

    public Wallet(User user, BigDecimal balance) {
        this.user = user;
        this.balance = balance;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public Instant getCreatedAt() { return createdAt; }
}
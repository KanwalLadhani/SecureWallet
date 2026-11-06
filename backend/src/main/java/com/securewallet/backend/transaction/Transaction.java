package com.securewallet.backend.transaction;

import com.securewallet.backend.wallet.Wallet;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_wallet_id", nullable = false)
    private Wallet senderWallet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_wallet_id", nullable = false)
    private Wallet receiverWallet;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(length = 140)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Transaction() {
    }

    public Transaction(Wallet senderWallet, Wallet receiverWallet, BigDecimal amount,
                       TransactionStatus status, String note) {
        this.senderWallet = senderWallet;
        this.receiverWallet = receiverWallet;
        this.amount = amount;
        this.status = status;
        this.note = note;
    }

    public Long getId() { return id; }
    public Wallet getSenderWallet() { return senderWallet; }
    public Wallet getReceiverWallet() { return receiverWallet; }
    public BigDecimal getAmount() { return amount; }
    public TransactionStatus getStatus() { return status; }
    public String getNote() { return note; }
    public Instant getCreatedAt() { return createdAt; }
}
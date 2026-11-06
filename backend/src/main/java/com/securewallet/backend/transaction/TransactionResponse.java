package com.securewallet.backend.transaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        String senderEmail,
        String receiverEmail,
        BigDecimal amount,
        TransactionStatus status,
        String note,
        Instant createdAt
) {
}
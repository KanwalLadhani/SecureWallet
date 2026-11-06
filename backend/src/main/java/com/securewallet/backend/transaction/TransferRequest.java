package com.securewallet.backend.transaction;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank @Email String receiverEmail,
        @NotNull
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        @DecimalMax(value = "1000000.00", message = "Amount is too large")
        @Digits(integer = 7, fraction = 2, message = "Amount can have at most 2 decimals")
        BigDecimal amount,
        @Size(max = 140) String note
) {
}
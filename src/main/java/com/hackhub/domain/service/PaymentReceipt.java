package com.hackhub.domain.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class PaymentReceipt {

    private UUID id;
    private String externalReference;
    private BigDecimal amount;
    private LocalDateTime paidAt;

    protected PaymentReceipt() {
        // For future ORM mapping.
    }

    public PaymentReceipt(String externalReference, BigDecimal amount, LocalDateTime paidAt) {
        this.id = UUID.randomUUID();
        this.externalReference = Objects.requireNonNull(externalReference, "externalReference cannot be null");
        this.amount = Objects.requireNonNull(amount, "amount cannot be null");
        this.paidAt = Objects.requireNonNull(paidAt, "paidAt cannot be null");

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
    }

    public UUID getId() {
        return id;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }
}
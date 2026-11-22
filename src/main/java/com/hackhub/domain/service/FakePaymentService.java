package com.hackhub.domain.service;

import com.hackhub.domain.actor.Team;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class FakePaymentService implements PaymentService {

    @Override
    public PaymentReceipt payPrize(Team winningTeam, BigDecimal amount, String reason) {
        String reference = "PAY-" + UUID.randomUUID();
        return new PaymentReceipt(reference, amount, LocalDateTime.now());
    }
}

package com.hackhub.domain.service;

import com.hackhub.domain.actor.Team;

import java.math.BigDecimal;

public interface PaymentService {

    PaymentReceipt payPrize(Team winningTeam, BigDecimal amount, String reason);
}
package com.hackhub.dto;

public record PrizePaymentDto(
        Long winningTeamId,
        String paymentReference
) {
}

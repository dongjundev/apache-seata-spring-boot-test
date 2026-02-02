package com.example.payment.dto;

import java.math.BigDecimal;

public record PaymentRequest(
        String productId,
        BigDecimal amount
) {
}

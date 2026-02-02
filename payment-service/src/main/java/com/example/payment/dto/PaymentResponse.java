package com.example.payment.dto;

public record PaymentResponse(
        Long paymentId,
        String status,
        String message
) {
    public static PaymentResponse success(Long paymentId) {
        return new PaymentResponse(paymentId, "SUCCESS", "Payment processed successfully");
    }

    public static PaymentResponse failure(String message) {
        return new PaymentResponse(null, "FAILED", message);
    }
}

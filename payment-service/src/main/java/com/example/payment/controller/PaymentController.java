package com.example.payment.controller;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.service.PaymentService;
import org.apache.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
        // XID는 SeataHttpAutoConfiguration의 JakartaTransactionPropagationInterceptor가 자동 바인딩
        log.info("Received payment request, XID: {}", RootContext.getXID());

        try {
            PaymentResponse response = paymentService.processPayment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Payment processing failed", e);
            return ResponseEntity.badRequest()
                    .body(PaymentResponse.failure(e.getMessage()));
        }
    }

    @PostMapping("/fail")
    public ResponseEntity<PaymentResponse> createPaymentAndFail(@RequestBody PaymentRequest request) {
        log.info("Received payment request (will fail), XID: {}", RootContext.getXID());
        paymentService.processPaymentAndFail(request);
        return ResponseEntity.ok(PaymentResponse.success(null));
    }
}

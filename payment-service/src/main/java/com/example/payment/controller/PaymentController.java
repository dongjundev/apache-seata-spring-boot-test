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
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestHeader(value = RootContext.KEY_XID, required = false) String xid,
            @RequestBody PaymentRequest request) {

        log.info("Received payment request with XID: {}", xid);

        if (xid != null) {
            RootContext.bind(xid);
        }

        try {
            PaymentResponse response = paymentService.processPayment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Payment processing failed", e);
            return ResponseEntity.badRequest()
                    .body(PaymentResponse.failure(e.getMessage()));
        } finally {
            if (xid != null) {
                RootContext.unbind();
            }
        }
    }
}

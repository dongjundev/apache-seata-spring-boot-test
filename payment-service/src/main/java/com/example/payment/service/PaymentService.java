package com.example.payment.service;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.entity.Payment;
import com.example.payment.repository.PaymentRepository;
import org.apache.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) throws InterruptedException {
        String xid = RootContext.getXID();
        log.info("Processing payment in global transaction, XID: {}", xid);
        log.info("Payment request - productId: {}, amount: {}", request.productId(), request.amount());

        Payment payment = new Payment(request.productId(), request.amount(), "COMPLETED");
        payment = paymentRepository.save(payment);
        Thread.sleep(20000);

        log.info("Payment saved successfully, paymentId: {}", payment.getId());
        return PaymentResponse.success(payment.getId());
    }
}

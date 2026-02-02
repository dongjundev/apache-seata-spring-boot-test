package com.example.bff.service;

import com.example.bff.dto.OrderRequest;
import com.example.bff.dto.OrderResponse;
import org.apache.seata.core.context.RootContext;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final RestClient restClient;

    @Value("${service.payment.url}")
    private String paymentServiceUrl;

    @Value("${service.inventory.url}")
    private String inventoryServiceUrl;

    public OrderService(RestClient restClient) {
        this.restClient = restClient;
    }

    @GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
    public OrderResponse createOrder(OrderRequest request) {
        String xid = RootContext.getXID();
        log.info("Starting global transaction, XID: {}", xid);

        try {
            // Step 1: Create payment
            log.info("Calling payment service for product: {}, amount: {}",
                    request.productId(), request.amount());

            callPaymentService(request);

            // Step 2: Deduct inventory
            log.info("Calling inventory service for product: {}, quantity: {}",
                    request.productId(), request.quantity());

            callInventoryService(request);

            log.info("Order completed successfully, XID: {}", xid);
            return OrderResponse.success(xid);

        } catch (Exception e) {
            log.error("Order failed, rolling back. XID: {}, Error: {}", xid, e.getMessage());
            throw new RuntimeException("Order failed: " + e.getMessage(), e);
        }
    }

    private void callPaymentService(OrderRequest request) {
        Map<String, Object> paymentRequest = Map.of(
                "productId", request.productId(),
                "amount", request.amount()
        );

        String response = restClient.post()
                .uri(paymentServiceUrl + "/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(paymentRequest)
                .retrieve()
                .body(String.class);

        log.info("Payment service response: {}", response);
    }

    private void callInventoryService(OrderRequest request) {
        Map<String, Object> inventoryRequest = Map.of(
                "productId", request.productId(),
                "quantity", request.quantity()
        );

        String response = restClient.post()
                .uri(inventoryServiceUrl + "/api/inventory/deduct")
                .contentType(MediaType.APPLICATION_JSON)
                .body(inventoryRequest)
                .retrieve()
                .body(String.class);

        log.info("Inventory service response: {}", response);
    }
}

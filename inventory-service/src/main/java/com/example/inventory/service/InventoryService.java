package com.example.inventory.service;

import com.example.inventory.dto.InventoryRequest;
import com.example.inventory.dto.InventoryResponse;
import com.example.inventory.entity.Inventory;
import com.example.inventory.repository.InventoryRepository;
import org.apache.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public InventoryResponse deductInventory(InventoryRequest request) throws InterruptedException {
        String xid = RootContext.getXID();
        log.info("Deducting inventory in global transaction, XID: {}", xid);
        log.info("Inventory request - productId: {}, quantity: {}", request.productId(), request.quantity());

        Inventory inventory = inventoryRepository.findByProductId(request.productId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + request.productId()));

        if (inventory.getQuantity() < request.quantity()) {
            log.error("Insufficient inventory for product: {}, available: {}, requested: {}",
                    request.productId(), inventory.getQuantity(), request.quantity());
            throw new RuntimeException("Insufficient inventory. Available: " + inventory.getQuantity() +
                    ", Requested: " + request.quantity());
        }

        inventory.setQuantity(inventory.getQuantity() - request.quantity());
        inventoryRepository.save(inventory);
        Thread.sleep(20000);

        log.info("Inventory deducted successfully, remaining: {}", inventory.getQuantity());
        return InventoryResponse.success(request.productId(), inventory.getQuantity());
    }

    @Transactional
    public InventoryResponse deductBySubqueryUpdate(InventoryRequest request) {
        String xid = RootContext.getXID();
        log.info("Deducting inventory via subquery UPDATE, XID: {}", xid);
        log.info("Inventory request - productId: {}, quantity: {}", request.productId(), request.quantity());

        int updatedRows = inventoryRepository.deductBySubqueryUpdate(request.productId(), request.quantity());

        if (updatedRows == 0) {
            throw new RuntimeException("Product not found: " + request.productId());
        }

        Inventory updated = inventoryRepository.findByProductId(request.productId())
                .orElseThrow(() -> new RuntimeException("Product not found after update: " + request.productId()));

        log.info("Inventory deducted via subquery UPDATE, remaining: {}", updated.getQuantity());
        return InventoryResponse.success(request.productId(), updated.getQuantity());
    }
}

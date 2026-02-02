package com.example.inventory.controller;

import com.example.inventory.dto.InventoryRequest;
import com.example.inventory.dto.InventoryResponse;
import com.example.inventory.service.InventoryService;
import org.apache.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/deduct")
    public ResponseEntity<InventoryResponse> deductInventory(
            @RequestHeader(value = RootContext.KEY_XID, required = false) String xid,
            @RequestBody InventoryRequest request) {

        log.info("Received inventory deduction request with XID: {}", xid);

        if (xid != null) {
            RootContext.bind(xid);
        }

        try {
            InventoryResponse response = inventoryService.deductInventory(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Inventory deduction failed", e);
            return ResponseEntity.badRequest()
                    .body(InventoryResponse.failure(request.productId(), e.getMessage()));
        } finally {
            if (xid != null) {
                RootContext.unbind();
            }
        }
    }
}

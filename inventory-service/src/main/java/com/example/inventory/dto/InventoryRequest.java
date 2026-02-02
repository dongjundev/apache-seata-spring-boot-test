package com.example.inventory.dto;

public record InventoryRequest(
        String productId,
        Integer quantity
) {
}

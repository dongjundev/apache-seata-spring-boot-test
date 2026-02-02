package com.example.inventory.dto;

public record InventoryResponse(
        String productId,
        Integer remainingQuantity,
        String status,
        String message
) {
    public static InventoryResponse success(String productId, Integer remainingQuantity) {
        return new InventoryResponse(productId, remainingQuantity, "SUCCESS", "Inventory deducted successfully");
    }

    public static InventoryResponse failure(String productId, String message) {
        return new InventoryResponse(productId, null, "FAILED", message);
    }
}

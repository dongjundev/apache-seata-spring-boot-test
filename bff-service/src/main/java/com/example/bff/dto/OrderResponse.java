package com.example.bff.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 응답 정보")
public record OrderResponse(
        @Schema(
                description = "처리 상태",
                example = "SUCCESS",
                allowableValues = {"SUCCESS", "FAILED"}
        )
        String status,

        @Schema(
                description = "응답 메시지",
                example = "Order completed successfully"
        )
        String message,

        @Schema(
                description = "Seata 글로벌 트랜잭션 ID (실패 시 null)",
                example = "192.168.1.1:8091:123456789",
                nullable = true
        )
        String xid
) {
    public static OrderResponse success(String xid) {
        return new OrderResponse("SUCCESS", "Order completed successfully", xid);
    }

    public static OrderResponse failure(String message) {
        return new OrderResponse("FAILED", message, null);
    }
}

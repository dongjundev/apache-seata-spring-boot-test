package com.example.bff.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "주문 요청 정보")
public record OrderRequest(
        @Schema(
                description = "상품 ID",
                example = "P001",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String productId,

        @Schema(
                description = "주문 수량 (재고: P001=100, P002=50, P003=200)",
                example = "5",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "1"
        )
        Integer quantity,

        @Schema(
                description = "결제 금액",
                example = "99.99",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "0.01"
        )
        BigDecimal amount
) {
}

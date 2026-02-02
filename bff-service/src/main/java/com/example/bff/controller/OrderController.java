package com.example.bff.controller;

import com.example.bff.dto.OrderRequest;
import com.example.bff.dto.OrderResponse;
import com.example.bff.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.lang.Thread.sleep;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order", description = "주문 관리 API - 분산 트랜잭션 테스트")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(
            summary = "주문 생성",
            description = """
                    분산 트랜잭션으로 주문을 생성합니다.

                    **처리 과정:**
                    1. 글로벌 트랜잭션 시작
                    2. Payment Service에 결제 생성
                    3. Inventory Service에서 재고 차감
                    4. 모든 작업 성공 시 커밋, 실패 시 자동 롤백

                    **테스트 시나리오:**
                    - quantity=5: 성공 (재고 충분)
                    - quantity=500: 실패 및 롤백 (재고 부족)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "주문 생성 성공",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "주문 생성 실패 (재고 부족, 결제 실패 등)",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        try {
            OrderResponse response = orderService.createOrder(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(OrderResponse.failure(e.getMessage()));
        }
    }
}

package com.example.bff.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Apache Seata Distributed Transaction API")
                        .version("1.0.0")
                        .description("""
                                Apache Seata AT Mode 분산 트랜잭션 테스트 API

                                ## 아키텍처
                                - **BFF Service** (8080): Transaction Manager (TM)
                                - **Payment Service** (8081): Resource Manager (RM) - PostgreSQL
                                - **Inventory Service** (8082): Resource Manager (RM) - MariaDB

                                ## 트랜잭션 플로우
                                1. BFF가 글로벌 트랜잭션 시작 (@GlobalTransactional)
                                2. Payment Service에 결제 생성 요청
                                3. Inventory Service에 재고 차감 요청
                                4. 모든 서비스 성공 시 커밋, 실패 시 자동 롤백

                                ## 테스트 방법
                                - **성공 케이스**: quantity=5 (재고 충분)
                                - **롤백 케이스**: quantity=500 (재고 부족)
                                """)
                        .contact(new Contact()
                                .name("Apache Seata Spring Boot Test")
                                .url("https://seata.apache.org/"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("BFF Service (Development)")
                ));
    }
}

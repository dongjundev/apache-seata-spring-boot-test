# Apache Seata Spring Boot Test

분산 트랜잭션 테스트 프로젝트 - Apache Seata AT Mode 데모

## 📋 프로젝트 개요

Spring Boot 3.2.5와 Apache Seata 2.3.0을 사용한 마이크로서비스 분산 트랜잭션 구현 예제입니다.

**아키텍처:**
- **BFF Service** (8080): Transaction Manager (TM) - 글로벌 트랜잭션 조정
- **Payment Service** (8081): Resource Manager (RM) - PostgreSQL
- **Inventory Service** (8082): Resource Manager (RM) - MariaDB

## 🚀 빠른 시작

### 1. Docker 컨테이너 시작

```bash
docker-compose up -d
```

### 2. 서비스 빌드

```bash
./mvnw clean package -DskipTests
```

### 3. 서비스 실행 (3개 터미널)

```bash
# Terminal 1
./mvnw spring-boot:run -pl bff-service

# Terminal 2
./mvnw spring-boot:run -pl payment-service

# Terminal 3
./mvnw spring-boot:run -pl inventory-service
```

### 4. 트랜잭션 테스트

**성공 케이스:**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": "P001", "quantity": 5, "amount": 99.99}'
```

**롤백 케이스 (재고 부족):**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": "P001", "quantity": 500, "amount": 99.99}'
```

## 📁 프로젝트 구조

```
.
├── docs/                          # 📄 문서
│   └── QUICK-START.md             # 빠른 시작 가이드
│
├── docker/                        # 🐳 Docker 관련
│   ├── init/                      # 데이터베이스 초기화 스크립트
│   │   ├── payment/
│   │   │   └── init-payment.sql
│   │   ├── inventory/
│   │   │   └── init-inventory.sql
│   │   └── seata/
│   │       └── seata-mysql.sql
│   └── seata-server-config/       # Seata Server 설정
│       ├── application.yml
│       └── jdbc/
│
├── bff-service/                   # 🚀 BFF 서비스 (TM)
├── payment-service/               # 💳 결제 서비스 (RM - PostgreSQL)
├── inventory-service/             # 📦 재고 서비스 (RM - MariaDB)
│
├── docker-compose.yml             # Docker Compose 설정
├── pom.xml                        # Maven 부모 POM
├── mvnw                           # Maven Wrapper
└── README.md                      # 이 파일
```

## 🗄️ 데이터베이스

| 서비스      | DB 타입    | 포트 | 데이터베이스  | 사용자    |
|------------|-----------|------|--------------|-----------|
| Payment    | PostgreSQL| 5432 | paymentdb    | payment   |
| Inventory  | MariaDB   | 3307 | inventorydb  | inventory |
| Seata      | MySQL     | 3306 | seata        | root      |

### 데이터베이스 접속

**PostgreSQL (Payment):**
```bash
docker exec -it payment-postgres psql -U payment -d paymentdb
```

**MariaDB (Inventory):**
```bash
docker exec -it inventory-mariadb mysql -u inventory -pinventory inventorydb
```

**MySQL (Seata):**
```bash
docker exec -it seata-mysql mysql -u root -proot seata
```

## 🛠️ 기술 스택

- **Java**: 21
- **Spring Boot**: 3.2.5
- **Apache Seata**: 2.3.0 (AT Mode)
- **Spring Data JPA**: 3.2.5
- **PostgreSQL**: 15
- **MariaDB**: 10.11
- **MySQL**: 8.0
- **Docker & Docker Compose**

## 📖 상세 문서

- **[QUICK-START.md](docs/QUICK-START.md)** - 빠른 시작 레퍼런스

## 🔍 동작 확인

### 성공 로그

모든 서비스 로그에서 다음 메시지를 확인하세요:

```
✓ register RM success (또는 register TM success)
✓ Started [Service]Application
✓ XID 전파 (동일한 XID가 모든 서비스에서 보임)
```

### 트랜잭션 플로우

**성공 시나리오:**
1. BFF가 글로벌 트랜잭션 시작 (`@GlobalTransactional`)
2. Payment Service에 결제 생성 요청
3. Inventory Service에 재고 차감 요청
4. 모든 서비스 성공 → 커밋

**실패 시나리오:**
1. 재고 부족 등 예외 발생
2. Seata가 모든 브랜치 트랜잭션 롤백
3. `undo_log` 테이블의 before-image로 데이터 복원

## 🧹 초기화

전체 재시작:
```bash
docker-compose down -v
docker-compose up -d
./mvnw clean package -DskipTests
```

## 🐛 트러블슈팅

### 포트 충돌
```bash
lsof -ti:8080,8081,8082,5432,3306,3307 | xargs kill -9
```

### 데이터베이스 연결 실패
```bash
docker logs payment-postgres
docker logs inventory-mariadb
docker-compose ps  # 모두 (healthy) 상태 확인
```

### Seata 연결 실패
```bash
docker logs seata-server
# Seata Server가 8091 포트에서 정상 실행 중인지 확인
```

## 📝 라이센스

이 프로젝트는 학습 목적의 데모 프로젝트입니다.

## 🔗 참고 자료

- [Apache Seata 공식 문서](https://seata.apache.org/)
- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Seata AT Mode](https://seata.apache.org/docs/dev/mode/at-mode/)

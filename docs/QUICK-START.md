# Quick Start Guide

Apache Seata 분산 트랜잭션 테스트 프로젝트 빠른 시작 가이드

## 📋 Prerequisites

- Docker and Docker Compose
- Java 21
- Maven (wrapper included)

## 🚀 Start Everything

### 1. Start Databases

```bash
docker-compose up -d
```

Wait ~10 seconds for all containers to be healthy:
```bash
docker-compose ps
```

**Expected:** All containers show `(healthy)` status

### 2. Build Services

```bash
./mvnw clean package -DskipTests
```

### 3. Start Services (3 terminals)

```bash
# Terminal 1 - BFF Service (Transaction Manager)
./mvnw spring-boot:run -pl bff-service

# Terminal 2 - Payment Service (PostgreSQL)
./mvnw spring-boot:run -pl payment-service

# Terminal 3 - Inventory Service (MariaDB)
./mvnw spring-boot:run -pl inventory-service
```

**Success Indicators in Logs:**
- ✓ "register TM success" (BFF)
- ✓ "register RM success" (Payment, Inventory)
- ✓ "Started [Service]Application"

### 4. Test Transaction

**Success Case:**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": "P001", "quantity": 5, "amount": 99.99}'
```

**Expected:**
- Payment created in PostgreSQL
- Inventory reduced in MariaDB
- Global transaction committed
- Same XID in all service logs

**Rollback Case (Insufficient Inventory):**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": "P001", "quantity": 500, "amount": 99.99}'
```

**Expected:**
- Transaction fails
- All changes rolled back automatically
- No payment record
- Inventory unchanged

## 🗄️ Database Access

**PostgreSQL (Payment Service):**
```bash
# Connect
docker exec -it payment-postgres psql -U payment -d paymentdb

# Check tables
\dt

# Query data
SELECT * FROM payment;
SELECT * FROM undo_log;

# Exit
\q
```

**MariaDB (Inventory Service):**
```bash
# Connect
docker exec -it inventory-mariadb mysql -u inventory -pinventory inventorydb

# Check tables
SHOW TABLES;

# Query data
SELECT * FROM inventory;
SELECT * FROM undo_log;

# Exit
EXIT;
```

**MySQL (Seata Server):**
```bash
# Connect
docker exec -it seata-mysql mysql -u root -proot seata

# Check transaction states
SELECT * FROM global_table;
SELECT * FROM branch_table;
SELECT * FROM lock_table;

# Exit
EXIT;
```

## 🧹 Clean Reset

Complete restart:
```bash
docker-compose down -v
docker-compose up -d
```

Build cleanup:
```bash
./mvnw clean
./mvnw clean package -DskipTests
```

## 📐 Architecture

| Component | Port | Role | Database |
|-----------|------|------|----------|
| BFF Service | 8080 | Transaction Manager (TM) | - |
| Payment Service | 8081 | Resource Manager (RM) | PostgreSQL:5432 |
| Inventory Service | 8082 | Resource Manager (RM) | MariaDB:3307 |
| Seata Server | 8091 | Transaction Coordinator (TC) | MySQL:3306 |

## 🗄️ Databases

| Service   | Type       | Port | Database     | User      | Password  |
|-----------|------------|------|--------------|-----------|-----------|
| Payment   | PostgreSQL | 5432 | paymentdb    | payment   | payment   |
| Inventory | MariaDB    | 3307 | inventorydb  | inventory | inventory |
| Seata     | MySQL      | 3306 | seata        | root      | root      |

## 📁 Project Structure

```
.
├── docs/                          # 📄 Documentation
├── docker/                        # 🐳 Docker configs
│   ├── init/                      # DB initialization scripts
│   │   ├── payment/               # PostgreSQL init
│   │   ├── inventory/             # MariaDB init
│   │   └── seata/                 # Seata MySQL init
│   └── seata-server-config/       # Seata Server config
├── bff-service/                   # Transaction Manager
├── payment-service/               # Payment RM
└── inventory-service/             # Inventory RM
```

## ✅ Verification Checklist

### Docker Containers
```bash
docker-compose ps
```
- [ ] All containers running
- [ ] All containers healthy
- [ ] No error logs

### Databases
```bash
# PostgreSQL
docker exec payment-postgres psql -U payment -d paymentdb -c "\dt"
# Expected: payment, undo_log (with id column)

# MariaDB
docker exec inventory-mariadb mysql -u inventory -pinventory inventorydb -e "SHOW TABLES;" 2>&1 | grep -v Warning
# Expected: inventory, undo_log (with id column)
```

### Services
- [ ] BFF started on 8080
- [ ] Payment started on 8081
- [ ] Inventory started on 8082
- [ ] All show "register success" in logs
- [ ] No connection errors

### Transactions
- [ ] Success case: Payment created, inventory reduced
- [ ] Rollback case: All changes reverted
- [ ] XID appears in all service logs
- [ ] undo_log entries created and cleaned

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Kill processes on ports
lsof -ti:8080,8081,8082 | xargs kill -9

# Or kill specific port
lsof -ti:8080 | xargs kill -9
```

### Database Connection Failed
```bash
# Check container logs
docker logs payment-postgres
docker logs inventory-mariadb
docker logs seata-mysql

# Check container health
docker-compose ps
```

### Tables Not Found
```bash
# Recreate containers (will run init scripts)
docker-compose down -v
docker-compose up -d

# Wait for healthy status
sleep 10
docker-compose ps
```

### Seata Connection Failed
```bash
# Check Seata Server
docker logs seata-server

# Verify Seata is running on 8091
curl http://localhost:7091/health

# Check service configuration
# services should have: seata.service.grouplist.default=localhost:8091
```

### "column id does not exist" Error
This means undo_log table is missing the id column. Recreate:

```bash
# PostgreSQL
docker exec payment-postgres psql -U payment -d paymentdb -c "DROP TABLE IF EXISTS undo_log;"
docker-compose restart payment-postgres

# MariaDB
docker exec inventory-mariadb mysql -u inventory -pinventory inventorydb -e "DROP TABLE IF EXISTS undo_log;"
docker-compose restart inventory-mariadb
```

### Service Won't Start
```bash
# Check for Java process conflicts
jps -l

# Clean build
./mvnw clean
./mvnw clean package -DskipTests

# Check application.yml configuration
# Verify database URLs match docker-compose ports
```

## 📊 Transaction Flow

### Success Flow
1. **BFF** receives order request
2. **BFF** starts global transaction (`@GlobalTransactional`)
3. **BFF** → **Payment Service**: Create payment
   - XID propagated via HTTP header
   - Payment record inserted
   - undo_log created
4. **BFF** → **Inventory Service**: Deduct inventory
   - XID propagated via HTTP header
   - Inventory reduced
   - undo_log created
5. **All succeed** → Seata commits all branches
6. undo_log entries deleted

### Rollback Flow
1. **BFF** receives order request (quantity > available)
2. **BFF** starts global transaction
3. **BFF** → **Payment Service**: Create payment ✓
4. **BFF** → **Inventory Service**: Deduct inventory ✗
   - Throws `InsufficientInventoryException`
5. **Exception caught** → Seata triggers rollback
6. **Seata** uses undo_log to restore:
   - Payment record deleted
   - Inventory restored
7. undo_log entries deleted
8. Global transaction rolled back

## 🔗 Related Documentation

- [README.md](../README.md) - Main project documentation
- [Apache Seata Docs](https://seata.apache.org/)

# Ledgerly – Distributed Order & Payment Orchestration (Spring Boot)

Ledgerly is a **production-style backend system** that demonstrates how to safely process
orders and payments in a **distributed environment** using modern backend patterns.

The project focuses on **data consistency**, **exactly-once semantics**, and **failure-resilient orchestration** —
the kind of problems faced by real financial and e-commerce platforms.

This repository is intentionally designed to be understandable by **both technical and non-technical reviewers**.

---

## What This Project Demonstrates

### For Engineers
- Transactional Outbox Pattern (PostgreSQL → Kafka)
- Saga Orchestration (payment + stock lifecycle)
- Exactly-Once Processing (Inbox + Redis deduplication)
- Optimistic Locking & versioned events
- Distributed Locks (Redis)
- CQRS (Write model in PostgreSQL, Read model in MongoDB)
- Kafka consumers with manual acknowledgment & poison message handling
- Idempotent HTTP APIs
- Kubernetes & Docker deployment awareness

### For Non-Engineers / Reviewers
- How an order is safely paid, reserved, and completed
- How failures are handled without double-charging
- How the system prevents duplicate processing
- How the system remains consistent even if parts fail
- That the system can scale and be deployed in the cloud

---

## High-Level Business Flow

1. A client creates an Order
2. Payment is authorized (external provider simulation)
3. Stock is reserved
4. Payment is captured
5. Order is completed

If any step fails:
- The system compensates (e.g. voids payment)
- The order is cancelled safely
- No duplicate processing occurs

---

## Architecture Overview

Client  
→ Order API (Spring Boot)  
→ PostgreSQL (Orders, Outbox, Inbox)  
→ Kafka  
→ Saga Orchestrator & Read Model Projector  
→ MongoDB (CQRS Read Model)

Supporting components:
- Redis for distributed locks and deduplication
- Kafka for event streaming
- PostgreSQL for strong consistency
- MongoDB for fast read models

---

## Data Consistency & Safety

### Exactly-Once Processing
- Every Kafka consumer uses an Inbox table
- Duplicate messages are ignored safely
- Redis is used as an additional deduplication store

### Transactional Outbox
- Events are written to the database in the same transaction
- Kafka publishing happens asynchronously
- Guarantees no lost or duplicated events

### Optimistic Locking
- Orders use a version field
- Events carry the order version
- Read models ignore out-of-order or stale events

---

## Saga Orchestration

Ledgerly uses orchestration-based Saga.

Each step:
- Executes locally
- Emits the next command
- Has a compensation path

Examples:
- Payment authorized → stock reservation
- Stock reservation fails → payment void
- Capture fails → order cancelled

This mirrors real payment systems.

---

## Money Safety

- Monetary values use BigDecimal
- Currency codes follow ISO-4217
- No floating-point arithmetic is used

This is critical for financial correctness.

---

## Idempotency

- All write APIs require an Idempotency-Key header
- Repeating the same request returns the same response
- Side effects are not repeated
- Different payload with the same key is rejected

---

## Runtime Environment

The system runs using:
- PostgreSQL
- Kafka
- Redis
- MongoDB

All dependencies are provided via Docker Compose for local execution.

---

## Running with Docker (Recommended)

### Build the application (tests skipped)
./gradlew clean bootJar -x test

### Start the full system
docker compose up --build

Application will be available at:
http://localhost:8080

---

## Example API Call

curl -X POST http://localhost:8080/api/orders
-H "Content-Type: application/json"
-H "Idempotency-Key: demo-1"
-d '{
"externalOrderId": "ORDER-123",
"totalAmount": "100.00",
"totalCurrency": "EUR",
"items": [
{ "sku": "SKU-1", "quantity": 1 }
]
}'

---

## Kubernetes Support

This repository includes Kubernetes manifests demonstrating:
- Namespaces
- ConfigMaps
- Secrets
- PostgreSQL
- Kafka (KRaft)
- Redis
- MongoDB
- Application Deployment & Services

These files demonstrate cloud deployment knowledge without requiring
an actual cloud account or cost.

---

## Cloud & AWS Awareness

The project does not require any cloud account.
However, it is designed to be compatible with:
- AWS EKS
- Managed PostgreSQL
- Managed Kafka
- Managed Redis

Environment variables follow cloud-native conventions.

---

## Java & Build Requirements

- Java 11 is required
- Gradle 6.8.3 is used
- Tests are intentionally skipped for demo/runtime focus

---

## Why This Project Exists

This project demonstrates:
- Real-world backend engineering skills
- Financial correctness
- Distributed system thinking
- Cloud and deployment awareness

---

## Author
Merve Döker

---

## Final Note for Reviewers

This repository is intentionally focused on
**correctness, consistency, and resilience** rather than toy examples.

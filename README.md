# Commerce Platform

A learning-focused Java Spring Boot microservices e-commerce platform demonstrating service separation, database-per-service, REST communication, Kafka-based asynchronous communication, and an API Gateway.

## V1 Architecture

```text
Client
  |
  v
API Gateway :8080
  |
  +--> User Service :8081 ------> userdb
  |
  +--> Product Service :8082 ---> productdb
  |
  +--> Order Service :8084 -----> orderdb
  |       |
  |       +-- REST --> User Service
  |       +-- REST --> Product Service
  |       |
  |       +-- Order Created Event --> Kafka
  |                                      |
  |                 +--------------------+-------------------+
  |                 |                    |                   |
  |                 v                    v                   v
  |          Payment Service      Product Service     Notification Service
  |              :8083                :8082                 :8085
  |                |                    |                    |
  |            paymentdb            productdb          notificationdb
  |
  +--> other routed services
```

## Services

| Service | Port | Responsibility | Database |
|---|---:|---|---|
| API Gateway | 8080 | Entry point and routing | - |
| User Service | 8081 | User management | `userdb` |
| Product Service | 8082 | Products and stock | `productdb` |
| Payment Service | 8083 | Payment processing | `paymentdb` |
| Order Service | 8084 | Order creation/orchestration | `orderdb` |
| Notification Service | 8085 | Notification processing | `notificationdb` |
| Common Packages | - | Shared DTOs/events/common code | - |

## Current V1 Flow

### Order validation

```text
Client
  |
  v
Order Service
  |
  +-- REST --> User Service     -> validate user
  |
  +-- REST --> Product Service  -> validate product + stock
  |
  v
Order DB
```

### Kafka flow

After an order is created, asynchronous communication is handled through Kafka:

```text
Order Service
     |
     | Order Created Event
     v
   Kafka
     |
     +--> Product Service       -> update stock
     |
     +--> Payment Service       -> process payment
     |
     +--> Notification Service -> create notification

Payment Service
     |
     | Payment result event
     v
   Kafka
     |
     v
relevant consumers
```

### Database isolation

Each service owns its own H2 database:

```text
User Service          -> userdb
Product Service       -> productdb
Order Service         -> orderdb
Payment Service       -> paymentdb
Notification Service  -> notificationdb
```

Services communicate through REST and Kafka rather than directly accessing another service's database.

## Technology Stack

- Java
- Spring Boot
- Spring Data JPA / Hibernate
- H2
- Apache Kafka
- Zookeeper-based Kafka setup
- Spring Kafka
- Spring Cloud Gateway
- Maven
- Docker / Docker Compose

## Prerequisites

Install:

- Java
- Maven
- Docker
- Docker Compose

## Run Locally

### 1. Build

From the project root:

```bash
mvn clean install
```

### 2. Start Kafka/Zookeeper

```bash
docker compose up -d
```

```bash
docker compose up -d zookeeper kafka kafka-init-topics
```
Verify:

```bash
docker ps
```

### 3. Start Spring Boot services

Start the applications from the IDE or Maven.

```text
API Gateway          :8080
User Service         :8081
Product Service      :8082
Payment Service      :8083
Order Service        :8084
Notification Service :8085
```

### 4. Stop infrastructure

```bash
docker compose down
```

## H2 Console

```text
http://localhost:8081/h2-console
http://localhost:8082/h2-console
http://localhost:8083/h2-console
http://localhost:8084/h2-console
http://localhost:8085/h2-console
```

Each service uses its own in-memory H2 database.

## Testing

Basic User/Product checks:

```bash
curl.exe -i http://localhost:8081/api/v1/users/1
curl.exe -i http://localhost:8082/api/v1/products/1
```

### Invalid User

```bash
curl.exe -i -X POST http://localhost:8084/api/v1/orders -H "Content-Type: application/json" -d "{"userId":99999,"items":[{"productId":1,"quantity":1,"price":100}]}"
```

### Invalid Product

```bash
curl.exe -i -X POST http://localhost:8084/api/v1/orders -H "Content-Type: application/json" -d "{"userId":1,"items":[{"productId":99999,"quantity":1,"price":100}]}"
```

### Insufficient Stock

```bash
curl.exe -i -X POST http://localhost:8084/api/v1/orders -H "Content-Type: application/json" -d "{"userId":1,"items":[{"productId":1,"quantity":999999,"price":100}]}"
```

### Successful Order

```bash
curl.exe -i -X POST http://localhost:8084/api/v1/orders -H "Content-Type: application/json" -d "{"userId":1,"items":[{"productId":1,"quantity":1,"price":100}]}"
```

### Through API Gateway

```bash
curl.exe -i http://localhost:8080/api/v1/users/1
curl.exe -i http://localhost:8080/api/v1/products/1
curl.exe -i -X POST http://localhost:8080/api/v1/orders -H "Content-Type: application/json" -d "{"userId":1,"items":[{"productId":1,"quantity":1,"price":100}]}"
```

## V1 Verification Checklist

- [x] Maven build succeeds
- [x] Kafka/Zookeeper start through Docker Compose
- [x] User REST APIs tested
- [x] Product REST APIs tested
- [x] Order -> User validation tested
- [x] Order -> Product validation tested
- [x] Invalid user scenario tested
- [x] Invalid product scenario tested
- [x] Insufficient stock scenario tested
- [x] Successful order tested
- [x] Kafka event flow tested
- [x] Database-per-service verified
- [x] API Gateway routing tested

## Current Design

**Microservices Architecture with Database-per-Service, REST-based synchronous communication, Kafka-based asynchronous event communication, and an API Gateway.**

V1 intentionally keeps the architecture simple so the core microservice boundaries and communication patterns are easy to understand and extend.

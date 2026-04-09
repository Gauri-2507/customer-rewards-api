# Customer Rewards API

Spring Boot REST API that calculates reward points per customer based on purchase transactions over a three-month period.

## Stack

| | |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.13 |
| Spring Data JPA | H2 embedded database |
| Testing | JUnit 5 + Mockito |
| Utilities | Lombok |

## Getting Started

```
git clone https://github.com/Gauri-2507/customer-rewards-api.git
cd customer-rewards-api
```

**On Windows:**
```
mvnw spring-boot:run
```

**On Mac/Linux:**
```
./mvnw spring-boot:run
```

Base URL: `http://localhost:8080`

The H2 database is seeded automatically on startup via `data.sql`.
H2 console: `http://localhost:8080/h2-console` — JDBC URL: `jdbc:h2:mem:rewardsdb`, username: `sa`, password: _(blank)_

## Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/rewards` | Rewards summary for all customers |
| GET | `/api/rewards/{customerId}` | Rewards summary for one customer |
| GET | `/api/transactions` | All transactions |
| POST | `/api/transactions` | Create a transaction |
| DELETE | `/api/transactions/{transactionId}` | Delete a transaction |

### GET /api/rewards

```json
[
  {
    "customerId": "C001",
    "customerName": "Alice Johnson",
    "monthlyPoints": [
      { "month": "JANUARY",  "points": 115 },
      { "month": "FEBRUARY", "points": 250 },
      { "month": "MARCH",    "points": 0   }
    ],
    "totalPoints": 365
  }
]
```

### GET /api/rewards/{customerId}

```json
{
  "customerId": "C001",
  "customerName": "Alice Johnson",
  "monthlyPoints": [
    { "month": "JANUARY",  "points": 115 },
    { "month": "FEBRUARY", "points": 250 },
    { "month": "MARCH",    "points": 0   }
  ],
  "totalPoints": 365
}
```

### GET /api/transactions

```json
[
  {
    "transactionId": "TXN001",
    "customerId": "C001",
    "customerName": "Alice Johnson",
    "amount": 120,
    "transactionDate": "2026-01-05"
  }
]
```

### POST /api/transactions

```json
{
  "transactionId": "TXN011",
  "customerId": "C001",
  "customerName": "Alice Johnson",
  "amount": 85.00,
  "transactionDate": "2026-04-01"
}
```

Returns `201 Created` with the saved transaction and a `Location` header.

### Error Responses

All errors follow this shape:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Customer not found with ID: C999",
  "timestamp": "2026-04-07T10:30:00"
}
```

| Status | Reason |
|---|---|
| 400 | Invalid or missing fields |
| 404 | Customer or transaction not found |
| 500 | Unexpected server error |

## Points Calculation

| Amount | Rule |
|---|---|
| ≤ $50 | 0 points |
| $50 – $100 | 1 pt per dollar over $50 |
| > $100 | 50 pts + 2 pts per dollar over $100 |

Examples: `$75` → 25 pts | `$120` → 90 pts | `$200` → 250 pts

## Running Tests

**On Windows:**
```
mvnw test
```

**On Mac/Linux:**
```
./mvnw test
```

## Project Structure

```
src/main/java/com/rewards/
├── controller/    RewardsController.java          ← REST endpoints
├── service/       RewardsService.java             ← Points calculation & business logic
├── repository/    TransactionRepository.java      ← JPA repository
├── model/         Transaction.java                ← JPA entity
├── dto/           RewardSummary, MonthlyPoints,
│                  TransactionRequest,
│                  ErrorResponse
└── exception/     RewardsException,
                   CustomerNotFoundException,
                   TransactionNotFoundException,
                   GlobalExceptionHandler
```

## Sample Data

| Customer ID | Name | Transactions |
|---|---|---|
| C001 | Alice Johnson | 4 |
| C002 | Michael Chen | 3 |
| C003 | Sarah Williams | 3 |
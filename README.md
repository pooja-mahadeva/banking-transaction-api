# Banking Transaction API

A REST API for managing bank accounts and transactions, built with Java and Spring Boot.

This project models core banking operations — account creation, deposits, withdrawals, and transfers between accounts — with input validation, proper error handling, and unit test coverage for all business logic.

## Why I built this

My professional background is in Java backend development for banking and insurance clients (credit card processing, insurance platforms, remittance systems). Since that work is client-owned and not publicly shareable, this project demonstrates the same kind of backend logic — account balances, transaction integrity, validation — in a small, self-contained, public codebase.

## Tech Stack

- **Java 17**
- **Spring Boot 3** (Web, Data JPA, Validation)
- **H2** in-memory database
- **JUnit 5 + Mockito** for unit testing
- **Maven** for build and dependency management
- **GitHub Actions** for CI (build + tests run automatically on every push)

## Features

- Create an account with an owner name and initial balance
- Deposit and withdraw funds, with balance validation (no overdrafts)
- Transfer funds between two accounts atomically
- View current balance
- View full transaction history per account
- Input validation on all requests (e.g. rejects negative amounts)
- Centralized error handling with meaningful HTTP status codes (404 for missing accounts, 400 for invalid requests/insufficient funds)

## API Endpoints

| Method | Endpoint                                | Description                          |
|--------|------------------------------------------|---------------------------------------|
| POST   | `/api/accounts`                          | Create a new account                  |
| GET    | `/api/accounts/{accountNumber}`          | Get account details                   |
| GET    | `/api/accounts/{accountNumber}/balance`  | Get current balance                   |
| POST   | `/api/accounts/{accountNumber}/deposit`  | Deposit funds                         |
| POST   | `/api/accounts/{accountNumber}/withdraw` | Withdraw funds                        |
| POST   | `/api/accounts/{accountNumber}/transfer` | Transfer funds to another account     |
| GET    | `/api/accounts/{accountNumber}/transactions` | View transaction history          |

### Example: Create an account

```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"ownerName": "Pooja Mahadeva", "initialBalance": 500.00}'
```

### Example: Deposit funds

```bash
curl -X POST http://localhost:8080/api/accounts/ACC-XXXXXXXX/deposit \
  -H "Content-Type: application/json" \
  -d '{"amount": 100.00}'
```

### Example: Transfer between accounts

```bash
curl -X POST http://localhost:8080/api/accounts/ACC-XXXXXXXX/transfer \
  -H "Content-Type: application/json" \
  -d '{"toAccountNumber": "ACC-YYYYYYYY", "amount": 50.00}'
```

## Running Locally

Requirements: Java 17+ and Maven.

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`. An H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:bankingdb`, username: `sa`, no password) for inspecting the in-memory database during development.

## Running Tests

```bash
mvn test
```

Unit tests cover account creation, deposits, withdrawals, transfers, and edge cases such as insufficient balance and transfers to non-existent accounts.

## Project Structure

```
src/main/java/com/pooja/bankingapi/
├── controller/    REST endpoints
├── service/       Business logic (balance rules, transaction recording)
├── repository/    Spring Data JPA repositories
├── model/         JPA entities (Account, Transaction)
├── dto/           Request/response objects with validation
└── exception/     Custom exceptions + centralized error handling
```

## Possible Next Steps

- Add authentication (Spring Security + JWT)
- Add pagination for transaction history on high-volume accounts
- Containerize with Docker for easier deployment
- Add integration tests against a real PostgreSQL instance

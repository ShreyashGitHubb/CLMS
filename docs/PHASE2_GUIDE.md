# Phase 2 Backend Guide

## Current implementation

The Java backend is organized into four layers:

```text
src/
  config/       Database connection and environment settings
  model/        User, equipment, and transaction records
  dao/          Prepared SQL statements and result mapping
  service/      Business rules and database transactions
  exception/    Domain validation errors
  Main.java     JDBC equipment-listing smoke test
```

`BorrowService` currently supports request creation, staff approval, returns, inventory quantity changes, and a `10.00` per-day overdue fine.

## Local setup

1. Complete the MySQL setup in `README.md`.
2. Download MySQL Connector/J and place its jar in `lib/`.
3. Export `CLMS_DB_URL`, `CLMS_DB_USER`, and `CLMS_DB_PASSWORD`.
4. Compile with `javac` and run `Main` using the classpath shown in `README.md`.

## Verification checklist

- [x] Java sources compile on Java 17 without external compile-time APIs.
- [ ] `Main` lists equipment from the configured MySQL database.
- [ ] A student request is inserted with `PENDING` status.
- [ ] A staff approval decrements available inventory.
- [ ] A return increments inventory and calculates the fine.
- [ ] Frontend requests are replaced by HTTP calls to the Java backend.

Never commit database passwords, local connector jars, or generated class files.
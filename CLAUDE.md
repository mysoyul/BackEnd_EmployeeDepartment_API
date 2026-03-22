# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build (skip tests)
./mvnw clean package -Dmaven.test.skip=true

# Run application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run with a specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=test
```

## Database Setup (MariaDB)

The default (`prod`) profile connects to MariaDB. Before running, create the DB and user:

```sql
create database emp_db;
CREATE USER 'boot'@'%' IDENTIFIED BY 'boot';
GRANT ALL PRIVILEGES ON emp_db.* TO 'boot'@'%';
flush privileges;
```

Default connection: `localhost:3307`, DB `emp_db`, user `boot` / password `boot`. Override with env vars: `DB_HOST`, `DB_PORT`, `DB_DATABASE`, `DB_USERNAME`, `DB_PASSWORD`.

The `test` profile uses an in-memory H2 database — no setup required.

Schema is managed by **Flyway**. Migration scripts are split by vendor:
- `src/main/resources/db/migration/mariadb/` — used by `prod` profile
- `src/main/resources/db/specific/h2/` — used by `test` profile

## Architecture

This is a Spring Boot 3.x REST API (Java 17) for Employee-Department management.

**Layer flow:** Controller → Service interface → ServiceImpl → Repository (JPA)

**Package structure** (`net.restapi.emp`):
- `controller/` — `EmployeeController`, `DepartmentController` (base paths `/api/employees`, `/api/departments`)
- `service/` + `service/impl/` — interfaces and implementations
- `repository/` — Spring Data JPA repositories
- `entity/` — JPA entities (`Employee`, `Department`)
- `dto/` — `EmployeeDto`, `DepartmentDto` (used for all request/response bodies)
- `mapper/` — Static mapper classes (`EmployeeMapper`, `DepartmentMapper`) that convert between entity and DTO
- `exception/` — `ResourceNotFoundException` (carries `HttpStatus`), `ErrorObject`, and `DefaultExceptionAdvice` (`@RestControllerAdvice`)
- `config/` — CORS config (`CorsConfig`, `WebConfig`)
- `runner/` — `DatabaseRunner` (logs DB metadata on startup), `EmpDepInsertRunner` (inserts seed data)

**Key relationships:**
- `Employee` has a `@ManyToOne(fetch = LAZY)` to `Department`
- `EmployeeDto` has two response modes: with `departmentId` only (standard) or with a nested `DepartmentDto` (returned by `GET /api/employees/departments`)
- `EmployeeMapper` has two mapping methods: `mapToEmployeeDto` (with departmentId) and `mapToEmployeeDepartmentDto` (with nested DepartmentDto)

**Exception handling:**
- `ResourceNotFoundException` defaults to HTTP 417 when constructed with only a message; pass `HttpStatus.NOT_FOUND` explicitly for 404
- `DefaultExceptionAdvice` handles: `ResourceNotFoundException`, `HttpMessageNotReadableException`, `MethodArgumentNotValidException` (returns field-level validation errors), and fallback `RuntimeException`

## Profiles

| Profile | Database | Flyway location |
|---------|----------|-----------------|
| `prod` (default) | MariaDB at `127.0.0.1:3307` | `db/migration/mariadb` |
| `test` | H2 in-memory | `db/specific/h2` |

## Docker / CI

Build the JAR, then build the Docker image:
```bash
./mvnw clean package -Dmaven.test.skip=true
docker build -t springbootreactjs:0.4 .
docker run -e DB_HOST=... springbootreactjs:0.4
```

The container runs with `--spring.profiles.active=prod`. GitHub Actions (`.github/workflows/maven.yml`) builds and pushes to Docker Hub on pushes/PRs to `main`.

## 추가기능 


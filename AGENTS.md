# AGENTS.md

## Project

This repository contains `supportdesk-api`, a backend-only REST API for managing technical support tickets.

The project is built as a portfolio backend project for junior, trainee, part-time, freelance, QA, application support, IT support, and defensive security-oriented roles.

## Technical Stack

Use:

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Validation
* PostgreSQL
* Docker Compose
* Swagger / OpenAPI
* JUnit

Do not introduce additional dependencies unless they are explicitly requested or clearly justified.

Avoid adding Lombok, MapStruct, Spring Security, JWT, Flyway, Redis, Kafka, Docker production images, AWS, Kubernetes, or frontend code in the MVP.

## Architecture Rules

Use a simple layered architecture:

```text
Controller -> Service -> Repository -> Database
```

Use these package responsibilities:

* `controller`: REST controllers only.
* `service`: business logic.
* `repository`: Spring Data JPA repositories.
* `entity`: JPA entities.
* `dto.request`: request DTOs.
* `dto.response`: response DTOs.
* `exception`: custom exceptions and global exception handling.
* `config`: application configuration.
* `enums`: domain enums.
* `util`: small utility classes only.

Controllers must not contain business logic.

Repositories must not contain business logic.

Do not expose JPA entities directly in API responses.

Use DTOs for request and response payloads.

Keep the code simple enough to explain in a junior backend interview.

## Domain Naming

Use `AppUser` as the Java entity name instead of `User`.

Use `TicketComment` instead of `Comment`.

Use `TicketHistory` for ticket change history.

Use clear enum names:

* `Role`
* `TicketStatus`
* `Priority`

## MVP Constraints

The MVP includes:

* User creation and retrieval.
* Ticket creation and retrieval.
* Filtering tickets by status and priority.
* Assigning tickets to support agents.
* Changing ticket status.
* Adding comments.
* Saving ticket history.
* Basic SLA calculation.
* Request validation.
* Global error handling.
* Swagger/OpenAPI documentation.
* Docker Compose for PostgreSQL.
* Basic tests.

The MVP excludes:

* Frontend.
* Authentication.
* JWT.
* Spring Security.
* Email notifications.
* WebSockets.
* Microservices.
* AWS.
* Kubernetes.

## Code Style

Prefer readable code over clever code.

Use descriptive class, method, and variable names.

Keep methods short when possible.

Validate input using Spring Validation annotations.

Return meaningful HTTP status codes.

Use explicit exception classes for common error cases.

Avoid premature abstraction.

Do not create generic base services or repositories unless there is a real need.

## Testing Expectations

Add tests gradually.

Prioritize service-layer tests first.

Use controller tests when endpoint behavior needs verification.

Each new feature should include at least one basic test when practical.

## Commands

When available, use:

```bash
./mvnw test
```

To run the application locally:

```bash
./mvnw spring-boot:run
```

To start the database:

```bash
docker compose up -d
```

To stop the database:

```bash
docker compose down
```

## Review Guidelines

Before finishing a task, check:

* Does the code compile?
* Are DTOs used instead of exposing entities?
* Are validation annotations present where needed?
* Are errors handled through the global exception handler?
* Is business logic kept out of controllers?
* Are entity relationships simple and understandable?
* Are names consistent with the domain?
* Is the change small enough to be reviewed in one commit?

## Commit Style

Use clear conventional commit messages.

Examples:

```text
docs: define project scope and architecture
chore: initialize Spring Boot project
feat: add user creation endpoint
feat: add ticket creation endpoint
test: add ticket service tests
refactor: simplify ticket status update logic
```

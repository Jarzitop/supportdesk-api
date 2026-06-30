# supportdesk-api

`supportdesk-api` is a backend-only REST API for managing technical support tickets. It is a junior backend portfolio project focused on Java, Spring Boot, PostgreSQL, automated testing, API documentation, and continuous integration.

The project is an MVP intended for learning and portfolio use. It is not presented as production-ready and currently has no frontend or authentication.

## Current Features

### Users

* Create users with requester, support agent, or administrator roles
* List users and retrieve a user by ID

### Tickets

* Create, list, and retrieve tickets
* Filter tickets by status and priority
* Assign tickets to support agents
* Update ticket status
* Calculate a basic SLA deadline from ticket priority

### Comments and history

* Add and list ticket comments
* Record and retrieve ticket assignment and status change history

### API quality

* Request validation and global error handling
* Service, controller, repository integration, request validation, and utility tests
* OpenAPI specification and Swagger UI
* Automated test execution with GitHub Actions CI

## Tech Stack

* Java 21
* Spring Boot
* Spring WebMVC
* Spring Data JPA
* Spring Validation
* PostgreSQL
* Maven with Maven Wrapper
* JUnit, MockMvc, and Mockito
* OpenAPI and Swagger UI
* Docker Compose
* GitHub Actions CI

## Architecture

The API uses a straightforward layered architecture:

```text
Controller -> Service -> Repository -> Database
```

Request and response DTOs define the HTTP contract without exposing JPA entities. Controllers handle HTTP concerns, services contain business rules, and repositories are responsible for persistence.

## Run Locally

### Prerequisites

* Java 21
* Docker with Docker Compose
* No system-wide Maven installation is required; the repository includes the Maven Wrapper

### 1. Start PostgreSQL

From the repository root, start the development database:

```powershell
docker compose up -d
```

The application uses the database configured in `docker-compose.yml`:

```text
Database: supportdesk_db
Username: supportdesk_user
Password: supportdesk_pass
Port: 5432
```

### 2. Prepare the local test database

Integration tests run against PostgreSQL rather than an in-memory database. Create the isolated test role and database once after starting the container:

```powershell
docker compose exec postgres psql -U supportdesk_user -d postgres -c "CREATE USER supportdesk_test_user WITH PASSWORD 'supportdesk_test_pass';"
docker compose exec postgres psql -U supportdesk_user -d postgres -c "CREATE DATABASE supportdesk_test_db OWNER supportdesk_test_user;"
```

The GitHub Actions workflow creates this test database automatically in CI.

### 3. Run the tests

On Windows:

```powershell
.\mvnw.cmd test
```

On Linux or macOS:

```bash
./mvnw test
```

### 4. Run the application

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On Linux or macOS:

```bash
./mvnw spring-boot:run
```

The API starts at `http://localhost:8080`.

### 5. Open the API documentation

With the application running:

* Swagger UI: `http://localhost:8080/swagger-ui.html`
* OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Stop PostgreSQL when it is no longer needed:

```powershell
docker compose down
```

## API Endpoints

All endpoints use the base URL `http://localhost:8080`.

### Users

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/v1/users` | Create a user |
| `GET` | `/api/v1/users` | List all users |
| `GET` | `/api/v1/users/{id}` | Get a user by ID |

### Tickets

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/v1/tickets` | Create a ticket |
| `GET` | `/api/v1/tickets` | List or filter tickets |
| `GET` | `/api/v1/tickets/{id}` | Get a ticket by ID |
| `PATCH` | `/api/v1/tickets/{id}/assign` | Assign a ticket to a support agent |
| `PATCH` | `/api/v1/tickets/{id}/status` | Update a ticket's status |
| `POST` | `/api/v1/tickets/{ticketId}/comments` | Add a comment to a ticket |
| `GET` | `/api/v1/tickets/{ticketId}/comments` | List a ticket's comments |
| `GET` | `/api/v1/tickets/{ticketId}/history` | View a ticket's change history |

Supported enum values:

* Roles: `ADMIN`, `SUPPORT_AGENT`, `REQUESTER`
* Priorities: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
* Ticket statuses: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`

## API Usage Examples

The examples below assume the application is running locally. They form a single flow: the first user is the requester with ID `1`, the second is the support agent with ID `2`, and the created ticket has ID `1`. Use the IDs returned by the API if the database already contains data.

### Create users

Create the requester:

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Ana Torres",
    "email": "ana.torres@example.com",
    "role": "REQUESTER"
  }'
```

Create the support agent used in the assignment and comment examples:

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Carlos Ruiz",
    "email": "carlos.ruiz@example.com",
    "role": "SUPPORT_AGENT"
  }'
```

### List users

```bash
curl http://localhost:8080/api/v1/users
```

### Create a ticket

```bash
curl -X POST http://localhost:8080/api/v1/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Cannot connect to VPN",
    "description": "The VPN client reports a connection timeout.",
    "priority": "HIGH",
    "requesterId": 1
  }'
```

### List tickets

The endpoint returns a paginated response. `page` is zero-based and defaults to `0`; `size` defaults to `20`.

```bash
curl "http://localhost:8080/api/v1/tickets?page=0&size=20"
```

Example response:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

### Filter tickets by status and priority

Both filters are optional and can be used separately or together:

```bash
curl "http://localhost:8080/api/v1/tickets?page=0&size=20&status=OPEN&priority=HIGH"
```

### Get a ticket by ID

```bash
curl http://localhost:8080/api/v1/tickets/1
```

### Assign a ticket to a support agent

The selected user must have the `SUPPORT_AGENT` role.

```bash
curl -X PATCH http://localhost:8080/api/v1/tickets/1/assign \
  -H "Content-Type: application/json" \
  -d '{
    "assignedAgentId": 2
  }'
```

### Update ticket status

```bash
curl -X PATCH http://localhost:8080/api/v1/tickets/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "IN_PROGRESS"
  }'
```

### Add a comment

```bash
curl -X POST http://localhost:8080/api/v1/tickets/1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "authorId": 2,
    "content": "Investigating the VPN gateway logs."
  }'
```

### List comments

The response uses the same paginated structure as ticket listings. `page` is zero-based and defaults to `0`; `size` defaults to `20`.

```bash
curl "http://localhost:8080/api/v1/tickets/1/comments?page=0&size=20"
```

### View ticket history

History responses are also paginated with default values `page=0` and `size=20`.

```bash
curl "http://localhost:8080/api/v1/tickets/1/history?page=0&size=20"
```

## Technical Decisions

* **DTOs instead of exposed JPA entities:** request and response DTOs keep persistence details out of the public API contract and provide a clear place for input validation.
* **Thin controllers:** controllers map HTTP requests and responses, then delegate work to services.
* **Business logic in services:** ticket assignment, status changes, history creation, and SLA calculation remain centralized and testable outside the web layer.
* **Repositories for persistence only:** Spring Data JPA repositories contain data access methods without business rules.
* **PostgreSQL in integration tests:** repository and application-context tests use the same database engine as the application instead of H2, reducing differences in SQL and persistence behavior.
* **`@EntityGraph` for ticket listings:** ticket queries fetch requester and assigned-agent relationships with the listing query to avoid N+1 selects while mapping response DTOs.
* **GitHub Actions CI:** pushes and pull requests to `main` run the Maven test suite with Java 21 and an isolated PostgreSQL service.
* **No Spring Security yet:** authentication and authorization are intentionally outside the current MVP scope so the project can focus on the ticket workflow and backend fundamentals first.

## Roadmap

* Authentication and authorization with Spring Security and JWT
* Pagination and sorting for collection endpoints
* More complete audit information for ticket changes
* Deployment configuration and an initial hosted environment
* Basic observability with health checks, structured logs, and application metrics

## Author

Jose Rojas, Systems and Computing Engineering student at Universidad de Los Andes.

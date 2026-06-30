# supportdesk-api

`supportdesk-api` is a backend-only REST API for managing users and technical support tickets. It is a Java 21 and Spring Boot portfolio project that demonstrates a simple layered architecture, request validation, persistence, error handling, ticket workflows, and automated testing.

The project is an MVP intended for learning and portfolio use. It is not presented as production-ready and currently has no frontend or authentication.

## Implemented Features

### User management

* Create a user
* List all users
* Get a user by ID

### Ticket management

* Create and retrieve tickets
* Filter tickets by status and priority
* Assign tickets to support agents
* Update ticket status
* Add and list ticket comments
* View ticket change history
* Calculate a basic SLA deadline from ticket priority

### API and persistence

* Request validation with meaningful validation errors
* Global handling for common API errors
* PostgreSQL persistence through Spring Data JPA
* Docker Compose configuration for a local PostgreSQL database
* Service, controller, request validation, and utility tests

## Tech Stack

* Java 21
* Spring Boot
* Spring WebMVC
* Spring Data JPA
* Spring Validation
* PostgreSQL
* Maven with Maven Wrapper
* JUnit, MockMvc, and Mockito
* Docker Compose

## Architecture

The API uses a straightforward layered architecture:

```text
Controller -> Service -> Repository -> Database
```

Request and response DTOs keep the HTTP contract separate from the JPA entities. Business rules are handled in the service layer.

## Getting Started

### Prerequisites

* Java 21
* PostgreSQL installed locally, or Docker with Docker Compose
* No system-wide Maven installation is required; the repository includes the Maven Wrapper

### Database

The default application configuration connects to PostgreSQL on `localhost:5432` using:

```text
Database: supportdesk_db
Username: supportdesk_user
Password: supportdesk_pass
```

Start the configured PostgreSQL container from the repository root:

```powershell
docker compose up -d
```

Alternatively, create a local PostgreSQL database and user with the same values. Stop the Docker Compose services with:

```powershell
docker compose down
```

### Run the Application

On Windows, start the application with the Maven Wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

The API is then available at:

```text
http://localhost:8080
```

### Run the Tests

```powershell
.\mvnw.cmd test
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
| `GET` | `/api/v1/tickets` | List all tickets |
| `GET` | `/api/v1/tickets/{id}` | Get a ticket by ID |
| `GET` | `/api/v1/tickets?status=OPEN` | Filter tickets by status |
| `GET` | `/api/v1/tickets?priority=HIGH` | Filter tickets by priority |
| `GET` | `/api/v1/tickets?status=OPEN&priority=HIGH` | Filter tickets by status and priority |
| `PATCH` | `/api/v1/tickets/{id}/assign` | Assign a ticket to a support agent |
| `PATCH` | `/api/v1/tickets/{id}/status` | Update a ticket's status |
| `POST` | `/api/v1/tickets/{ticketId}/comments` | Add a comment to a ticket |
| `GET` | `/api/v1/tickets/{ticketId}/comments` | List a ticket's comments |
| `GET` | `/api/v1/tickets/{ticketId}/history` | View a ticket's change history |

Supported enum values:

* Roles: `ADMIN`, `SUPPORT_AGENT`, `REQUESTER`
* Priorities: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
* Ticket statuses: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`

## Example Requests

### Create a User

`POST /api/v1/users`

```json
{
  "fullName": "Ana Torres",
  "email": "ana.torres@example.com",
  "role": "REQUESTER"
}
```

### Create a Ticket

`POST /api/v1/tickets`

```json
{
  "title": "Cannot connect to VPN",
  "description": "The VPN client reports a connection timeout.",
  "priority": "HIGH",
  "requesterId": 1
}
```

### Assign a Ticket

`PATCH /api/v1/tickets/1/assign`

```json
{
  "assignedAgentId": 2
}
```

The selected user must have the `SUPPORT_AGENT` role.

### Update Ticket Status

`PATCH /api/v1/tickets/1/status`

```json
{
  "status": "IN_PROGRESS"
}
```

### Add a Comment

`POST /api/v1/tickets/1/comments`

```json
{
  "authorId": 2,
  "content": "Investigating the VPN gateway logs."
}
```

## Roadmap

* OpenAPI/Swagger documentation
* Pagination and sorting
* Spring Security and JWT authentication
* Continuous integration with GitHub Actions
* Test database isolation
* Deployment-specific configuration profile

## Author

Jose Rojas, Systems and Computing Engineering student at Universidad de Los Andes.

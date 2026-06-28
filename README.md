# supportdesk-api

Backend REST API for managing technical support tickets, users, agents, comments, basic SLA rules, and ticket change history.

This project is part of my backend development portfolio as a Systems and Computing Engineering student. The goal is to build a clean, professional API using Java and Spring Boot.

## Project Scope

`supportdesk-api` is a backend-only application. It does not include a frontend, authentication system, cloud deployment, or microservices in the initial MVP.

The API allows users to create and manage support tickets, assign tickets to support agents, update ticket status, add comments, calculate basic SLA deadlines, and store important ticket changes in a history table.

## Tech Stack

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Validation
* PostgreSQL
* Docker Compose
* Swagger / OpenAPI
* JUnit
* Postman
* GitHub Actions later

## Main Domain Concepts

* Users
* Tickets
* Comments
* Ticket history
* Roles
* Ticket statuses
* Priorities
* Basic SLA calculation

## MVP Features

* Create users
* List users
* Get user by id
* Create tickets
* List tickets
* Get ticket by id
* Filter tickets by status and priority
* Assign ticket to support agent
* Change ticket status
* Add comments to tickets
* Store ticket change history
* Calculate basic SLA deadline based on priority
* Validate input data
* Handle errors with a global exception handler
* Document endpoints with Swagger/OpenAPI
* Run the database with Docker Compose
* Add basic automated tests

## Out of Scope for the MVP

* Frontend
* Authentication and authorization
* JWT
* Email notifications
* WebSockets
* Microservices
* AWS or cloud deployment
* Kubernetes
* Advanced reporting

## Initial Architecture

The project follows a simple layered backend architecture:

```text
Controller -> Service -> Repository -> Database
```

DTOs are used to avoid exposing JPA entities directly through the API.

Business logic belongs in the service layer.

Repositories should only handle database access.

Controllers should stay thin and only handle HTTP-level concerns.

## Database

The initial database will be PostgreSQL and will run locally through Docker Compose.

## API Documentation

Swagger/OpenAPI will be available once the Spring Boot application is implemented.

Expected local URL:

```text
http://localhost:8080/swagger-ui/index.html
```

## Status

Current phase: Phase 0 — project definition and repository setup.

## Author

Jose Rojas
Systems and Computing Engineering student
Universidad de Los Andes

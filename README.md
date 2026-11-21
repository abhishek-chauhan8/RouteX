# RouteX

RouteX is a backend-focused, multi-tenant API management platform built with Java and Spring Boot. It separates the **control plane**—where platform and tenant administrators define route configuration—from the **data plane**, where a gateway can consume the active configurations and proxy traffic.

The repository currently contains the API Management Service, plus initial Smart Gateway and Analytics services. The API Management Service is the most complete service and is the focus of the implemented functionality.

## Why RouteX?

In a multi-tenant platform, each customer needs to expose APIs safely without being able to read or modify another customer's configuration. RouteX centralizes that responsibility:

- Platform administrators manage organizations and tenant administrators.
- Tenant administrators create, update, activate, deactivate, and inspect their own API routes.
- Every route configuration is scoped to an organization, versioned, and audited.
- A gateway service can retrieve only active routes belonging to active organizations.

## Architecture

```text
                         ┌─────────────────────────────────┐
                         │      API Management Service     │
                         │          (port 8081)            │
                         │                                 │
Platform Admin ────────► │ Organization + tenant lifecycle │
Tenant Admin ──────────► │ Route configuration             │ ──► MySQL
Gateway Service ───────► │ Active-route configuration API  │     (Flyway schema)
                         │ JWT auth • audit • versioning   │
                         └──────────────┬──────────────────┘
                                        │ active route definitions
                                        ▼
                         ┌─────────────────────────────────┐
                         │ Smart Gateway Service (port 8080)│
                         │ Current state: health endpoint   │
                         └─────────────────────────────────┘

                         ┌─────────────────────────────────┐
                         │ Analytics Service (port 8082)    │
                         │ Current state: health endpoint   │
                         └─────────────────────────────────┘
```

### Services

| Service | Port | Responsibility | Current implementation |
| --- | ---: | --- | --- |
| `api-management-service` | 8081 | Control plane: users, organizations, routes, configuration history, and audit records | Core functionality implemented |
| `smart-gateway-service` | 8080 | Runtime data plane that should apply RouteX route definitions | Bootstrap and health endpoint only |
| `analytics-service` | 8082 | Traffic and operational analytics | Bootstrap and health endpoint only |

## Implemented control-plane capabilities

### Multi-tenant organization lifecycle

- Platform administrators can create, list, view, update, change status of, and soft-delete organizations.
- Organizations begin in `PENDING` state. Tenant logins and route operations require the organization to be `ACTIVE`.
- A platform administrator can create, retrieve, update, and remove the single tenant administrator associated with an organization.
- Tenant administrators can view and rename their own organization; they cannot change its slug or lifecycle status.

### Tenant route management

- Create, read, list, update, and delete API routes.
- Create multiple routes in one transactional bulk request.
- Activate or deactivate a route independently of its other configuration.
- Configure matching path, upstream URL and path prefix, allowed HTTP methods, rate limit metadata, timeout, and idempotency metadata.
- Configure request and response header rules with `ADD` or `REMOVE` actions.

### Tenant isolation

Tenant identity comes from the `organizationId` JWT claim. Route lookup, creation conflict checks, version history, and audit queries are all constrained to that organization. A tenant route lookup uses both the route’s primary key and the tenant organization ID, so a tenant cannot retrieve or modify a route belonging to another tenant.

Route path validation adds a second boundary: a tenant’s route pattern must start with `/{organization-slug}/`.

### Configuration history and auditability

- Every single-route create, update, activation/deactivation, and deletion persists a JSON configuration snapshot in `route_configuration_versions`.
- Version records are tenant-scoped and returned newest first.
- Implemented organization lifecycle actions and single-route lifecycle actions record the authenticated actor, action, resource type and ID, optional `X-Correlation-ID`, operation duration, result, details, and timestamp.
- Tenant audit records are returned only for the requesting organization.

## Technology

- Java 21
- Spring Boot 4.1.2
- Spring MVC, Spring Security, OAuth2 Resource Server, Spring Data JPA, Bean Validation
- JWT signed with HS256
- MySQL and Flyway migrations
- Maven Wrapper
- Lombok

## Project structure

```text
RouteX/
├── api-management-service/
│   ├── src/main/java/.../management/
│   │   ├── Config/          # Security and JWT configuration
│   │   ├── Controller/      # HTTP API boundaries
│   │   ├── Service/         # Domain workflows and tenant checks
│   │   ├── Repository/      # Spring Data persistence access
│   │   ├── Entity/          # JPA data model
│   │   ├── Dto/             # Validated request and response shapes
│   │   └── Exception/       # API exception type
│   └── src/main/resources/db/migration/
│       ├── V1__create_api_management_schema.sql
│       ├── V2__seed_management_roles.sql
│       └── V3__seed_platform_accouts.sql
├── smart-gateway-service/
└── analytics-service/
```

The package structure keeps transport concerns in controllers, business rules in services, database access in repositories, and API contracts in DTOs. This keeps the authorization and tenant-isolation rules centralized rather than spread across controllers.

## Data model

| Entity | Purpose |
| --- | --- |
| `Organization` | Tenant identity, stable slug, lifecycle status, and soft-delete timestamp |
| `ManagementUsers` | Platform, tenant, and gateway service identities; passwords are BCrypt hashes |
| `Role` | `PLATFORM_ADMIN`, `TENANT_ADMIN`, and `GATEWAY_SERVICE` authorization roles |
| `ApiRoute` | The tenant-owned route configuration |
| `RouteHeaderRule` | Request/response header mutation rules attached to a route |
| `RouteConfigurationVersion` | Immutable JSON snapshot of a route at a lifecycle event |
| `ControlPlaneAuditRecord` | Audit event for a control-plane action |

## Security model

`POST /api/auth/login` validates a username and BCrypt password hash, then issues an HS256 JWT with:

- subject: username
- role: `PLATFORM_ADMIN`, `TENANT_ADMIN`, or `GATEWAY_SERVICE`
- organization ID: present for tenant administrators
- configured expiry: 3,600 seconds by default

Spring Security is stateless. It permits health checks and login, then authorizes endpoint families by role:

| Endpoint family | Required role |
| --- | --- |
| `/api/platform/**` | `PLATFORM_ADMIN` |
| `/api/tenant/**` | `TENANT_ADMIN` |
| `/api/gateway/**` | `GATEWAY_SERVICE` |

> For production, move the database password and JWT secret out of `application.properties` and into environment-specific secrets. Rotate the JWT signing secret before any deployment.

## Prerequisites

- JDK 21
- MySQL 8+ running locally
- Maven is supplied through each service’s Maven Wrapper

Create a local database before starting the management service:

```sql
CREATE DATABASE routeX_management;
```

The current development defaults are in `api-management-service/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/routeX_management
spring.datasource.username=root
spring.datasource.password=root
```

Flyway applies the schema and seed migrations automatically. Adjust these settings locally before running the service if your database differs.

## Run locally

Start each service from its own directory:

```bash
cd api-management-service
./mvnw spring-boot:run
```

```bash
cd smart-gateway-service
./mvnw spring-boot:run
```

```bash
cd analytics-service
./mvnw spring-boot:run
```

Health endpoints:

```bash
curl http://localhost:8081/health
curl http://localhost:8080/health
curl http://localhost:8082/health
```

Run the available test suites with:

```bash
./mvnw test
```

Run this command inside each service directory. The currently committed tests are Spring application-context smoke tests; they are a starting point, not a full behavioral test suite.

## API overview

All protected calls use:

```http
Authorization: Bearer <JWT>
```

### Authentication

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/auth/login` | Exchange username and password for a JWT |

```json
{
  "username": "tenant-admin",
  "password": "your-password"
}
```

### Platform organization administration

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/platform/organizations` | Create organization in `PENDING` state |
| `GET` | `/api/platform/organizations` | List non-deleted organizations |
| `GET` | `/api/platform/organizations/{id}` | Get organization |
| `PUT` | `/api/platform/organizations/{id}` | Update name and slug |
| `PATCH` | `/api/platform/organizations/{id}/status` | Set `PENDING`, `ACTIVE`, or `SUSPENDED` |
| `DELETE` | `/api/platform/organizations/{id}` | Soft-delete organization |
| `POST/GET/PUT/DELETE` | `/api/platform/organizations/{id}/tenant-admin` | Manage that organization’s tenant admin |

### Tenant APIs

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/tenant/organization` | Get the authenticated tenant’s organization |
| `PUT` | `/api/tenant/organization` | Rename the authenticated tenant’s organization |
| `POST` | `/api/tenant/routes` | Create a route |
| `POST` | `/api/tenant/routes/bulk` | Create routes atomically |
| `GET` | `/api/tenant/routes` | List the authenticated tenant’s routes |
| `GET` | `/api/tenant/routes/{id}` | Get an owned route |
| `PUT` | `/api/tenant/routes/{id}` | Update an owned route; `routeId` is immutable |
| `PATCH` | `/api/tenant/routes/{id}/status` | Activate or deactivate an owned route |
| `DELETE` | `/api/tenant/routes/{id}` | Delete an owned route after storing a final version |
| `GET` | `/api/tenant/route-versions/{routeId}` | Get tenant-scoped route history |
| `GET` | `/api/tenant/audit-records` | Get tenant-scoped control-plane audit records |

### Gateway configuration API

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/gateway/routes` | Get active routes from active organizations |

### Route request example

When the authenticated tenant has slug `acme`, its route pattern must begin with `/acme/`.

```json
{
  "routeId": "orders-v1",
  "name": "Orders API",
  "pathPattern": "/acme/orders/**",
  "targetBaseUrl": "https://orders.internal.example",
  "targetPathPrefix": "/api/v1",
  "requestsPerMinute": 1000,
  "responseTimeoutMs": 3000,
  "idempotencyEnabled": true,
  "active": false,
  "allowedMethods": ["GET", "POST"],
  "headerRules": [
    {
      "direction": "REQUEST",
      "action": "ADD",
      "headerName": "X-Tenant-ID",
      "headerValue": "acme"
    },
    {
      "direction": "RESPONSE",
      "action": "REMOVE",
      "headerName": "Server"
    }
  ]
}
```

Validation rejects a non-HTTP(S) upstream URL, an invalid HTTP method, a target path prefix without a leading `/`, a route outside the tenant prefix, an invalid header direction/action, and an `ADD` header rule without a value.

## Interview walkthrough

### 60-second version

> RouteX is a multi-tenant API management backend. I designed the API Management Service as a control plane where a platform administrator onboards organizations and tenant administrators manage only their own routes. Authentication is stateless JWT-based, and the tenant ID in the token is used as the boundary for every route query and mutation. A route has operational metadata such as methods, rate-limit metadata, timeout, idempotency, and request/response header rules. To make changes traceable, every route lifecycle change stores a configuration snapshot and a control-plane audit record. A gateway-facing endpoint exposes active routes for active organizations. The repository also has gateway and analytics service foundations, which are the next services to complete.

### Detailed explanation

**Problem and users.** A shared API gateway is risky when every tenant can configure public paths. RouteX treats routing as a control-plane concern: platform staff own tenant lifecycle, while tenant staff own only their routes. The runtime gateway is deliberately separate so it can operate with a small, read-only configuration surface.

**Core design decision: tenant isolation.** The important security boundary is not a client-supplied `organizationId`. After a tenant administrator logs in, the service places the organization ID in the signed JWT. `TenantContext` reads that claim and verifies that the organization still exists and is active. Service methods then query by both route and organization identity. This prevents insecure direct object reference issues where one tenant guesses another route’s database ID.

**Route lifecycle.** A tenant creates a route with a stable `routeId`, match path, upstream destination, runtime metadata, allowed methods, and header rules. The service validates tenant path ownership and HTTP configuration, writes the route, and saves configuration version 1. Updates keep `routeId` immutable and create a new version. Activation, deactivation, and deletion also increase the version and record an immutable snapshot. The gateway queries only routes that are active and whose organization is active.

**Auditability.** Route history answers “what configuration existed at a point in time?” Control-plane audit records answer “who performed which action, on what resource, when, with what correlation ID, and how long did it take?” These are separate intentionally: one is configuration state, and the other is an operational event stream.

**Persistence and schema safety.** JPA represents the domain; MySQL stores the data; Flyway owns schema creation and seed data. The application uses `ddl-auto=validate`, so it validates that entity mappings match the migration-managed schema rather than changing the schema implicitly at startup.

**What I would build next.** I would implement gateway route refresh/caching and proxy execution, enforce rate limiting and header rules in that gateway, emit traffic events to Kafka for analytics, add integration tests with Testcontainers, add pagination/filtering to audit/history APIs, introduce secret management and key rotation, and move gateway credentials to a service-to-service authentication mechanism. These steps turn the current control-plane foundation into a production-ready API platform.

## Current scope and next steps

The management service has the route-management foundation described above. Bulk creation persists routes transactionally, but it does not yet create per-route version snapshots or audit events; that should be completed before treating bulk import as fully auditable. The gateway does not yet proxy requests, apply header rules, enforce limits, or refresh configuration; it currently exposes only a health endpoint. The analytics service also currently exposes only a health endpoint. Kafka dependencies are present in the management service but no event producer/consumer workflow is implemented yet.

Recommended next milestones:

1. Implement gateway configuration polling or event-driven refresh and runtime route matching/proxying.
2. Enforce request limits, timeouts, idempotency behavior, and header rules in the gateway.
3. Publish gateway traffic/audit events and implement analytics aggregation.
4. Add controller, service, repository, security, and cross-tenant integration tests (preferably with Testcontainers for MySQL).
5. Add production configuration profiles, secret management, observability, error response standards, pagination, and CI.

## Notes

- This repository includes a small static UI under the API Management Service, but RouteX’s current work and documentation focus on the backend APIs.
- The Flyway seed migration includes usernames for bootstrap platform and gateway accounts, but it stores password hashes only. Do not publish or depend on development credentials; provision secure credentials for your environment.

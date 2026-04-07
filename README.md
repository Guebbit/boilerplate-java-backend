# Boilerplate Java Backend

Spring Boot + MongoDB REST API generated from `Blueprint.md` and aligned to `openapi.yaml`.

## Architecture

- Controllers: HTTP mapping and auth composition
- Services: domain business rules and orchestration
- Repositories: Mongo persistence only
- Models: Mongo documents, embedded cart/order/token items
- Common: response envelope and global error handling

## Environment

Copy `.env.example` and provide:

- `NODE_DB_URI`
- `NODE_ACCESS_TOKEN_SECRET`
- `NODE_REFRESH_TOKEN_SECRET`

## Run

```bash
mvn spring-boot:run
```

## Test

```bash
mvn test
```

## API domains

- Account/Auth
- Users (admin)
- Products (public read, admin write)
- Cart (auth)
- Orders (auth with owner/admin scoping)
- System health (`/health`)

## Contract and behavior

- Endpoints and payload shapes based on `openapi.yaml`
- Role and ownership rules, token lifecycle, soft delete, checkout flow based on `Blueprint.md`
- Uniform envelope for JSON responses:
  - success
  - status
  - message
  - data/errors

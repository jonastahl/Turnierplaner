# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

### Backend (Maven / Quarkus)

```shell
# Dev mode (starts Keycloak + DB via Dev Services, hot reload)
mvn compile quarkus:dev

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=TestCompetitionResource

# Full build (backend + frontend)
mvn package

# Build without recompiling frontend
mvn package -DnoFrontendComp

# Build without recompiling backend
mvn package -DnoBackendComp

# Format Java code (also runs automatically before compile)
mvn spotless:apply
```

### Frontend (in `src/main/webui/`)

```shell
# Dev server (proxies API to localhost:2000)
npm run dev

# Type check + build
npm run build

# Lint + format check
npm run format:validate

# Auto-fix lint + format
npm run format:fix
```

### Production

```shell
docker compose up   # runs at http://localhost:8080
```

## Architecture

### Stack

- **Backend**: Quarkus 3.x, Java 25, JAX-RS, Hibernate ORM + Panache, PostgreSQL
- **Frontend**: Vue 3 + TypeScript, Vite, TanStack Vue Query, PrimeVue, vue-i18n (DE/EN)
- **Auth**: Keycloak (OIDC); dev mode auto-starts a Keycloak container via Dev Services
- **Frontend served by**: Quinoa (Maven plugin; embeds the Vue build inside the Quarkus jar)

### Domain model

```
Tournament
  └── Competition (many per tournament)
        ├── mode: KNOCKOUT | GROUP_SYSTEM
        ├── type: SINGLE | DOUBLE (singles vs. doubles)
        ├── CreationProgress: TEAMS → GAMES → SCHEDULING → PUBLISHING → DONE
        ├── Team (signed-up participant; has playerA and optionally playerB)
        └── Match → Set (scores)
```

`Group`, `MatchOfGroup`, `FinalOfGroup`, and `NextMatch` (knockout tree node) are the sub-entities for the two tournament modes.

### Backend layers (`src/main/java/de/secretj12/turnierplaner/`)

| Package | Role |
|---|---|
| `resources/` | JAX-RS REST endpoints — thin, delegates to logic |
| `logic/` | Business logic (e.g. `CompetitionLogic`) — `@Transactional` lives here |
| `db/entities/` | JPA entities (plain getters/setters, no business logic) |
| `db/repositories/` | Panache repositories with named queries |
| `model/user/` | DTOs returned to any authenticated user |
| `model/director/` | DTOs accepted/returned only to directors (admins) |
| `tools/` | Pure computation: `KnockoutTools`, `GroupTools`, `CommonHelpers` |
| `mails/` | Quarkus Mailer + Qute templates for transactional email |
| `enums/` | Shared enumerations (`CompetitionMode`, `CompetitionType`, `CreationProgress`, …) |
| `startup/` | `Startup` (observes `StartupEvent`): seeds defaults and optionally test data |

### Frontend layers (`src/main/webui/src/`)

| Directory | Role |
|---|---|
| `backend/` | All API calls — `axios` + TanStack Vue Query (`useQuery`/`useMutation`) |
| `interfaces/` | TypeScript types mirroring backend DTOs; conversion helpers (`*ServerToClient`) |
| `components/pages/` | Feature page components, organized by domain (tournament, competition, management, …) |
| `security/` | OIDC/Keycloak integration (`AuthService.ts`, OIDC callbacks) |
| `i18n/` | Translation files (`de.json`, `en.json`) |
| `routes.ts` | Vue Router route definitions and `Routes` enum |

The `backend/Tracker.ts` utility smoothly animates reactive data transitions (used in schedule/plan views).

### Competition lifecycle

A competition progresses through `CreationProgress` states gated by director actions:
1. **TEAMS** – director edits which teams/players participate
2. **GAMES** – director assigns match brackets (knockout order or group division)
3. **SCHEDULING** – director drags matches onto courts/timeslots (calendar)
4. **PUBLISHING** – director publishes so players see the schedule
5. **DONE** – competition completed

### Test setup

Tests use Quarkus Dev Services (Testcontainers) to spin up a real PostgreSQL instance. OIDC is disabled in test mode (`%test.quarkus.oidc.enabled=false`); security is simulated via `@TestSecurity`. The DB schema is dropped and recreated each dev/test run (`drop-and-create`); production uses `update`.

### Formatting

Java code is formatted automatically via **Spotless** (Eclipse formatter config in `formatJava.xml`) during the Maven `validate` phase — it runs before every compile. Frontend uses **ESLint + Prettier** (`npm run format:fix`).

### Key configuration

`src/main/resources/application.properties` — backend defaults. Notable properties:

- `turnierplaner.testdata=true` (set automatically in `%dev`) — seeds random test tournaments/players on startup
- `turnierplaner.admin-verification-needed` — whether new player registrations need admin approval
- Mail credentials (`MAIL_FROM`, `MAIL_HOST`, `MAIL_USER`, `MAIL_PASS`) must be set in a root `.env` file for mail to work

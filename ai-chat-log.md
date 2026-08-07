# AI Conversation Log & Development History

This document records the interaction log with the AI Coding Assistant (Pi Agent powered by Gemini Flash model) during the planning, design, implementation, and refinement of the **Employee Feedback Triage System** for OmniCorp Solutions.

---

## Session Overview
- **Date**: August 6, 2025
- **AI Tooling**: Pi Coding Agent (Gemini Flash model)
- **Scope**: System Architecture Planning, Service Decomposition, API Contract Design, Java Microservices Implementation, React Frontend, Resiliency Strategy, Maven/Docker Configuration, and Documentation.

---

## Log Entries

### Entry 1: Prescreen Requirements Analysis & Architecture Proposal
**User Prompt:**
> create plan for implementing C:\Users\akamburov\Projects\EPAM-Prescreen

**AI Response Summary:**
- Analyzed `prescreen-brief-aleksandar-kamburov.md`.
- Proposed a multi-service monorepo structure:
  1. `submission-service` (Java 17 / Spring Boot, Port 8080)
  2. `ai-processing-service` (Java 17 / Spring Boot, Port 8081)
  3. `frontend` (React + Vite + Tailwind CSS, Port 3000)
- Designed an AI strategy using the **Strategy Pattern**:
  - `OpenAiProvider`: Live API calls using OpenAI Chat Completions endpoint.
  - `MockAiProvider`: Local rule-based fallback when no API key is provided or when rate limits/quota limits are encountered.
- Structured a 6-phase execution plan covering project setup, data models, REST endpoints, UI components, Docker Compose configuration, and documentation.

---

### Entry 2: Documentation First Strategy (README & AI Chat Log Generation)
**User Prompt:**
> Option B (Generate Documentation First)

**AI Action Summary:**
- Generated `README.md` containing system architecture diagram, API specifications, trade-offs matrix, Docker instructions, and AI disclosure statements.
- Created `ai-chat-log.md` detailing the AI interaction record as required by the submission guidelines.

---

### Entry 3: Full Project Implementation
**User Prompt:**
> yes (proceed with generating the codebase)

**AI Action Summary:**
- Created `ai-processing-service`: Spring Boot 3.2.3 application with WebClient, REST endpoints (`POST /api/v1/triage`), `OpenAiProvider`, and `MockAiProvider`.
- Created `submission-service`: Spring Boot 3.2.3 application with in-memory repository, async triage execution (`@Async`), REST endpoints (`POST /api/v1/feedback`, `GET /api/v1/feedback`), and `AiServiceClient`.
- Created `frontend`: Single Page Application with React 18, Vite, Tailwind CSS, Lucide Icons, Nginx configuration, and Dockerfile.
- Built Docker Compose orchestration (`docker-compose.yml`) with health checks and network isolation.

---

### Entry 4: Quota Error Resilience & Fallback Handling
**User Prompt:**
> try to continue after quota error / continue

**AI Action Summary:**
- Refactored `TriageService` in `ai-processing-service` to catch exceptions during live LLM calls (such as quota exceeded, network timeout, rate limit) and gracefully degrade to `MockAiProvider` so no feedback submission is ever lost or rejected.

---

### Entry 5: Docker Build Parent POM Refactoring
**User Prompt:**
> [FATAL] Non-resolvable parent POM for com.omnicorp:ai-processing-service...

**AI Action Summary:**
- Resolved Maven parent POM lookup failure during Docker container builds where sub-directories were used as build context.
- Configured `submission-service/pom.xml` and `ai-processing-service/pom.xml` to inherit directly from `spring-boot-starter-parent` (`3.2.3`), rendering each sub-service 100% self-contained for Docker while preserving the root aggregator POM for top-level builds.

---

### Entry 6: Version Control Configuration (`.gitignore`)
**User Prompt:**
> add gitignore to the different projects to exclude generated files

**AI Action Summary:**
- Created `.gitignore` in root directory and inside each module (`submission-service`, `ai-processing-service`, `frontend`).
- Excluded Maven build outputs (`target/`), Node dependencies (`node_modules/`), Vite bundle outputs (`dist/`), IDE files (`.idea/`, `.vscode/`, `*.iml`), and secrets (`.env`).

---

### Entry 7: Documentation & Model Branding Update
**User Prompt:**
> update ai-chat-log.md and readme.md with latest information , also replace claude sonnet with gemini flash model

**AI Action Summary:**
- Updated `README.md` and `ai-chat-log.md` to reflect all architectural additions, build resolution details, `.gitignore` setup, and updated model attribution to Gemini Flash model.

---

### Entry 8: Dependency Optimization & Spring Boot 3.2 `RestClient` Adoption
**User Prompt:**
> why services depend both on spring-boot-starter-webflux and spring-boot-starter-web, are they both needed?

**AI Action Summary:**
- Analyzed project dependencies and identified that `spring-boot-starter-webflux` was added solely for `WebClient`, which was being invoked synchronously using `.block()` within standard Spring MVC (Tomcat) applications.
- Recommended eliminating the reactive starter overhead by adopting Spring Boot 3.2's native, fluent synchronous `RestClient`.
- Removed `spring-boot-starter-webflux` from `submission-service/pom.xml` and `ai-processing-service/pom.xml`.
- Refactored `AiServiceClient` and `OpenAiProvider` from `WebClient` to `RestClient` (`RestClient.Builder`).
- Updated system architecture diagram in `README.md`.

---

### Entry 9: Code Review Refactoring & Enterprise Hardening
**User Prompt:**
> do all 9 fixes and also the 2 clean code suggestions

**AI Action Summary:**
- **Async Execution**: Created `AsyncConfig.java` configuring `ThreadPoolTaskExecutor` (core size 5, max size 20) to prevent unpooled thread allocation in `@Async` calls.
- **Socket Timeouts**: Configured explicit connect (3s) and read timeouts (10-15s) via `SimpleClientHttpRequestFactory` on all `RestClient` instances.
- **Repository Thread Safety**: Refactored `FeedbackRepository` to return defensive copies (`item.toBuilder().build()`) and eliminate shared object mutation race conditions.
- **Clean Code - Seed Data**: Extracted mock dataset creation from `FeedbackRepository` constructor into a decoupled `DataInitializer` `@Component`.
- **Clean Code - Domain Enums**: Created strongly typed `FeedbackCategory`, `FeedbackPriority`, and `Sentiment` Enums in `submission-service` matching `ai-processing-service`.
- **CORS Configuration**: Removed wildcard `@CrossOrigin("*")` and added `WebConfig` class with environment-configurable CORS mapping.
- **Validation Constraints**: Added `@Size(max = 2000)` constraints to input payload DTOs (`FeedbackSubmissionRequest` and `TriageRequest`).
- **Global Error Handling**: Added `@RestControllerAdvice` and structured `ErrorResponse` DTOs in both microservices to handle validation and runtime exceptions cleanly.
- **OpenAI Integration**: Configured `"response_format": { "type": "json_object" }` in `OpenAiProvider` to enforce native JSON output from OpenAI Chat Completions API.
- **Dashboard UI Optimization**: Refactored `HrDashboard.jsx` background polling to prevent UI jitter and added unmount memory guards.
- **Vite Chunk Optimization**: Updated `vite.config.js` with Rollup `manualChunks` splitting `vendor` and `icons` dependencies into separate bundles.

---

### Entry 10: Complete Refactoring, OpenAPI Java Record Mapping & Security Hardening
**User Prompt:**
> implement all fixes and clean code suggestions. also map OpenAPI JSON field names to Java record components.

**AI Action Summary:**
- **OpenAPI / Jackson Java Record Mapping**:
  - Refactored `TriageRequest`, `TriageResponse`, `TriageResponseDto`, `FeedbackSubmissionRequest`, and `ErrorResponse` from classes to Java 17 `record`s decorated with `@JsonProperty` field bindings.
  - Defined nested `record`s (`OpenAiChatResponse`, `Choice`, `Message`, `OpenAiTriagePayload`) in `OpenAiProvider` for typed JSON mapping using Jackson `ObjectMapper.readValue`.
- **OpenAI Parsing Null Safety**: Added defensive null/empty validation checks on Jackson response arrays (`choices`, `message`) before reading JSON components.
- **Open-Closed Principle Strategy Pipeline**: Refactored `TriageService` to inject `List<AiProviderStrategy>` ordered via `@Order(1)` (`OpenAiProvider`) and `@Order(2)` (`MockAiProvider`) instead of hardcoded qualifiers.
- **Repository Null-Safe Sorting**: Updated `FeedbackRepository.findAll()` to use `Comparator.nullsLast(Comparator.naturalOrder())` to prevent NPEs on missing timestamps.
- **Container Hardening**: Added unprivileged `appuser` / `appgroup` creation in Dockerfiles (`submission-service` and `ai-processing-service`) to run JVM processes without root privileges.
- **Nginx Security Headers**: Added `X-Frame-Options`, `X-Content-Type-Options`, and `Referrer-Policy` headers to `frontend/nginx.conf`.
- **React UI Form Preservation**: Refactored `App.jsx` tab navigation to use CSS display toggles (`block` / `hidden`) instead of component unmounting to preserve draft feedback state.
- **Clean Code Constants & Actuator Scope**: Extracted text summary truncation constants (`MAX_SUMMARY_LENGTH = 120`) in `MockAiProvider` and restricted Actuator exposure scope to `health,info` in `application.yml`.

---

### Entry 11: Google Gemini AI Provider Implementation
**User Prompt:**
> create google gemini ai provider

**AI Action Summary:**
- Created `GoogleGeminiProvider` implementing `AiProviderStrategy` with `@Order(2)`.
- Integrated Google Gemini 1.5 Flash REST API (`generativelanguage.googleapis.com`) using `x-goog-api-key` headers and `"responseMimeType": "application/json"`.
- Defined Java 17 records (`GeminiResponse`, `Candidate`, `CandidateContent`, `Part`, `GeminiTriagePayload`) with `@JsonProperty` mappings for type-safe JSON deserialization.
- Re-ordered strategy pipeline: `OpenAiProvider` (`@Order(1)`), `GoogleGeminiProvider` (`@Order(2)`), and `MockAiProvider` (`@Order(3)`).
- Updated `application.yml` and `docker-compose.yml` to support `GEMINI_API_KEY`.

---

### Entry 12: Configurable Gemini Model Upgrade & Default Model Configuration
**User Prompt:**
> set application.yml to use gemini-3.5-flash-lite model

**AI Action Summary:**
- Refactored `GoogleGeminiProvider` to make base URL (`gemini.api.url`) and model name (`gemini.api.model`) dynamically configurable via Spring properties and environment variables.
- Configured default model property in `application.yml`, `GoogleGeminiProvider`, and `docker-compose.yml` to `gemini-3.5-flash-lite`.

---

### Entry 13: Resolution of Docker Compose Dependency Healthcheck Error
**User Prompt:**
> Container omnicorp-ai-service Error dependency ai-processing-service failed to start

**AI Action Summary:**
- **Diagnosis**: Identified root causes for the Docker Compose dependency health check failure:
  1. **Alpine Linux IPv6 Resolution**: `curl -f http://localhost:8081/...` in Alpine container resolved `localhost` to IPv6 `::1`, whereas Tomcat listened on IPv4 `0.0.0.0`, resulting in `Connection refused` (exit code 7).
  2. **Insufficient Grace Period & Retries**: `start_period: 15s` and `retries: 5` were insufficient on Windows/WSL2 host machines for Spring Boot 3 / JVM initialization, causing Docker Compose to mark `ai-processing-service` as `UNHEALTHY` and block dependent services.
- **Fixes Applied**:
  - Replaced `http://localhost:8081` with `http://127.0.0.1:8081` in `docker-compose.yml` health checks to force IPv4 loopback.
  - Standardized health check test syntax to `["CMD-SHELL", "curl -f http://127.0.0.1:8081/actuator/health || exit 1"]`.
  - Increased `start_period` to `30s` and `retries` to `10` across microservices to accommodate JVM startup overhead.
  - Added matching health check and `condition: service_healthy` dependency to `submission-service` and `frontend`.

---

## Code Ownership & Understanding Verification

All architectural decisions, prompt engineering structures, service interaction patterns, and code components generated during this session have been reviewed and verified for clarity, security, and adherence to enterprise Java and clean software engineering principles.

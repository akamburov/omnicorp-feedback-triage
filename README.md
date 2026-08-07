# Employee Feedback Triage System

An automated multi-service enterprise prototype for OmniCorp Solutions. This system receives open-ended employee feedback, automatically categorizes, summarizes, and evaluates urgency/sentiment using an AI processing engine, and presents structured insights to HR Managers via a web dashboard.

---

## 🏛️ Architecture Overview

The system consists of three main components running in separate execution contexts:

```
+-------------------------------------------------------------------------+
|                                                                         |
|                          Frontend UI (React + Vite)                     |
|                   Employee Portal  |  HR Dashboard                      |
|                                                                         |
+------------------------------------+------------------------------------+
                                     |
                       HTTP / REST   |
                                     v
+-------------------------------------------------------------------------+
|                                                                         |
|                     Submission Service (Spring Boot)                    |
|                              Port: 8080                                 |
|                                                                         |
|   - POST /api/v1/feedback (Receive employee input)                      |
|   - GET  /api/v1/feedback (HR Dashboard query)                         |
|   - Managed ThreadPoolTaskExecutor for @Async triage processing          |
|   - In-Memory Storage & Status Tracking (PENDING, TRIAGED, FAILED)      |
|                                                                         |
+------------------------------------+------------------------------------+
                                     |
                HTTP REST (RestClient)| Inter-service Communication
                                     v
+-------------------------------------------------------------------------+
|                                                                         |
|                    AI Processing Service (Spring Boot)                  |
|                              Port: 8081                                 |
|                                                                         |
|   - POST /api/v1/triage (Analyze & summarize text)                      |
|   - Multi-Cloud LLM Strategy Pipeline:                                  |
|       1. OpenAI API Engine (gpt-4o-mini) [@Order(1)]                    |
|       2. Google Gemini API Engine (gemini-3.5-flash-lite) [@Order(2)]   |
|       3. Automated Mock Fallback Engine [@Order(3)]                     |
|   - Type-safe JSON record parsing (Category, Priority, Sentiment, etc.) |
|                                                                         |
+-------------------------------------------------------------------------+
```

---

## 🧩 Services & Component Breakdown

### 1. `submission-service` (Port 8080)
- Entry point for all external client interaction.
- Manages feedback lifecycle: `PENDING` $\rightarrow$ `TRIAGED` (or `FAILED`).
- Persists submissions in-memory with thread-safe defensive copying and dispatches asynchronous triage requests via a managed `ThreadPoolTaskExecutor`.
- Exposes REST APIs using Java 17 `record` DTOs and `@RestControllerAdvice` error responses.

### 2. `ai-processing-service` (Port 8081)
- Isolated service containing business intelligence and LLM integration logic.
- Implements an extensible **Strategy Pattern** pipeline for AI processing:
  - **OpenAI Provider** (`@Order(1)`): Sends structured prompts to OpenAI Chat Completions API (`gpt-4o-mini`) enforcing JSON mode (`response_format: { type: "json_object" }`).
  - **Google Gemini Provider** (`@Order(2)`): Connects to Google's Generative Language REST API (`gemini-3.5-flash-lite`) using `x-goog-api-key` headers and `responseMimeType: application/json`.
  - **Mock AI Provider** (`@Order(3)`): Deterministic, rule-based keyword & sentiment analyzer used when no external API keys are configured or when upstream LLM APIs fail (quota limit, network error, rate limit).
- Ensures the entire system remains 100% functional out-of-the-box without requiring an active paid subscription key.

### 3. `frontend` (Port 3000 / 5173)
- Modern Single Page Application (SPA) built with React 18, Vite, and Tailwind CSS.
- Optimized bundle splitting with Rollup `manualChunks` (vendor & icon chunks).
- Features dual view modes:
  - **Employee Submission View**: Submit feedback with input validation and real-time acknowledgment. Draft state is preserved during tab navigation.
  - **HR Management View**: Real-time table displaying triaged entries with background polling, category filtering, priority badges (Critical/High/Medium/Low), automated summaries, and recommended actions.

---

## 📡 API Specifications

### Submission Service (`http://localhost:8080`)

- **Submit Feedback**
  - `POST /api/v1/feedback`
  - Body:
    ```json
    {
      "content": "The AC in 3rd floor office is broken and freezing. It's impossible to work.",
      "department": "Engineering",
      "employeeName": "John Doe"
    }
    ```
  - Response (`201 Created`):
    ```json
    {
      "id": "e8a931c8-6701-4683-93fb-98d0c15926c4",
      "content": "The AC in 3rd floor office is broken and freezing. It's impossible to work.",
      "department": "Engineering",
      "employeeName": "John Doe",
      "status": "PENDING",
      "createdAt": "2025-08-06T18:30:00Z"
    }
    ```

- **Get All Feedback (HR View)**
  - `GET /api/v1/feedback`
  - Response (`200 OK`): Array of feedback records sorted by creation timestamp (null-safe).

- **Get Single Feedback Detail**
  - `GET /api/v1/feedback/{id}`

---

### AI Processing Service (`http://localhost:8081`)

- **Triage Request**
  - `POST /api/v1/triage`
  - Body:
    ```json
    {
      "submissionId": "e8a931c8-6701-4683-93fb-98d0c15926c4",
      "content": "The AC in 3rd floor office is broken and freezing. It's impossible to work."
    }
    ```
  - Response (`200 OK`):
    ```json
    {
      "submissionId": "e8a931c8-6701-4683-93fb-98d0c15926c4",
      "category": "FACILITIES",
      "priority": "HIGH",
      "sentiment": "NEGATIVE",
      "summary": "Employee reports severe temperature issues due to broken HVAC on the 3rd floor.",
      "actionableSteps": "Notify Facilities management team to dispatch an HVAC technician to 3rd floor.",
      "processedBy": "Google-gemini-3.5-flash-lite"
    }
    ```

---

## ⚖️ Key Design Decisions & Trade-offs

| Decision | Rationale | Production Trade-off / Alternative |
| :--- | :--- | :--- |
| **Spring Boot 3.2 `RestClient`** | Provides a modern, fluent, synchronous HTTP client built natively into Spring Boot without reactive starter overhead. | Can be swapped for non-blocking WebFlux if fully reactive event-driven streaming is required. |
| **Java 17 Records & Jackson `@JsonProperty`** | Ensures immutability, type safety, and clean JSON field mapping for all DTOs and LLM payloads. | Traditional mutable POJOs or Lombok data objects. |
| **Multi-Cloud AI Strategy (OpenAI $\rightarrow$ Gemini $\rightarrow$ Mock)** | Guarantees automatic fallback and zero downtime if an API key expires, rate limits occur, or network failures happen. | Single AI provider dependency with retry queues. |
| **Unprivileged Docker Execution (`USER appuser`)** | Microservice containers run as unprivileged non-root users (`uid=0` disabled) for security hardening. | Standard root execution. |
| **In-Memory Store with Defensive Copying** | Requires zero database installation while eliminating shared reference mutation bugs across threads. | Production requires persistent relational storage (PostgreSQL/MySQL). |

---

## 🚀 How to Build & Run

### Method 1: Docker Compose (Recommended)

Run all three services with a single command:

```bash
# Clone the repository
git clone <your-repository-url>
cd EPAM-Prescreen

# Option A: Run in Mock Fallback Mode (No API keys needed)
docker compose up --build

# Option B: Run with Google Gemini API Key
GEMINI_API_KEY=AIzaSy... docker compose up --build

# Option C: Run with OpenAI API Key
OPENAI_API_KEY=sk-... docker compose up --build

# Option D: Run with Both Keys (OpenAI primary, Gemini fallback, Mock tertiary)
OPENAI_API_KEY=sk-... GEMINI_API_KEY=AIzaSy... docker compose up --build
```

Access the applications:
- **Frontend UI**: `http://localhost:3000`
- **Submission Service API**: `http://localhost:8080/api/v1/feedback`
- **AI Service API**: `http://localhost:8081/api/v1/triage`

---

### Method 2: Running Locally (Maven & Node.js)

#### 1. Start AI Processing Service
```bash
cd ai-processing-service
# Set API keys optionally
export GEMINI_API_KEY="AIzaSy..."
mvn spring-boot:run
```

#### 2. Start Submission Service
```bash
cd submission-service
mvn spring-boot:run
```

#### 3. Start Frontend UI
```bash
cd frontend
npm install
npm run dev
```

---

## 🧪 End-to-End Testing Guide

1. Open `http://localhost:3000` in your web browser.
2. Select the **Employee Portal** tab.
3. Submit a feedback item:
   - *Example Text*: `"My computer crashes whenever I launch the software development environment. I need a RAM upgrade urgently."`
4. Switch to the **HR Dashboard** tab.
5. Verify that the entry appears triaged with:
   - **Category**: `IT`
   - **Priority**: `HIGH` / `MEDIUM`
   - **Processed By**: `Google-gemini-3.5-flash-lite`, `OpenAI-gpt-4o-mini`, or `Mock-RuleEngine-v1`
   - **Summary**: Auto-generated concise summary.
   - **Action**: Recommended IT action.

---

## 🤖 AI Assistant Disclosure & Conversation Logs

In accordance with the project guidelines, AI assistance was utilized during architecture planning, code refactoring, security hardening, and documentation.

- **AI Tools Used**: Pi Coding Agent (powered by Gemini Flash model)
- **Primary Use Cases**:
  - Designing multi-cloud AI provider fallback pipelines (OpenAI + Gemini + Local Mock).
  - Refactored Spring Boot HTTP communication from WebFlux/WebClient to native `RestClient`.
  - Converted DTOs and API payloads to Java 17 `record`s with Jackson `@JsonProperty` annotations.
  - Hardening Docker containers (`appuser` unprivileged user) and Nginx proxy headers.
  - Authoring project documentation, `.gitignore` rules, and comprehensive conversation logs.
- **Full Conversation Log**: See [`ai-chat-log.md`](./ai-chat-log.md) committed in the root directory.

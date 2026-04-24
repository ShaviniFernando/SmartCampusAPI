# 🏛️ SmartCampusAPI
### *Next-Generation RESTful API for Smart Campus Management*

[![JAX-RS](https://img.shields.io/badge/JAX--RS-3.1.3-blue.svg)](https://jakarta.ee/specifications/restful-ws/)
[![Java](https://img.shields.io/badge/Java-11+-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Build-Maven-red.svg)](https://maven.apache.org/)

---

## 📖 Overview
The **SmartCampusAPI** is a sophisticated, stateless RESTful service engineered using **JAX-RS (Jakarta REST)**. It serves as the digital backbone for a modern campus, providing real-time management of physical spaces (Rooms) and IoT infrastructure (Sensors). Designed with high availability and thread safety in mind, it utilizes advanced JAX-RS patterns such as Sub-Resource Locators and Custom Exception Mapping.

### 🌟 Key Pillars
*   **Performance:** Optimized in-memory operations using `ConcurrentHashMap`.
*   **Discoverability:** Full HATEOAS-compliant discovery endpoint.
*   **Robustness:** Global error handling with semantic HTTP status codes.
*   **Observability:** Comprehensive request/response logging via JAX-RS filters.

---

## 🏗️ System Architecture

```mermaid
graph TD
    Client[Client / Postman] -->|HTTP Request| Server[Grizzly/Tomcat Server]
    subgraph JAX-RS_Runtime ["JAX-RS Runtime (Jersey)"]
        Server -->|Intercept| Filter[LoggingFilter]
        Filter -->|Route| Resource{Resource Locator}
        Resource -->|GET/POST/DELETE| Handler[Resource Class]
        Handler -->|CRUD| Store[(In-Memory DataStore)]
    end
    Store -->|JSON Data| Handler
    Handler -->|Entity| Filter
    Filter -->|HTTP Response| Client
    
    style Store fill:#f9f,stroke:#333,stroke-width:2px
    style JAX-RS_Runtime fill:#f5f5f5,stroke:#666,stroke-dasharray: 5 5
```

---

## 🚀 Getting Started

### 📋 Prerequisites
*   **Java JDK 11** or higher
*   **Apache Maven 3.6+**

### 🛠️ Build & Installation
```bash
# 1. Clone the repository
git clone https://github.com/ShaviniFernando/SmartCampusAPI.git

# 2. Navigate to project directory
cd SmartCampusAPI

# 3. Build the project
mvn clean package
```

### 🏃 Running the Service
You can run the API either as a standalone application (embedded Grizzly) or deploy it to a container (Tomcat).

**Option A: Development Mode (Grizzly)**
```bash
# Start the server directly from source
mvn exec:java
```

**Option B: Servlet Mode (Tomcat)**
Deploy the generated `target/ROOT.war` to your Tomcat `webapps` folder. The API will be accessible at `http://localhost:8080/ROOT/api/v1`.

---

## 📡 API Reference

### 🗺️ Discovery & Navigation
| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/api/v1/` | `GET` | **API Discovery:** Returns version and HATEOAS links. |

### 🏠 Room Management
| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/api/v1/rooms` | `GET` | Retrieve all registered rooms. |
| `/api/v1/rooms` | `POST` | Register a new room. |
| `/api/v1/rooms/{id}`| `GET` | Get detailed metadata for a specific room. |
| `/api/v1/rooms/{id}`| `DELETE`| Remove a room (blocked if sensors are linked). |

### 🌡️ Sensor Network
| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/api/v1/sensors` | `GET` | List all sensors (Supports `?type=` filter). |
| `/api/v1/sensors` | `POST` | Register a sensor to a room (Atomic update). |
| `/api/v1/sensors/{id}`| `GET` | Get real-time sensor status. |

### 📊 Historical Data (Sub-Resource)
| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/api/v1/sensors/{id}/readings` | `GET` | Retrieve time-series history for a sensor. |
| `/api/v1/sensors/{id}/readings` | `POST` | Ingest new telemetry data. |

---

## ⌨️ Sample Operations (cURL)

> [!TIP]
> Use these commands to quickly verify your installation.

```bash
# 1. API Discovery (HATEOAS Links)
curl -X GET http://localhost:8080/api/v1/

# 2. Register a New Room
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"LAB-101","name":"AI Research Lab","capacity":25}'

# 3. Retrieve All Registered Rooms
curl -X GET http://localhost:8080/api/v1/rooms

# 4. Register a Sensor (Linked to Room)
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-01","type":"Temperature","status":"ACTIVE","currentValue":22.5,"roomId":"LAB-101"}'

# 5. Filter Sensors by Type
curl -G http://localhost:8080/api/v1/sensors --data-urlencode "type=Temperature"

# 6. Post a Sensor Reading (Updates Current Value)
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-01/readings \
  -H "Content-Type: application/json" \
  -d '{"id":"R-1001","timestamp":1713960000000,"value":24.3}'
```

---

## 📝 Technical Report (Conceptual Answers)

### 🟢 Part 1: Service Architecture & Setup

#### 1.1 JAX-RS Resource Lifecycle & Thread Safety
**Question:** *Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.*

**Analysis:** By default, JAX-RS resources operate on a **Request-Scoped** lifecycle. This means the JAX-RS runtime (Jersey) instantiates a new object for every incoming HTTP request and discards it once the response is dispatched. 
*   **Implication:** Because resource instances are ephemeral, class-level fields cannot be used to store persistent state. 
*   **Solution:** We implement a centralized `DataStore` using `static` collections. To ensure thread safety in a multi-threaded environment (where multiple requests might modify the state concurrently), we utilize **java.util.concurrent** structures like `ConcurrentHashMap`. This provides high-performance, lock-free reads and thread-safe writes, preventing race conditions and memory consistency errors without the bottleneck of heavy synchronization.

#### 1.2 The Strategic Value of HATEOAS
**Question:** *Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?*

**Analysis:** **HATEOAS** (Hypermedia as the Engine of Application State) elevates an API to Level 3 of the Richardson Maturity Model. By providing dynamic URIs in the response (as seen in our `/api/v1/` root), the API becomes **self-discoverable**. 
*   **Benefit:** It decouples the client from the server’s URI structure. If the endpoint paths change, the client—which follows links rather than hardcoding URLs—continues to function without modification. This significantly enhances the system's evolvability and reduces the maintenance burden on client developers, who can rely on the server to guide the application's state transitions rather than strictly following static, brittle documentation.

### 🔵 Part 2: Room Management

#### 2.1 Payload Optimization: IDs vs. Full Objects
**Question:** *When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.*

**Analysis:** This is a classic trade-off between **Bandwidth** and **Latency (N+1 Problem)**.
*   **Returning IDs only:** Significantly reduces the initial payload size, saving network bandwidth and making the primary response extremely fast. However, it forces the client to perform multiple subsequent "follow-up" requests (N requests for N items) to fetch the details of each referenced resource, which increases overall latency and server processing load.
*   **Returning Full Objects:** Minimizes the number of round-trips (reducing latency) by providing all data in one request, but results in much larger payloads and potential data redundancy. 
*   **Our Design:** We favor the ID approach to maintain a "flat" and efficient resource structure, ensuring high performance for mobile or bandwidth-constrained clients.

#### 2.2 Formal Idempotency of the DELETE Method
**Question:** *Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.*

**Analysis:** **Yes**, our implementation is strictly idempotent. Idempotency defines an operation where multiple identical requests have the same effect on the **server state** as a single request. 
*   **Scenario:** If a client mistakenly sends the same `DELETE` request twice:
    1.  The **first request** finds the resource, removes it from the `DataStore`, and returns `204 No Content`.
    2.  The **second request** finds that the resource no longer exists and returns `404 Not Found`. 
*   **Justification:** Although the HTTP status codes differ, the side effect on the server state is identical: the resource remains deleted. In REST, idempotency refers to the *state* of the server, not the response code received by the client.

### 🟡 Part 3: Sensor Operations & Linking

#### 3.1 Content-Type Integrity & @Consumes
**Question:** *We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?*

**Analysis:** JAX-RS uses **Content Negotiation** to ensure data integrity and contract enforcement. 
*   **Consequences:** If a client sends `text/plain` or `application/xml` to an endpoint that only `@Consumes` JSON, the JAX-RS runtime performs a pre-flight check on the `Content-Type` header. 
*   **Handling:** Before the business logic is even reached, the server rejects the request with a **415 Unsupported Media Type** status. This prevents the application from attempting to parse incompatible or potentially malicious data formats, ensuring that the resource method only receives data it is equipped to process.

#### 3.2 Filtering Strategy: @QueryParam vs. Path-based Design
**Question:** *You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?*

**Analysis:** In RESTful design, **Path Parameters** are used for *identity* (e.g., `/sensors/101`), whereas **Query Parameters** are used for *attributes* (e.g., `?type=CO2`).
*   **Superiority:** The query parameter approach is vastly more scalable and flexible. It allows for optional, combinable filters (e.g., `?type=CO2&status=ACTIVE`) without creating an explosion of hardcoded URI paths. Using paths for filtering leads to brittle routing logic and a confusing URI hierarchy, whereas query parameters follow the industry standard for searching, sorting, and filtering collections.

### 🔴 Part 4: Deep Nesting with Sub-Resources

#### 4.1 Architectural Benefits of Sub-Resource Locators
**Question:** *Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?*

**Analysis:** We utilize the **Sub-Resource Locator** pattern to manage the nested `/sensors/{id}/readings` path. 
*   **Management of Complexity:** Instead of creating a "God Class" that handles every operation for sensors, their metadata, and their telemetry history, we delegate the logic to a dedicated `SensorReadingResource`. 
*   **Benefits:** This promotes **Separation of Concerns** and follows the **Single Responsibility Principle**. It makes the codebase easier to navigate, test, and maintain. As the API grows, sub-resources can be modified or extended independently without risking regression in the parent resource logic, leading to a much cleaner, modular, and professional architecture.

### 🟣 Part 5: Security, Semantics & Cross-Cutting Concerns

#### 5.1 Resource Conflict Management (HTTP 409)
**Scenario:** *Attempting to delete a Room that still has Sensors assigned to it.*

**Analysis:** To maintain referential integrity, the API prevents the deletion of rooms that are "occupied" by active hardware. When a `DELETE` request is received, the system checks the room's `sensorIds` list. If not empty, we throw a custom `RoomNotEmptyException`. Our Exception Mapper translates this into a **409 Conflict**, informing the client that the request cannot be completed due to the current state of the resource (the presence of linked sensors).

#### 5.2 Semantic Accuracy: HTTP 422 vs. 404
**Question:** *Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?*

**Analysis:** Semantic accuracy is vital for API usability and debugging. 
*   **404 Not Found:** Implies the URL itself (the endpoint) does not exist.
*   **422 Unprocessable Entity:** Indicates that the URL and JSON syntax are correct, but the request contains **semantically invalid data** (e.g., a `roomId` that doesn't exist in the system). 
*   **Benefit:** Using 422 provides the client with specific feedback that the error lies in the *content provided*, not the *endpoint targeted*, facilitating much faster resolution of client-side logic errors.

#### 5.3 State Constraints & Business Logic (HTTP 403)
**Scenario:** *A sensor marked as "MAINTENANCE" cannot accept new readings.*

**Analysis:** This implements a **State Constraint**. When a new reading is POSTed, the API inspects the parent sensor's status. If the status is `MAINTENANCE`, the request is rejected with a `SensorUnavailableException`, mapped to **403 Forbidden**. This correctly communicates that while the client is authenticated and the resource is found, the specific operation is "forbidden" because of the current operational state of the hardware.

#### 5.4 Cybersecurity: The Danger of Stack Traces
**Question:** *From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?*

**Analysis:** Exposing stack traces is a critical security vulnerability known as **Information Leakage**. An attacker can extract:
1.  **Framework Versions:** Allowing them to target known exploits (CVEs).
2.  **Internal File Paths:** Revealing the server's directory structure and OS details.
3.  **Persistence Details:** Inferred from persistence-related errors.
4.  **Logic Vulnerabilities:** Revealing exact class names and internal method flows.
Our API uses a `GlobalExceptionMapper` to catch all unhandled errors and return a sanitized **500 Internal Server Error** without sensitive details.

#### 5.5 Decorator Pattern: JAX-RS Filters for Logging
**Question:** *Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?*

**Analysis:** This is an implementation of **Aspect-Oriented Programming (AOP)** principles. 
*   **DRY (Don't Repeat Yourself):** Centralizing logging in a `LoggingFilter` ensures that every request/response is captured automatically.
*   **Clean Business Logic:** It keeps our resource methods focused purely on business requirements rather than infrastructure concerns.
*   **Reliability:** It eliminates the risk of a developer forgetting to add a log statement to a new endpoint, ensuring 100% audit coverage across the entire API without cluttering the codebase.

---

## 🛠️ Error Handling Summary
The API implements a robust error-mapping layer to ensure consistent and informative responses:

| Status Code | Meaning | Context |
| :--- | :--- | :--- |
| `201 Created` | Success | Resource successfully created. |
| `204 No Content` | Success | Resource deleted/updated successfully. |
| `400 Bad Request` | Client Error | Invalid JSON syntax or missing required fields. |
| `403 Forbidden` | Client Error | Sensor is in `MAINTENANCE` mode. |
| `404 Not Found` | Client Error | Target resource does not exist. |
| `409 Conflict` | Client Error | Business rule violation (e.g., deleting a room with sensors). |
| `415 Unsupported Media Type` | Client Error | Client sent non-JSON data. |
| `422 Unprocessable Entity` | Client Error | Referenced `roomId` does not exist. |
| `500 Internal Server Error` | Server Error | An unexpected error occurred (Details hidden). |

---
*Developed as part of the Smart Campus IoT Initiative.*
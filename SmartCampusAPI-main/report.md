# Smart Campus Sensor & Room Management API - Report

## Part 1: Service Architecture & Setup

### 1.1 Project & Application Configuration

**Question:** In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

**Answer:** 
By default, JAX-RS resource classes (like `RoomResource`) are **request-scoped**. This means a new instance is created for every incoming HTTP request and discarded once the response is sent. While this promotes isolation, it means class-level state is not preserved. To manage persistence, we use a separate `DataStore` class with `static` members. Because multiple request threads access these static collections simultaneously, we must use thread-safe structures like `ConcurrentHashMap` and `CopyOnWriteArrayList` to prevent race conditions and ensure data consistency without the performance bottleneck of global synchronization.

---

### 1.2 The "Discovery" Endpoint

**Question:** Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

**Answer:**
HATEOAS (Hypermedia as the Engine of Application State) enables an API to be self-documenting. By including URIs to related resources within the response (as seen in our `/api/v1/` endpoint), the server guides the client on what actions are possible. This benefits developers by decoupling the client from hardcoded URL paths; if the API structure changes, the client simply follows the links provided in the response. It reduces the reliance on out-of-date static documentation and makes the API more discoverable and evolvable.

---

## Part 2: Room Management

### 2.1 Room Resource Implementation

**Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.

**Answer:**
Returning only IDs (e.g., in `Room.sensorIds`) significantly reduces network bandwidth and payload size, leading to faster initial response times. However, it forces the client to perform additional "follow-up" requests to fetch full details for each sensor (the N+1 query problem), which increases total latency and server load. Conversely, embedding full objects provides all necessary data in one round-trip but increases the bandwidth per request and may include data the client does not need. Our implementation favors IDs to keep the core resource representation lean and efficient.

---

### 2.2 Room Deletion & Safety Logic

**Question:** Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

**Answer:**
Yes, our `DELETE` implementation is idempotent. The first call finds the room, removes it, and returns `204 No Content`. A second, identical call will find that the room no longer exists and return `404 Not Found`. Although the status codes differ, the **server state** is identical after both calls: the room is gone. In REST, idempotency refers to the side effects on the server state being the same regardless of how many times the request is repeated.

---

## Part 3: Sensor Operations & Linking

### 3.1 Sensor Resource & Integrity

**Question:** We explicitly use the @Consumes(MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

**Answer:**
If a client sends a `Content-Type` other than `application/json` (like `text/plain`), the JAX-RS runtime performs a pre-flight check against the `@Consumes` annotation. Finding a mismatch, it immediately rejects the request with an **HTTP 415 Unsupported Media Type** response. This prevents the resource method from having to handle unparseable data, ensuring that the Jackson provider only attempts to deserialize formats it is configured for, thereby maintaining the integrity of the data processing pipeline.

---

### 3.2 Filtered Retrieval & Search

**Question:** You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

**Answer:**
Path parameters are intended to identify a **specific** resource, whereas query parameters are the standard for **filtering and searching** collections. The `@QueryParam` approach (`?type=CO2`) is superior because it allows for optional and combinable filters (e.g., `?type=CO2&status=ACTIVE`) without creating a rigid and complex URL hiearchy. Using paths for filtering (like `/type/CO2`) leads to "routing bloat" where every possible filter requires a new endpoint definition, making the API harder to maintain and scale.

---

## Part 4: Deep Nesting with Sub-Resources

### 4.1 The Sub-Resource Locator Pattern

**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

**Answer:**
The Sub-Resource Locator pattern promotes the **Single Responsibility Principle**. By delegating `/sensors/{id}/readings` to a dedicated `SensorReadingResource` class, we keep the code modular and focused. Instead of a single "God Class" handling dozens of endpoints, each class handles only one level of the URI hierarchy. This makes the codebase significantly easier to navigate, unit test, and extend, as developers can modify reading-specific logic without touching the core sensor management code.

---

### 4.2 Historical Data Management

**Functional Implementation Note:**

In your SensorReadingResource, ensure you have implemented:
- GET / to fetch reading history for a specific sensor
- POST / to append new readings for that sensor context
- Side Effect: A successful POST must update the currentValue field on the parent Sensor object

**Implementation Approach:**
The `SensorReadingResource` is a sub-resource delegated from `SensorResource`. It receives the `sensorId` via context. The `POST` method validates the sensor's status, auto-generates IDs and timestamps for the `SensorReading` POJO, and calls `DataStore.addReadingToSensor()`. This method appends the reading to the sensor's internal list and simultaneously updates the `currentValue` field, ensuring real-time data consistency.

---

## Part 5: Advanced Error Handling, Exception Mapping & Logging

### 5.1 Resource Conflict (409 Conflict)

**Implementation Details:**
- **Scenario:** Attempting to delete a Room that still has Sensors assigned to it
- **Exception:** RoomNotEmptyException
- **Response:** HTTP 409 Conflict with JSON body

**Technical Description:**
When `DELETE /rooms/{id}` is called, the resource checks the `DataStore` to see if any sensors are currently mapped to that room ID. If the list is not empty, a `RoomNotEmptyException` is thrown. The `RoomNotEmptyExceptionMapper` catches this and returns a `409 Conflict` response with a JSON payload `{ "error": "Conflict", "message": "Room still has active sensors assigned." }`.

---

### 5.2 Dependency Validation (422 Unprocessable Entity)

**Implementation Details:**
- **Scenario:** A client attempts to POST a new Sensor with a roomId that does not exist
- **Exception:** LinkedResourceNotFoundException
- **Response:** HTTP 422 Unprocessable Entity

**Question:** Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

**Answer:**
HTTP 404 implies that the **URL endpoint** itself was not found. In contrast, HTTP 422 (Unprocessable Entity) indicates that the server understands the content type and the syntax is valid, but it cannot process the contained instructions due to a semantic error—in this case, a broken logical reference. Using 422 tells the client "the request is well-formed, but the data is logically invalid," which provides much clearer diagnostic information than a generic "resource missing" error.

---

### 5.3 State Constraint (403 Forbidden)

**Implementation Details:**
- **Scenario:** A sensor currently marked with the status "MAINTENANCE" is physically disconnected and cannot accept new readings
- **Exception:** SensorUnavailableException
- **Response:** HTTP 403 Forbidden

**Technical Description:**
In the `SensorReadingResource.addReading()` method, the sensor is first retrieved from the `DataStore`. If its status is "MAINTENANCE", the system throws a `SensorUnavailableException`. The associated mapper converts this into an **HTTP 403 Forbidden** status, signaling that while the sensor exists, the operation is forbidden due to its current operational state.

---

### 5.4 The Global Safety Net (500 Internal Server Error)

**Question:** From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

**Answer:**
Exposing stack traces is a form of **Information Leakage** that can be exploited by attackers. A trace reveals internal package names, class structures, and potentially third-party library versions which can be checked against known vulnerabilities (CVEs). It may also expose sensitive file paths or database schema details. Hiding these behind a generic 500 error is a critical "defense in depth" measure that prevents attackers from gaining a blueprint of the system's internal workings.

**Implementation Details:**
- **Exception Mapper:** `GlobalExceptionMapper` implements `ExceptionMapper<Throwable>`
- **Response:** HTTP 500 Internal Server Error with generic error message

**Deployment:** We registered the `GlobalExceptionMapper` as a provider to catch all unhandled throwables. It logs the full stack trace to the server-side console for debugging but sends only a sanitized JSON response `{ "error": "Internal Server Error", "message": "An unexpected error occurred." }` to the client.

---

### 5.5 API Request & Response Logging Filters

**Question:** Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

**Answer:**
Using JAX-RS filters centrally implements the **Decorator** pattern and adheres to the **DRY (Don't Repeat Yourself)** principle. Centralized filters ensure that **every** request and response is captured automatically, eliminating the risk of a developer forgetting to add logging to a new endpoint. This separation of concerns keeps the resource classes clean, allowing them to focus purely on business logic while infrastructure concerns are handled in a single, maintainable location.

**Implementation Details:**
We implemented `LoggingFilter` which implements both `ContainerRequestFilter` and `ContainerResponseFilter`. It uses the `jakarta.annotation.Priority` and `@Provider` annotations to ensure it intercepts every transaction, logging the target URI, HTTP method, and final status code to the server log.

---

## Summary

### Key Architectural Decisions
The primary decision was to use **request-scoped resources** backed by a **thread-safe static DataStore**. This ensured that the API remains lightweight and stateless at the resource level while maintaining a high-performance in-memory state. We also prioritized **semantic HTTP status codes** (409, 422, 403) to provide a superior developer experience for API consumers.

### Notable Implementation Challenges
One challenge was maintaining the bi-directional relationship between Rooms and Sensors without a database. We resolved this by implementing update logic in the `DataStore` that automatically adds sensor IDs to the parent room when a sensor is created, and enforces deletion constraints (409) to ensure orphaned sensors are never created.

### Performance & Scalability Considerations
By using `ConcurrentHashMap`, we ensure that the API can handle high-concurrency workloads without data corruption. The use of **Sub-Resource Locators** ensures that even as the API grows (e.g., adding thousands of readings per sensor), the codebase remains modular and easy to scale from a development perspective.

---
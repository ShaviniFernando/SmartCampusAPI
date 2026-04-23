# SmartCampusAPI
# Smart Campus API - REST API for Sensor & Room Management

## Overview
A robust RESTful API built with **JAX-RS (Jakarta)** for managing campus rooms and IoT sensors in real-time. The API provides endpoints for CRUD operations, nested resource management, and comprehensive error handling.

## Key Features
- ✅ Complete CRUD operations for Rooms and Sensors
- ✅ Nested resource management (sensor readings)
- ✅ Query parameter filtering by sensor type
- ✅ Comprehensive error handling with custom HTTP status codes
- ✅ Request/Response logging for observability
- ✅ In-memory data storage (ConcurrentHashMap)
- ✅ HATEOAS-enabled discovery endpoint

## Technology Stack
- **Framework:** JAX-RS (Jersey 3.1.3)
- **Language:** Java 11+
- **Build Tool:** Maven 3.6+
- **Server:** Grizzly HTTP Server (embedded) OR Tomcat (servlet)
- **JSON:** Jackson via Jersey

---

## Build & Run Instructions

### Prerequisites
```bash
# Ensure you have Java 11+ installed
java -version

# Ensure you have Maven installed
mvn --version
```

### Step 1: Clone the Repository
```bash
git clone https://github.com/YOUR-USERNAME/smart-campus-api.git
cd smart-campus-api
```

### Step 2: Build the Project
```bash
mvn clean package
```

### Step 3: Run the Server
```bash
# Option A: Using embedded Grizzly server
java -jar target/SmartCampusAPI-1.0-SNAPSHOT-shaded.jar

# Option B: Deploy WAR to Tomcat
# Copy target/smart-campus-api-1.0.0.war to $CATALINA_HOME/webapps/
```

### Step 4: Verify API is Running
```bash
curl http://localhost:8080/api/v1/
```

Expected output:
```json
{
  "version": "1.0.0",
  "name": "Smart Campus API",
  "contact": {
    "name": "Campus IT",
    "email": "it@campus.edu"
  },
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

---

## API Endpoints Summary

### Discovery
- `GET /api/v1/` → Returns API metadata and resource links

### Rooms
- `GET /api/v1/rooms` → List all rooms
- `POST /api/v1/rooms` → Create a new room
- `GET /api/v1/rooms/{roomId}` → Get room by ID
- `DELETE /api/v1/rooms/{roomId}` → Delete room (fails if sensors exist)

### Sensors
- `GET /api/v1/sensors` → List all sensors
- `GET /api/v1/sensors?type=Temperature` → Filter sensors by type
- `POST /api/v1/sensors` → Create a new sensor
- `GET /api/v1/sensors/{sensorId}` → Get sensor by ID

### Sensor Readings (Sub-Resource)
- `GET /api/v1/sensors/{sensorId}/readings` → Get reading history
- `POST /api/v1/sensors/{sensorId}/readings` → Add new reading

---

## Sample cURL Commands

### 1. Get API Discovery
```bash
curl -X GET http://localhost:8080/api/v1/
```

### 2. Create a Room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "id": "LAB-101",
    "name": "Computer Lab",
    "capacity": 40
  }'
```

### 3. Create a Sensor (with valid roomId)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "TEMP-001",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 22.5,
    "roomId": "LAB-101"
  }'
```

### 4. Get All Sensors (Filtered by Type)
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"
```

### 5. Add a Reading to a Sensor
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{
    "timestamp": 1713960000000,
    "value": 23.1
  }'
```

### 6. Delete a Room (will fail if sensors exist)
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LAB-101
```

### 7. Test Error Handling - Non-existent Room for Sensor
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "TEST-001",
    "type": "CO2",
    "status": "ACTIVE",
    "currentValue": 400,
    "roomId": "NONEXISTENT"
  }'
# Expected: 422 Unprocessable Entity
```

### 8. Test Error Handling - Maintenance Sensor
```bash
# First, create a sensor with MAINTENANCE status
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "MAINT-001",
    "type": "Occupancy",
    "status": "MAINTENANCE",
    "currentValue": 5,
    "roomId": "LAB-101"
  }'

# Try to add a reading (will fail with 403)
curl -X POST http://localhost:8080/api/v1/sensors/MAINT-001/readings \
  -H "Content-Type: application/json" \
  -d '{
    "timestamp": 1713960000000,
    "value": 10
  }'
# Expected: 403 Forbidden
```

---

## Error Handling

### HTTP Status Codes
- **200 OK** → Successful GET/POST
- **201 Created** → Resource created successfully
- **204 No Content** → Successful DELETE
- **400 Bad Request** → Invalid input
- **404 Not Found** → Resource not found
- **409 Conflict** → Business logic violation (e.g., room with sensors)
- **422 Unprocessable Entity** → Invalid reference (missing room for sensor)
- **403 Forbidden** → Operation not allowed (sensor unavailable)
- **500 Internal Server Error** → Unexpected server error (no stack trace exposed)

### Error Response Format
```json
{
  "error": "Descriptive error message",
  "status": 409,
  "timestamp": 1713960000000
}
```

---

## Thread Safety & Concurrency

This API uses `ConcurrentHashMap` for in-memory data storage to ensure:
- **Thread-safe operations** across multiple concurrent requests
- **No data loss** during simultaneous reads/writes
- **Request isolation** with independent JAX-RS resource instances
- **Proper synchronization** of shared data structures

See the Report section below for detailed discussion of JAX-RS lifecycle management.

---

## Logging

All incoming and outgoing requests are logged to console using Java's built-in Logger via the `LoggingFilter` provider.

---

## Technical Report & Design Discussion

### 1.1 JAX-RS Resource Lifecycle & Thread Safety
**Question:** Explain the default lifecycle of a JAX-RS Resource class (request-scoped vs singleton). How does this impact managing in-memory data structures to prevent race conditions?

**Answer:** By default, JAX-RS resources (like `RoomResource`) are **request-scoped**. A new instance is created for every incoming HTTP request and destroyed after the response is sent. This promotes isolation but means that class-level fields cannot persist state across requests. To manage in-memory data, we use a separate `DataStore` with `static` members or a singleton pattern. Because multiple request threads access these static collections simultaneously, we must use thread-safe structures like `ConcurrentHashMap` and `CopyOnWriteArrayList` to prevent race conditions and memory consistency errors without the overhead of manual synchronization.

### 1.2 The Value of HATEOAS
**Question:** Why is HATEOAS considered a hallmark of advanced RESTful design? How does it benefit client developers compared to static documentation?

**Answer:** HATEOAS (Hypermedia as the Engine of Application State) allows a client to interact with the API entirely through responses provided dynamically by the server. By including URIs to related resources (as seen in our `/api/v1/` discovery endpoint), the API becomes self-documenting. This benefits developers by decoupling the client from hardcoded URL structures; if the API path changes, the client simply follows the new link provided in the discovery response, reducing maintenance effort and improving system evolvability.

### 2.1 Bandwidth vs. Processing: IDs vs. Full Objects
**Question:** What are the implications of returning only IDs vs full room objects in a sensor response?

**Answer:** Returning only IDs (e.g., in `Room.sensorIds`) reduces network bandwidth and payload size, making the initial response faster. However, it requires the client to make additional "follow-up" requests to fetch full details for each ID (the N+1 query problem), which increases latency and server load. Conversely, embedding full objects provides all data in one round-trip but increases bandwidth usage and may provide redundant data the client doesn't need. Our implementation favors IDs to keep the resource representation flat and efficient.

### 2.2 Idempotency of the DELETE Method
**Question:** Is DELETE idempotent in your implementation? Justify by describing what happens if the same DELETE request is sent multiple times.

**Answer:** Yes, our `DELETE` implementation is idempotent. The first request finds the resource, deletes it, and returns `204 No Content`. A second, identical request will find that the resource no longer exists and return `404 Not Found`. While the status codes differ, the *state of the server* remains the same after the first and any subsequent calls (the resource remains deleted). In REST, idempotency refers to the side effects on the server state, not necessarily the returned status code.

### 3.1 Handling Content-Type Mismatches
**Question:** What happens technically if a client sends `text/plain` or `application/xml` instead of JSON? How does JAX-RS handle the `@Consumes` mismatch?

**Answer:** When a resource method is annotated with `@Consumes(MediaType.APPLICATION_JSON)`, the JAX-RS runtime inspects the incoming `Content-Type` header. If a client sends an unsupported type (like `text/plain`), the server automatically rejects the request with a **415 Unsupported Media Type** error before the method is even executed. This ensures the method only receives data it is equipped to parse (via Jackson/JAXB).

### 3.2 Filtering: @QueryParam vs. Path-based Design
**Question:** Contrast `@QueryParam` filtering vs path-based design (e.g., `/sensors/type/CO2`). Why is the query parameter approach superior for filtering collections?

**Answer:** Path parameters (`@PathParam`) are best suited for identifying a **specific** resource (e.g., `/sensors/{id}`). Query parameters (`@QueryParam`) are the standard for **filtering, sorting, or searching** collections. The query parameter approach (`?type=CO2`) is superior because it allows for optional combinations (e.g., `?type=CO2&status=ACTIVE`) without creating an explosion of complex, hierarchical URL paths that would be difficult to maintain and route.

### 4.1 Sub-Resource Locator Pattern
**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating to separate classes help manage complexity?

**Answer:** The Sub-Resource Locator pattern (e.g., `getReadingResource` in `SensorResource`) promotes code modularity and follows the Single Responsibility Principle. Instead of one "God Class" handling every operation for sensors and their readings, we delegate reading-specific logic to `SensorReadingResource`. This makes the code easier to test, navigate, and maintain, as each class stays focused on its own tier of the URI hierarchy.

### 5.2 Semantics of HTTP 422 vs. 404
**Question:** Why is HTTP 422 more semantically accurate than 404 when the issue is a missing reference inside a valid JSON payload?

**Answer:** **HTTP 404** indicates that the URL itself (the endpoint) does not exist. **HTTP 422 (Unprocessable Entity)** indicates that the URL is correct and the JSON syntax is valid, but the *business logic* cannot be fulfilled—in this case, because a referenced `roomId` does not exist in the system. 422 tells the client "I understood your request, but the data you provided is logically invalid," which provides much better debugging context than a generic "Not Found."

### 5.4 Risks of Exposing Stack Traces
**Question:** From a cybersecurity standpoint, what risks come from exposing internal Java stack traces? What specific info could an attacker gather?

**Answer:** Exposing stack traces is a significant security risk (Information Leakage). An attacker can learn:
1. **Internal class structures** and package names.
2. **Third-party library versions**, which can be checked against known CVEs.
3. **Internal logic flow** and sensitive variable names.
4. **Environment details** (e.g., server paths, OS info).
Using a `GlobalExceptionMapper` ensures that these details are hidden, returning only a generic "Internal Server Error" to the user while logging the full trace safely on the server.

### 5.5 Benefits of JAX-RS Filters for Logging
**Question:** Why is it better to use JAX-RS filters for cross-cutting concerns like logging, rather than adding `Logger.info()` in every resource method?

**Answer:** Using `ContainerRequestFilter` and `ContainerResponseFilter` implements the **Decorator** pattern and ensures **Don't Repeat Yourself (DRY)**. It centralizes logging logic in one class (`LoggingFilter`), ensuring that *every* request and response is logged automatically, regardless of which resource is called. This prevents developer error (forgetting to log in a new method) and keeps business logic clean and focused solely on processing data, rather than being cluttered with infrastructure concerns.
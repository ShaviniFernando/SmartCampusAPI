# 5COSC022W Coursework Report

### 1.1 Project & Application Configuration
**"Explain the default lifecycle of a JAX-RS Resource class. Is a new instance created per request, or is it a singleton? How does this impact managing in-memory data structures (maps/lists) to prevent data loss or race conditions?"**

By default, JAX-RS handles resource classes using a *request-scoped* lifecycle. This means that a completely new instance of the resource class (e.g., `RoomResource`) is instantiated by the container for every single incoming HTTP request, and the instance is garbage-collected once the response is sent. 

Because of this transient lifecycle, we absolutely cannot store persistent state in instance variables of the resource class itself (as that data would disappear when the request ends). To handle in-memory persistence across isolated requests, we must externalize our storage into a JVM-wide state. In this project, this was achieved by using a dedicated static `DataStore` class containing `ConcurrentHashMap` structures. The `ConcurrentHashMap` specifically ensures thread safety, preventing race conditions or data corruption when multiple request-scoped resource instances attempt to read or write data concurrently.

---

### 1.2 The Discovery Endpoint
**"Why is Hypermedia (HATEOAS) considered a hallmark of advanced RESTful design? How does it benefit client developers vs static documentation?"**

Hypermedia As The Engine Of Application State (HATEOAS) is considered Richardson’s highest maturity level (Level 3) for REST architectures. It transforms the API from a static directory of endpoints into a self-navigating application state machine. 

For client developers, HATEOAS drastically reduces coupling between the frontend and the backend URI structure. Instead of hard-coding REST paths (like `/api/v1/sensors`) directly into client applications based on static documentation, the client can parse the discovery endpoint's JSON to find the authoritative URLs dynamically. If the backend needs to rename a path or version an endpoint in the future, the client automatically adapts by following the new hypermedia links, vastly reducing breaking changes.

---

### 2.1 Room Resource Implementation
**"When returning a list of rooms, what are the implications of returning only IDs versus full room objects? Consider network bandwidth and client-side processing."**

Returning **only IDs** minimizes the payload size footprint significantly, lowering bandwidth consumption and speeding up the initial network transfer. However, this severely increases client-side processing latency because the client must subsequently make N additional HTTP `GET` requests (for each ID) to assemble the full dataset, leading to an N+1 fetching problem. 

Conversely, returning **full room objects** initially costs more bandwidth per payload and uses more server memory to serialize. However, it requires only a single network round-trip, significantly optimizing Time-to-First-Meaningful-Paint (TTFMP) on the frontend. For modest lists like a university campus room directory, returning full objects is almost always superior to save HTTP request overhead.

---

### 2.2 Room Deletion & Safety Logic
**"Is DELETE idempotent in your implementation? Justify by describing what happens if the same DELETE request is sent multiple times."**

Yes, the `DELETE` implementation is idempotent, as required by REST principles. Idempotency dictates that performing an action multiple times yields the same ultimate system state as performing it precisely once. 

If a client sends `DELETE /api/v1/rooms/ROOM-01`, the first request will remove the room and return a `204 No Content`. If the client executes that exact same `DELETE` request a second, third, or hundredth time, the `RoomResource` checks the empty `DataStore`, realizes the room isn't there, and returns a `404 Not Found`. Despite the different HTTP response code, the *server-side system state* remains exactly the same logic condition: the room is deleted. The repeated actions cause no extra side effects. 

---

### 3.1 Sensor Resource & Integrity
**"What are the technical consequences if a client sends data as text/plain or application/xml instead of JSON? How does JAX-RS handle this @Consumes mismatch?"**

If a client sends data using an invalid format, JAX-RS leverages its `@Consumes(MediaType.APPLICATION_JSON)` annotation to intercept the discrepancy at the framework boundary, before the code even executes.

JAX-RS compares the incoming `Content-Type` HTTP header against the stated acceptable media types. Because of a mismatch (e.g., `text/plain`), JAX-RS realizes there is no `MessageBodyReader` capable of mapping the raw bytes into the required POJO (`Sensor.class`). Consequently, it immediately aborts connection routing and automatically generates an HTTP `415 Unsupported Media Type` response to the client. This protects the backend application logic from having to manually parse invalid payload formats.

---

### 3.2 Filtered Retrieval & Search
**"Contrast @QueryParam filtering vs path-based design (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally superior for filtering?"**

Path-based design (`/sensors/type/CO2`) is excellent for identifying unique, hierarchal resources. However, it is structurally rigid. Filtering is a dynamic subset operation, not a structural resource identifier.

Using `@QueryParam` (`/sensors?type=CO2`) is superior because query strings are inherently optional and composable. If you needed to filter by type *and* status, you can easily stack parameters (`?type=CO2&status=ACTIVE`) without completely breaking your API tree. Trying to chain multiple optional filters using only Path Params would result in exponentially convoluted routing tables (e.g., `/sensors/type/{type}/status/{status}`) that fail if a user only wants to provide the latter argument. 

---

### 4.1 Sub-Resource Locator Pattern
**"Discuss architectural benefits of Sub-Resource Locator pattern. How does delegating to separate classes help manage complexity compared to defining all nested paths in one massive controller?"**

The Sub-Resource Locator acts exactly like a factory or routing table within the JAX-RS ecosystem. Instead of a single monolithic `SensorResource` processing endpoints 5 levels deep in the API hierarchy, the parent resource simply intercepts the `{id}/readings` path and delegates the execution to a freshly instantiated `SensorReadingResource`.

This maintains the Single Responsibility Principle and achieves high cohesion. The `SensorResource` remains purely focused on top-level Sensor entity logic, while `SensorReadingResource` manages the distinct historical timeseries behavior. In massive APIs, this allows entire teams to work on independent sub-resource classes without creating fatal merge conflicts or bloated "God classes."

---

### 5.2 Dependency Validation — 422
**"Why is HTTP 422 often more semantically accurate than 404 when the issue is a missing reference inside a valid JSON payload?"**

A `404 Not Found` implies that the endpoint URI itself (`http://localhost/api/v1/sensors`) does not exist on the server. If a client targets a valid endpoint but embeds an invalid foreign key in the body (e.g., `roomId: "LAKE-5"`), returning a `404` is highly ambiguous. The client won't know if the URL was typoed, or if the data inside the body triggered the failure.

HTTP `422 Unprocessable Entity` explicitly solves this constraint perfectly. A 422 declares: "The server understands the content-type (JSON), and the syntax of the JSON body is perfect, but I cannot process the embedded instructions because of semantic logical errors." It points precisely to bad data references rather than a bad routing path.

---

### 5.4 Global Safety Net — 500
**"From a cybersecurity standpoint, explain the risks of exposing internal Java stack traces to external API consumers. What specific info could an attacker gather?"**

Exposing raw Java stack traces constitutes a catastrophic Information Disclosure vulnerability. 

If an attacker forces a `NullPointerException`, a stack trace maps out the entire execution pathway of the backend. They gain visibility into the precise internal package architecture (e.g., `com.smartcampus.config.DataStore`), the utilized third-party libraries and potentially their specific versions (like Jersey internals or Jackson parse trees), and the internal file system layouts depending on where the error occurred. An attacker will use this reconnaissance to look for unpatched exploits in those precise dependencies or construct tailored SQL injections/RCE payloads now that they understand the internal schema logic. A global 500 mapper neutralizes this by swallowing the trace and returning safe, opaque error JSON.

---

### 5.5 API Request & Response Logging Filters
**"Why is it better to use JAX-RS filters for cross-cutting concerns like logging, rather than manually adding Logger.info() in every resource method?"**

Manually placing `Logger.info()` at the top of every resource method violates the DRY (Don't Repeat Yourself) principle and pollutes core business logic with infrastructure code.

Using JAX-RS Container Filters allows for an Aspect-Oriented design. By intercepting inbound contexts (`ContainerRequestFilter`) and outbound answers (`ContainerResponseFilter`), the server uniformly guarantees that every single footprint is logged, even for resources a developer forgot to annotate, or for requests that failed at the routing phase before a resource method was ever invoked. This creates comprehensive, guaranteed technical observability with cleaner, decoupled code.

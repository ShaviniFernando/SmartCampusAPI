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
java -jar target/smart-campus-api-1.0.0-shaded.jar

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

All incoming and outgoing requests are logged to console using Java's built-in Logger:
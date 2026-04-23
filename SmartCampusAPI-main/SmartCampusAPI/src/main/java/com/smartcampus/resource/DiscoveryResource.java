package com.smartcampus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Discovery endpoint for the Smart Campus API.
 * GET /api/v1 — returns API version, admin contact, and resource map (HATEOAS).
 */
@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDiscovery() {
        Map<String, Object> discovery = new LinkedHashMap<>();
        discovery.put("name", "Smart Campus API");
        discovery.put("version", "1.0.0");
        discovery.put("description", "RESTful API for managing campus rooms and IoT sensors.");

        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("name", "Campus IT Administration");
        contact.put("email", "admin@smartcampus.edu");
        contact.put("phone", "+94-11-000-0000");
        discovery.put("contact", contact);

        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        resources.put("readings", "/api/v1/sensors/{sensorId}/readings");
        discovery.put("resources", resources);

        return Response.ok(discovery).build();
    }
}

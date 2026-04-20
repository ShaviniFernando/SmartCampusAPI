package com.smartcampus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDiscovery() {
        Map<String, Object> discovery = new HashMap<>();
        discovery.put("version", "1.0.0");
        discovery.put("name", "Smart Campus API");
        
        Map<String, String> contact = new HashMap<>();
        contact.put("name", "Campus IT");
        contact.put("email", "it@campus.edu");
        discovery.put("contact", contact);
        
        Map<String, String> resources = new HashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        discovery.put("resources", resources);
        
        return Response.ok(discovery).build();
    }
}
